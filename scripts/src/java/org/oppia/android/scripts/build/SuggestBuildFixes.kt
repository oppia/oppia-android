package org.oppia.android.scripts.build

import java.io.BufferedReader
import java.io.File
import java.util.concurrent.TimeUnit
import org.oppia.android.scripts.build.SuggestBuildFixes.PeekableSequence.Companion.toPeekable
import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.util.math.PeekableIterator
import org.oppia.android.util.math.PeekableIterator.Companion.toPeekableIterator

fun main(vararg args: String) {
  require(args.size > 2) {
    "Usage: bazel run //scripts:suggest_build_fixes --" +
      " <root_directory> <mode=deltas/fix>[_force] <bazel_target_exp:String> ..."
  }
  val repoRoot = File(args[0]).absoluteFile.normalize().also {
    require(it.exists() && it.isDirectory) {
      "Provided repository root doesn't exist or isn't a directory: $it."
    }
  }
  require(args[1].startsWith("mode=")) { "Expected 'mode' argument to start with 'mode='." }
  val expFastRun = args[1].endsWith("_exp_fast_run")
  val mode = when (val modeStr = args[1].removePrefix("mode=").removeSuffix("_exp_fast_run")) {
    "deltas" -> SuggestBuildFixes.OutputMode.DELTAS
    "fix" -> SuggestBuildFixes.OutputMode.FIX
    else -> error("Expected mode to be one of: 'deltas' or 'fix', not: $modeStr.")
  }
  ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
    val commandExecutor =
      CommandExecutorImpl(
        scriptBgDispatcher, processTimeout = 30, processTimeoutUnit = TimeUnit.MINUTES
      )
    val bazelClient = BazelClient(repoRoot, commandExecutor)
    SuggestBuildFixes(repoRoot, bazelClient)
      .suggestBuildFixes(args.drop(2).toList(), mode, expFastRun)
  }
}

// TODO: Update to assemble an actual dependency graph to traverse. This has major benefits:
//  - It should significantly speed up rebuilding (since building should always start at the bottom
//    of the graph).
//  - It theoretically allows for batch building by asking Bazel to build unrelated targets (care
//    needs to be taken during output parsing) which also means utilizing parallelization for faster
//    builds.
//  - It allows for correct target specification when mentioning import failures (since the exact
//    target will be known).
//  (Follow-up: is the above still needed with the new pruning technique + retry?)
class SuggestBuildFixes(private val repoRoot: File, private val bazelClient: BazelClient) {
  fun suggestBuildFixes(targetPatterns: List<String>, outputMode: OutputMode, expFastRun: Boolean) {
    suggestBuildFixesAux(targetPatterns, outputMode, expFastRun)
  }

  private fun suggestBuildFixesAux(
    targetPatterns: List<String>,
    outputMode: OutputMode,
    expFastRun: Boolean,
    previousFailures: Set<DetectedFailure> = emptySet()
  ) {
    val allTargets = targetPatterns.flatMap {
      // Ignore 'manual' tagged builds as they aren't meant to be included in broad patterns.
      bazelClient.query("set($it) - attr(tags, 'manual', $it)", withSkyQuery = true)
    }
    println(
      "Trying to build ${allTargets.size} total targets from ${targetPatterns.size} pattern(s):"
    )
    targetPatterns.forEach { println("- $it") }
    println()

    // First, build the targets to ensure unrelated out-of-date actions are addressed.
    if (expFastRun) {
      println("[Experimental] Pre-building to compute which targets actually require reanalysis...")
    } else println("Pre-building to improve analyzing performance...")
    targetPatterns.forEach { bazelClient.build(it, keepGoing = true, allowFailures = true) }

    // Second, determine which targets are failing and require reworking.
    val targetsRequiringAnalysis = if (expFastRun) {
      targetPatterns.flatMap { targetPattern ->
        bazelClient.build(
          targetPattern,
          keepGoing = true,
          allowFailures = true,
          configProfiles = setOf("only_check_for_failures")
        ).mapNotNull { outputLine ->
          outputLine.takeIf { it.startsWith("ERROR: ") }
            ?.substringAfter("ERROR: ", missingDelimiterValue = "")
        }.mapNotNull { errorLine ->
          check(errorLine.startsWith("action '") && errorLine.endsWith("is not up-to-date")) {
            "Encountered unexpected failure while building pattern '$targetPattern':\n$errorLine" +
              "\nPlease resolve this manually."
          }
          // Ignore out-of-date actions that don't include targets, and ones that have targets which
          // aren't tied to the main repository (since these should be built incidentally).
          return@mapNotNull errorLine.takeIf { line ->
            '@' in line
          }?.substringAfter('@', missingDelimiterValue = "")?.substringBefore(' ')?.takeIf {
            it.startsWith("//")
          }
        }
      }.mapToSet(Target::parse)
    } else {
      println("Forcing all targets to be analyzed (per command line).")
      allTargets.mapToSet(Target::parse)
    }

    val targetCount = targetsRequiringAnalysis.size
    println("$targetCount/${allTargets.size} target(s) require analysis.")
    println()

    val allFailures = targetsRequiringAnalysis.mapIndexed { index, target ->
      printTargetLine(prefix = "Inspecting (${index + 1}/$targetCount) ", target, suffix = "")
      findIssuesWithTarget(target)
    }
    if (targetsRequiringAnalysis.isNotEmpty()) println()

    val unknownFailures = allFailures.filterIsInstance<DetectedFailure.Unknown>()
    val noteworthyFailures = allFailures.filterNot {
      it is DetectedFailure.NoFailure || it is DetectedFailure.Unknown
    }.distinct().sortedBy { it.target }
    check(unknownFailures.isEmpty()) {
      "Encountered unknown failures for targets: ${unknownFailures.mapToSet { it.target }}." +
        " Please resolve them directly and try again."
    }

    for (failure in noteworthyFailures) {
      val buildFile = StarlarkBuildFileInterpreter.interpret(repoRoot, failure.target.pkg)
      val target = buildFile.findTarget(failure.target.name)
        ?: error("Cannot find failing target in its corresponding BUILD file: ${failure.target}.")
      val adjustedOutputMode = if (outputMode == OutputMode.FIX && !target.canBeRegenerated) {
        println("WARNING: Cannot auto-fix target.")
        OutputMode.DELTAS
      } else outputMode
      when (failure) {
        is DetectedFailure.UnresolvedReferences -> {
          printTargetLine("Add deps to ", failure.target, " (or a dep) for missing imports:")
          println("  in ${buildFile.file.toRelativeString(repoRoot)}")
          failure.unresolvableImports.forEach { println(" - $it") }
          println()
        }
        is DetectedFailure.StrictDeps -> {
          val action = if (adjustedOutputMode == OutputMode.FIX) "Adding" else "Add"
          printTargetLine("$action strict deps to ", failure.target, ":")
          println("  in ${buildFile.file.toRelativeString(repoRoot)}")
          failure.targetsToAdd.forEach {
            println("    \"${it.correctedTarget.simpleQualifiedTargetPath}\",")
          }
          when (adjustedOutputMode) {
            OutputMode.DELTAS -> {} // Nothing extra to do.
            OutputMode.FIX -> {
              buildFile.regenerateTarget(failure.target.name) { oldDeps ->
                oldDeps + failure.targetsToAdd.mapToSet(InterpretedTarget::correctedTarget)
              }
            }
          }
          println()
        }
        is DetectedFailure.UnusedDeps -> {
          val action = if (adjustedOutputMode == OutputMode.FIX) "Removing" else "Remove"
          printTargetLine("$action unused deps from ", failure.target, ":")
          println("  in ${buildFile.file.toRelativeString(repoRoot)}")
          failure.targetsToRemove.forEach {
            println("    \"${it.correctedTarget.simpleQualifiedTargetPath}\",")
          }
          when (adjustedOutputMode) {
            OutputMode.DELTAS -> {} // Nothing extra to do.
            OutputMode.FIX -> {
              buildFile.regenerateTarget(failure.target.name) { oldDeps ->
                oldDeps - failure.targetsToRemove.mapToSet(InterpretedTarget::correctedTarget)
              }
            }
          }
          println()
        }
        is DetectedFailure.NoFailure, is DetectedFailure.Unknown ->
          error("Something internally went wrong in the script.")
      }
    }

    when {
      noteworthyFailures.isEmpty() -> println("No issues found in provided target patterns.")
      previousFailures == noteworthyFailures.toSet() && outputMode == OutputMode.FIX ->
        println("All remaining issues cannot be auto-resolved. Please fix them manually.")
      outputMode == OutputMode.FIX -> {
        println("Issues were fixed, re-running check to ensure everything has been fixed.")
        if (expFastRun) {
          println(
            "WARNING: Fast running was enabled. This is experimental and may miss targets." +
              " Suggest re-running with it disabled to ensure everything is up-to-date."
          )
        }
        println()
        suggestBuildFixesAux(
          targetPatterns, outputMode, expFastRun, previousFailures = noteworthyFailures.toSet()
        )
      }
      else -> println("Please fix the issues described above and try again.")
    }
  }

  private fun findIssuesWithTarget(target: Target): DetectedFailure =
    DetectedFailure.detectFailures(bazelClient, target)

  enum class OutputMode {
    DELTAS,
    FIX
  }

  private sealed class DetectedFailure {
    abstract val target: Target

    data class NoFailure(override val target: Target) : DetectedFailure()

    data class Unknown(override val target: Target) : DetectedFailure()

    data class UnresolvedReferences(
      override val target: Target,
      val unresolvableImports: Set<String>
    ): DetectedFailure()

    data class StrictDeps(
      override val target: Target, val targetsToAdd: Set<InterpretedTarget>
    ): DetectedFailure()

    data class UnusedDeps(
      override val target: Target, val targetsToRemove: Set<InterpretedTarget>
    ): DetectedFailure()

    companion object {
      fun detectFailures(bazelClient: BazelClient, target: Target): DetectedFailure {
        val failureLines = bazelClient.build(target.fullyQualifiedTargetPath, allowFailures = true)
        val correctedTarget = target.normalize()

        val targetForDepAdding = failureLines.singleOrNull {
          it.trim().startsWith("** Please add the following dependencies to")
        }?.trim()?.removePrefix("** Please add the following dependencies to")
          ?.trim()?.removeSuffix(":")?.parseNormalizedTarget() ?: correctedTarget
        val depsToAdd = failureLines.asSequence().map { it.trim() }.dropUntil {
          it.trim().startsWith("** Please add the following dependencies to")
        }.takeUntil { !it.startsWith("- ") }.map { it.removePrefix("- ") }.map {
          if ("Target //" in it) it.substringBefore("Target //") else it
        }.mapToSet { InterpretedTarget.interpretTarget(bazelClient, it) }

        val targetForDepRemoval = failureLines.asSequence().map {
          it.removePrefix("INFO:").trim()
        }.singleOrNull {
          it.startsWith("** Please remove the following dependencies from")
        }?.removePrefix("** Please remove the following dependencies from")
          ?.trim()?.removeSuffix(":")?.parseNormalizedTarget() ?: correctedTarget
        val depsToRemove = failureLines.asSequence().map {
          it.removePrefix("INFO:").trim()
        }.dropUntil {
          it.trim().startsWith("** Please remove the following dependencies from")
        }.takeUntil { !it.startsWith("- ") }.map { it.removePrefix("- ") }.mapToSet {
          InterpretedTarget.interpretTarget(bazelClient, it)
        }

        val targetForJavaDepAdding = failureLines.asSequence().dropUntil {
          it.trim().startsWith("** Please add the following dependencies:")
        }.firstOrNull()?.substringAfterLast(" to ", missingDelimiterValue = "")?.trim()
          ?.removeSuffix(":")?.parseNormalizedTarget() ?: correctedTarget
        val depsToAddForJava = failureLines.asSequence().map { it.trim() }.dropUntil {
          it.trim().startsWith("** Please add the following dependencies:")
        }.firstOrNull()?.substringBefore(" to ")?.trim()?.split(" ")?.mapToSet {
          InterpretedTarget.interpretTarget(bazelClient, it)
        } ?: emptySet()

        val unresolvedReferences = failureLines.mapIndexedNotNull { index, line ->
          index.takeIf { "unresolved reference" in line }?.let { it + 1 }
        }.mapNotNull(failureLines::getOrNull).filter {
          it.startsWith("import")
        }.mapTo(mutableSetOf()) { it.substringAfter("import ", missingDelimiterValue = "") }
        val hasFailure = failureLines.any { "ERROR:" in it }

        return when {
          !hasFailure -> NoFailure(correctedTarget)
          depsToAdd.isNotEmpty() -> StrictDeps(targetForDepAdding, depsToAdd)
          depsToRemove.isNotEmpty() -> UnusedDeps(targetForDepRemoval, depsToRemove)
          depsToAddForJava.isNotEmpty() -> StrictDeps(targetForJavaDepAdding, depsToAddForJava)
          unresolvedReferences.isNotEmpty() ->
            UnresolvedReferences(correctedTarget, unresolvedReferences)
          else -> Unknown(correctedTarget)
        }
      }
    }
  }

  private sealed class InterpretedTarget {
    abstract val correctedTarget: Target

    data class Unknown(val originalRawTarget: String): InterpretedTarget() {
      override val correctedTarget get() = error("Unknown target: $originalRawTarget.")
    }

    data class Resolved(val target: Target): InterpretedTarget() {
      override val correctedTarget by lazy { target.normalize() }
    }

    companion object {
      private val cachedTargetMapping = mutableMapOf<String, InterpretedTarget>()
      private val mavenThirdPartyPrefixMapping = mapOf(
        Workspace.parse("@maven_app") to "//third_party",
        Workspace.parse("@maven_scripts") to "//scripts/third_party"
      )
      private val externalRepoReferenceFixesMapping = mapOf(
        Workspace.parse("@com_google_protobuf_protobuf_javalite") to
          Target.parse("//third_party:com_google_protobuf_protobuf"),
        Workspace.parse("@kotlitex") to Target.parse("//third_party:io_github_karino2_kotlitex"),
        Workspace.parse("@guava_android") to Target.parse("//third_party:com_google_guava_guava"),
        Workspace.parse("@circularimageview") to
          Target.parse("//third_party:circularimageview_circular_image_view"),
        Workspace.parse("@androidsvg") to Target.parse("//third_party:com_caverock_androidsvg"),
        Workspace.parse("@android-spotlight") to
          Target.parse("//third_party:com_github_takusemba_spotlight")
      )

      fun interpretTarget(bazelClient: BazelClient, rawTarget: String): InterpretedTarget {
        return cachedTargetMapping.getOrPut(rawTarget) {
          val parsedTarget = Target.parseNonStrict(rawTarget)
          val workspace = parsedTarget?.workspace
          return@getOrPut when {
            rawTarget.endsWith("_proto") ->
              bazelClient.interpretProtoTarget(rawTarget)
            rawTarget.startsWith("//") || rawTarget.startsWith("@//") ->
              Resolved(Target.parse(rawTarget))
            workspace in mavenThirdPartyPrefixMapping ->
              bazelClient.interpretMavenTarget(rawTarget, parsedTarget)
            "/_aar/" in rawTarget ->
              bazelClient.interpretAarTarget(rawTarget, parsedTarget)
            // Special case re-interpretations since deps use a different target for these repos.
            workspace in externalRepoReferenceFixesMapping ->
              Resolved(externalRepoReferenceFixesMapping.getValue(workspace))
            "kotlinx-coroutines-core-jvm" in rawTarget ->
              Resolved(Target.parse("//third_party:kotlinx-coroutines-core-jvm"))
            else -> {
              bazelClient.interpretThirdPartyWrappedDependency(
                rawTarget, expectedThirdPartyPrefix = "//third_party"
              )
            }
          }
        }
      }

      // The first wrapper library which depends on the proto should be the one exporting it (and
      // there should be just one).
      private fun BazelClient.interpretProtoTarget(rawTarget: String): InterpretedTarget {
        return resolveTarget(rawTarget) {
          query(
            "kind(java_lite_proto_library,allrdeps($rawTarget,1))", withSkyQuery = true
          ).single()
        }
      }

      private fun BazelClient.interpretAarTarget(
        rawTarget: String, parsedTarget: Target?
      ): InterpretedTarget {
        val mavenType =
          rawTarget.substringBefore("/_aar/").substringAfterLast('/', missingDelimiterValue = "")
        val targetName =
          rawTarget.substringAfter("/_aar/", missingDelimiterValue = "").substringBefore('/')
        return interpretMavenTarget(rawTarget = "@$mavenType//:$targetName", parsedTarget)
      }

      private fun BazelClient.interpretMavenTarget(
        rawTarget: String, parsedTarget: Target?
      ): InterpretedTarget {
        return interpretThirdPartyWrappedDependency(
          rawTarget,
          expectedThirdPartyPrefix = mavenThirdPartyPrefixMapping.getValue(parsedTarget?.workspace)
        )
      }

      private fun BazelClient.interpretThirdPartyWrappedDependency(
        rawTarget: String, expectedThirdPartyPrefix: String
      ): InterpretedTarget {
        return resolveTarget(rawTarget) {
          val queryResult =
            query("somepath($expectedThirdPartyPrefix/..., $rawTarget)", withSkyQuery = false)
          checkNotNull(queryResult.firstOrNull()) {
            "Failed to find third-party wrapper for Maven target: $rawTarget."
          }.also {
            require(it.startsWith(expectedThirdPartyPrefix)) {
              "Expected resolved target $it (for $rawTarget) to start with" +
                " $expectedThirdPartyPrefix."
            }
          }
        }
      }

      private fun resolveTarget(rawTarget: String, query: () -> String?): InterpretedTarget =
        query()?.parseNormalizedTarget()?.let(::Resolved) ?: Unknown(rawTarget)
    }
  }

  private data class Target(
    val workspace: Workspace, val pkg: Package, val name: String
  ): Comparable<Target> {
    val simpleQualifiedTargetPath: String by lazy {
      "${workspace.simpleName}//${pkg.path}$simpleLocalName"
    }
    val fullyQualifiedTargetPath: String by lazy { "${workspace.fullName}//${pkg.path}$localName" }

    private val localName: String get() = ":$name"
    private val simpleLocalName: String get() = if (name != pkg.name) localName else ""

    override fun compareTo(other: Target): Int = TARGET_COMPARATOR.compare(this, other)

    fun retarget(transform: (String) -> String): Target = copy(name = transform(name))

    fun computeRelativePath(referencingPackage: Package): String =
      if (referencingPackage == pkg) localName else simpleQualifiedTargetPath

    fun normalize(): Target = retarget { it.replace("Test_lib", "Test").removeSuffix("_kt") }

    companion object {
      private const val VALID_FILENAME_CHARS = "./${Package.VALID_COMPONENT_CHARS}"
      private const val VALID_FULLY_QUALIFIED_TARGET_CHARS = "@:$VALID_FILENAME_CHARS"
      private val TARGET_COMPARATOR =
        compareBy(Target::workspace).thenBy(Target::pkg).thenBy(Target::simpleLocalName)

      fun parse(rawTarget: String): Target {
        // Basic target validation for initial parsing.
        require(rawTarget.all { it in VALID_FULLY_QUALIFIED_TARGET_CHARS }) {
          "Target contains invalid characters: `$rawTarget`."
        }
        require("//" in rawTarget) { "Target must be fully qualified: `$rawTarget`." }

        // Extract the target's prefix (for repository locating).
        val prefix = rawTarget.substringBefore("//")
        val workspace = requireNotNull(Workspace.parse(prefix)) {
          "Target has invalid prefix (expected one of: '//', '@//', or '@<name>//'): `$rawTarget`."
        }

        val localTarget = rawTarget.substringAfter("//", missingDelimiterValue = "")
        val path = localTarget.substringBeforeLast(':')

        val pkg = requireNotNull(Package.parse(path)) {
          "Target contains an invalid path component (cannot be empty or have non-word" +
            " characters except for '_' and '-'): `$rawTarget`."
        }

        val targetName = requireNotNull(parseTargetName(localTarget, pkg)) {
          "Target contains an invalid target name (must only have word characters, '_', '-', or" +
            " '.'): `$rawTarget`."
        }
        return Target(workspace, pkg, targetName)
      }

      fun parseInterpretedReference(
        targetReference: String, workspace: Workspace, pkg: Package
      ): Target {
        return if (targetReference.startsWith(':')) {
          parse("${workspace.simpleName}//${pkg.path}$targetReference")
        } else parse(targetReference)
      }

      fun parseNonStrict(rawTarget: String): Target? {
        return Workspace.parse(rawTarget.substringBefore("//"))?.let { workspace ->
          val localTarget = rawTarget.substringAfter("//", missingDelimiterValue = "")
          Package.parse(localTarget.substringBeforeLast(':'))?.let { pkg ->
            parseTargetName(localTarget, pkg)?.let { targetName ->
              Target(workspace, pkg, targetName)
            }
          }
        }
      }

      private fun parseTargetName(localTarget: String, pkg: Package): String? {
        val maybeTargetName =
          localTarget.substringAfterLast(':', missingDelimiterValue = "").takeIf(String::isNotEmpty)
        return (maybeTargetName ?: pkg.name).takeIf { targetName ->
          targetName.isNotEmpty() && targetName.all { it in VALID_FILENAME_CHARS }
        }
      }
    }
  }

  private data class Package(val parent: Package? = null, val name: String): Comparable<Package> {
    val path: String by lazy { parent?.path?.let { "$it/$name" } ?: name }

    override fun compareTo(other: Package): Int = PACKAGE_COMPARATOR.compare(this, other)

    companion object {
      const val VALID_COMPONENT_CHARS =
        "_-abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

      private val PACKAGE_COMPARATOR = compareBy(Package::path)

      fun parse(path: String): Package? {
        return path.split('/').map { component ->
          if (component.any { it !in VALID_COMPONENT_CHARS }) return null
          Package(name = component)
        }.reduce { parent, child -> child.copy(parent = parent) }
      }
    }
  }

  private sealed class Workspace: Comparable<Workspace> {
    abstract val fullName: String
    abstract val simpleName: String
    protected abstract val intrinsicOrder: Int

    override fun compareTo(other: Workspace): Int = WORKSPACE_COMPARATOR.compare(this, other)

    object Main: Workspace() {
      override val fullName = "@"
      override val simpleName = ""
      override val intrinsicOrder get() = 0 // Main dependencies always go first.
    }

    data class Remote(val name: String): Workspace() {
      override val fullName = "@$name"
      override val simpleName = fullName
      override val intrinsicOrder get() = 1 // External dependencies always go second.
    }

   companion object {
     private val WORKSPACE_COMPARATOR =
       compareBy(Workspace::intrinsicOrder).thenBy(Workspace::simpleName)

     fun parse(prefix: String): Workspace? {
       val workspaceName = prefix.substringAfter('@', missingDelimiterValue = "")
       return when {
         prefix.isEmpty() -> Main
         !prefix.startsWith('@') -> null
         workspaceName.isEmpty() -> Main // prefix is just '@'.
         workspaceName.any { it !in Package.VALID_COMPONENT_CHARS } -> null // Invalid chars.
         else -> Remote(workspaceName)
       }
     }
   }
  }

  private data class LineFragment(val linePart: String, val indentation: Int = 0)

  private sealed class PropertyValue {
    abstract val canBeRegenerated: Boolean

    abstract fun generate(indentation: Int): List<LineFragment>

    object RuntimeEvaluated: PropertyValue() {
      override val canBeRegenerated = false

      override fun generate(indentation: Int) =
        error("Generation is unsupported for runtime-evaluated properties.")
    }

    data class StringValue(val value: String): PropertyValue() {
      override val canBeRegenerated = true

      override fun generate(indentation: Int) =
        listOf(LineFragment(linePart = "\"$value\"", indentation))
    }

    data class BooleanValue(val value: Boolean): PropertyValue() {
      override val canBeRegenerated = true

      override fun generate(indentation: Int) =
        listOf(LineFragment(linePart = if (value) "True" else "False", indentation))
    }

    data class IntValue(val value: Int): PropertyValue() {
      override val canBeRegenerated = true

      override fun generate(indentation: Int) =
        listOf(LineFragment(linePart = value.toString(), indentation))
    }

    data class ListValue(val values: List<PropertyValue>): PropertyValue() {
      override val canBeRegenerated = values.all { it.canBeRegenerated }

      override fun generate(indentation: Int): List<LineFragment> {
        return formatMultiLineStructure(
          values,
          indentation,
          openPrefix = "[",
          closePrefix = "]",
          formatElement = PropertyValue::generate
        )
      }
    }

    data class DictionaryValue(val values: Map<String, PropertyValue>): PropertyValue() {
      override val canBeRegenerated = values.values.all { it.canBeRegenerated }

      override fun generate(indentation: Int): List<LineFragment> {
        return formatMultiLineStructure(
          values.toList(),
          indentation,
          openPrefix = "{",
          closePrefix = "}",
          formatElement = { (name, value), indent ->
            value.generate(indent).transformFirst {
              it.copy(linePart = "\"$name\": ${it.linePart}")
            }
          }
        )
      }
    }

    data class SimpleGlob(val patterns: ListValue): PropertyValue() {
      override val canBeRegenerated = patterns.canBeRegenerated

      override fun generate(indentation: Int): List<LineFragment> {
        return formatMultiLineStructure(
          patterns.values,
          indentation,
          openPrefix = "glob([",
          closePrefix = "])",
          formatElement = PropertyValue::generate
        )
      }
    }

    companion object {
      private fun <T> formatMultiLineStructure(
        elements: List<T>,
        indentation: Int,
        openPrefix: String,
        closePrefix: String,
        formatElement: (T, Int) -> List<LineFragment>
      ): List<LineFragment> {
        val openPrefixFragment = LineFragment(linePart = openPrefix, indentation)
        val lines = listOf(openPrefixFragment) + elements.flatMap { elem ->
          // Add comma to the last element of the (potentially nested) element.
          formatElement(elem, indentation + 1).transformLast {
            it.copy(linePart = "${it.linePart},")
          }
        } + LineFragment(linePart = closePrefix, indentation)
        return if (lines.size == 3) {
          // If there are exactly three lines for a multi-line structure, it can be condensed to a
          // single line.
          val (openLine, element, closeLine) = lines
          val openLineContent = openLine.linePart
          val elemContent = element.linePart.removeSuffix(",")
          val closeLineContent = closeLine.linePart
          val condensedLine =
            LineFragment(linePart = "$openLineContent$elemContent$closeLineContent", indentation)
          return listOf(condensedLine)
        } else lines
      }
    }
  }

  private data class ParsedTarget(
    val workspace: Workspace,
    val pkg: Package,
    val kind: String,
    val properties: Map<String, PropertyValue>,
    val sourceLineIndexRange: IntRange
  ) {
    val name: String by lazy { getExpectedProperty<PropertyValue.StringValue>("name").value }
    val deps: Set<Target> by lazy {
      extractDeps()?.mapToSet { Target.parseInterpretedReference(it, workspace, pkg) } ?: emptySet()
    }
    val canBeRegenerated: Boolean by lazy { properties.values.all { it.canBeRegenerated } }

    fun generateLines(transform: (Set<Target>) -> Set<Target> = { it }): List<String> {
      val properties = this.properties.toMutableMap()
      val updatedDeps = transform(deps).map { target ->
        PropertyValue.StringValue(target.computeRelativePath(pkg))
      }
      properties.replace("deps", PropertyValue.ListValue(updatedDeps))
      val lines = listOf(LineFragment(linePart = "$kind(")) + properties.flatMap { (name, prop) ->
        prop.generate(indentation = 1).transformFirst {
          it.copy(linePart = "$name = ${it.linePart}")
        }.transformLast { it.copy(linePart = "${it.linePart},") }
      } + LineFragment(linePart = ")")
      return lines.generateLines()
    }

    private fun extractDeps(): List<String>? {
      return getProperty<PropertyValue.ListValue>("deps")
        ?.extractVals<PropertyValue.StringValue>("deps")
        ?.map(PropertyValue.StringValue::value)
    }

    private inline fun <reified T: PropertyValue> getProperty(name: String) = properties[name] as? T

    private inline fun <reified T: PropertyValue> getExpectedProperty(name: String): T {
      return checkNotNull(getProperty(name)) {
        "Expected property with name $name and type ${T::class.java}."
      }
    }

    private inline fun <reified T: PropertyValue> PropertyValue.ListValue.extractVals(
      name: String
    ): List<T> {
      return values.filterIsInstance<T>().also {
        check(it.size == values.size) {
          "Expected all values in list property $name to be of type ${T::class.java}."
        }
      }
    }
  }

  private class BuildFile(val file: File, val parsedTargets: List<ParsedTarget>) {
    fun findTarget(name: String): ParsedTarget? = parsedTargets.find { it.name == name }

    fun regenerateTarget(name: String, transform: (Set<Target>) -> Set<Target> = { it }) {
      val tempOutput = File.createTempFile(/* prefix = */ "BUILD-", /* suffix = */ ".bazel")
      val target = findTarget(name) ?: error("Cannot find target for name: $name.")
      tempOutput.outputStream().bufferedWriter().use { writer ->
        file.inputStream().bufferedReader().use { reader ->
          reader.lineSequence().replaceRange(target.sourceLineIndexRange) {
            target.generateLines(transform)
          }.forEach(writer::appendLine)
        }
      }
      tempOutput.copyTo(file, overwrite = true)
    }
  }

  private class PeekableSequence<T: Any> private constructor(
    private val iterator: PeekableIterator<T>
  ): Sequence<T> {
    val currentValue: T? get() = iterator.peek()
    val currentCount: Int get() = iterator.getRetrievalCount()

    override fun iterator(): Iterator<T> = iterator

    fun skip() = iterator.next()

    fun skipWhile(predicate: (T) -> Boolean) {
      var peekedValue = currentValue
      while (peekedValue != null && predicate(peekedValue)) {
        skip()
        peekedValue = currentValue
      }
    }

    fun takeWhileLazy(predicate: (T) -> Boolean): Sequence<T> {
      return sequence {
        var peekedValue = currentValue
        while (peekedValue != null && predicate(peekedValue)) {
          val peekedCount = currentCount
          yield(peekedValue)
          // Only skip the current value if it wasn't already skipped while being yielded.
          if (currentCount == peekedCount) skip()
          peekedValue = currentValue
        }
      }
    }

    companion object {
      fun <T: Any> Sequence<T>.toPeekable(): PeekableSequence<T> =
        if (this is PeekableSequence) this else PeekableSequence(toPeekableIterator())
    }
  }

  private companion object {
    private const val CONSOLE_LINE_LIMIT = 80

    private fun printTargetLine(prefix: String, target: Target, suffix: String) {
      val rawTarget = target.simpleQualifiedTargetPath
      val unshortenedString = "$prefix$rawTarget$suffix"
      val unshortenedLength = unshortenedString.length
      val targetWithoutStart = rawTarget.substringAfter("//", missingDelimiterValue = "")

      if (unshortenedLength <= CONSOLE_LINE_LIMIT) {
        // No shortening needed.
        println(unshortenedString)
        return
      }

      if ("/" !in targetWithoutStart || !rawTarget.startsWith("//") || ":" !in rawTarget) {
        println(unshortenedString)
        return
      }

      val firstColon = rawTarget.indexOf(':')
      val firstSlash = rawTarget.substringAfter("//", missingDelimiterValue = "").indexOf('/') + 2
      val lastColon = rawTarget.indexOfLast { it == ':' }
      val lastSlash = rawTarget.indexOfLast { it == '/' }
      val initialDelimiter = firstColon.coerceAtMost(firstSlash)
      val finalDelimiter = lastColon.coerceAtLeast(lastSlash)
      val requiredTargetPrefix = rawTarget.substring(0 .. initialDelimiter)
      val targetMiddle = rawTarget.substring(initialDelimiter + 1 until finalDelimiter)
      val requiredTargetSuffix = rawTarget.substring(finalDelimiter)
      if (requiredTargetPrefix.isEmpty() || requiredTargetSuffix.isEmpty()) {
        println(unshortenedString)
        return
      }

      // The '+3' corresponds to an ellipsis for shortening.
      val minLength = prefix.length + suffix.length + requiredTargetPrefix.length +
        requiredTargetSuffix.length + 3
      if (minLength >= unshortenedLength) {
        // Something weird is going on with the target string.
        println(unshortenedString)
        return
      }

      // minLength may be larger than console line limit, so just ignore shortening if that happens.
      val maxCharactersToAdd = (CONSOLE_LINE_LIMIT - minLength).coerceAtLeast(0)
      val adjustedTargetMiddle = targetMiddle.take(maxCharactersToAdd)
      val adjustedTarget = "$requiredTargetPrefix$adjustedTargetMiddle...$requiredTargetSuffix"
      println("$prefix$adjustedTarget$suffix")
    }

    private fun <I, O> Iterable<I>.mapToSet(transform: (I) -> O): Set<O> =
      mapTo(mutableSetOf(), transform)

    private fun <I, O> Sequence<I>.mapToSet(transform: (I) -> O): Set<O> =
      mapTo(mutableSetOf(), transform)

    private fun <T> Sequence<T>.dropUntil(predicate: (T) -> Boolean): Sequence<T> {
      var finished = false
      return mapNotNull {
        when {
          finished -> it
          predicate(it) -> null.also { finished = true }
          else -> null
        }
      }
    }

    private fun <T> Sequence<T>.takeUntil(predicate: (T) -> Boolean): Sequence<T> {
      var finished = false
      return mapNotNull {
        when {
          finished -> null
          predicate(it) -> null.also { finished = true }
          else -> it
        }
      }
    }

    private fun <T> Sequence<T>.replaceRange(
      range: IntRange, createReplacement: () -> Iterable<T>
    ): Sequence<T> {
      var addedReplacement = false
      return withIndex().flatMap { (index, line) ->
        when {
          index !in range -> listOf(line) // Line before/after replacement.
          !addedReplacement -> createReplacement().also { addedReplacement = true }
          else -> emptyList() // Drop the old line.
        }
      }
    }

    private fun String.parseNormalizedTarget(): Target = Target.parse(this).normalize()

    private fun <T> List<T>.transformFirst(transform: (T) -> T): List<T> =
      listOf(transform(first())) + drop(1)

    private fun <T> List<T>.transformLast(transform: (T) -> T): List<T> =
      dropLast(1) + transform(last())

    private fun List<LineFragment>.generateLines(): List<String> =
      map { "${it.indentation.generateIndentation()}${it.linePart}" }

    private fun Int.generateIndentation(): String =
      " ".repeat(this * 4) // 4 spaces per indent in Starlark.
  }

  // Subset of the actual language available tokens (just need enough to parse BUILD files).
  private sealed class StarlarkToken {
    abstract val location: Location

    data class LineComment(val contents: String, override val location: Location): StarlarkToken()

    data class DocString(val contents: String, override val location: Location): StarlarkToken()
    data class Identifier(val value: String, override val location: Location): StarlarkToken()
    data class SimpleString(val value: String, override val location: Location): StarlarkToken()
    data class Integer(val value: Int, override val location: Location): StarlarkToken()
    data class Bool(val value: Boolean, override val location: Location): StarlarkToken()

    data class If(override val location: Location): StarlarkToken()
    data class Else(override val location: Location): StarlarkToken()
    data class For(override val location: Location): StarlarkToken()
    data class In(override val location: Location): StarlarkToken()

    data class Comma(override val location: Location): StarlarkToken()
    data class Equals(override val location: Location): StarlarkToken()
    data class Colon(override val location: Location): StarlarkToken()
    data class Period(override val location: Location): StarlarkToken()
    data class Plus(override val location: Location): StarlarkToken()
    data class Minus(override val location: Location): StarlarkToken()
    data class Modulo(override val location: Location): StarlarkToken()
    data class LeftParenthesis(override val location: Location): StarlarkToken()
    data class RightParenthesis(override val location: Location): StarlarkToken()
    data class LeftSquareBrace(override val location: Location): StarlarkToken()
    data class RightSquareBrace(override val location: Location): StarlarkToken()
    data class LeftCurlyBrace(override val location: Location): StarlarkToken()
    data class RightCurlyBrace(override val location: Location): StarlarkToken()

    data class Location(
      val filePath: String, val sourceLineIndexes: IntRange, val lineCharacterIndexes: IntRange
    ) {
      fun computeErrorMessagePrefix(): String {
        val linePart = if (sourceLineIndexes.first == sourceLineIndexes.last) {
          "${sourceLineIndexes.first + 1}"
        } else "${sourceLineIndexes.first + 1}-${sourceLineIndexes.last + 1}"
        val colPart = if (lineCharacterIndexes.first == lineCharacterIndexes.last) {
          "${lineCharacterIndexes.first + 1}"
        } else "${lineCharacterIndexes.first + 1}-${lineCharacterIndexes.last + 1}"
        return "$filePath:$linePart:$colPart"
      }
    }
  }

  private object StarlarkTokenizer {
    private const val NEWLINE = '\n'

    fun tokenize(filePath: String, lines: Sequence<String>): Sequence<StarlarkToken> {
      val charSequence = lines.flatMap { line -> line.asSequence() + sequenceOf(NEWLINE) }
      return TrackedPeekableSequence(charSequence.toPeekable(), filePath).tokenize()
    }

    private fun TrackedPeekableSequence.tokenize(): Sequence<StarlarkToken> {
      return sequence {
        while (base.currentValue != null) {
          val nextToken = when (val nextChar = base.currentValue) {
            '"' -> {
              base.skip() // Skip the string's opening quotes.

              // Either a single-line or multi-line string. First, assume a single-line string.
              val stringToken = createToken(StarlarkToken::SimpleString) {
                base.takeWhileLazy { it != NEWLINE && it != '"' }.concatToString().also {
                  if (base.currentValue == '"') {
                    base.skip() // Skip the string's closing quotes.
                  } else {
                    val line = currentLineIndex + 1
                    val col = currentCharacterColumn + 1
                    error("$filePath:$line:$col: Improperly terminated single-line string.")
                  }
                }
              }
              if (stringToken.value.isEmpty() && base.currentValue == '"') {
                // An empty string followed by a single quote would create a triple quote, that is,
                // the start of a multi-line comment.
                base.skip() // Skip the quotes beginning the multi-line comment.
                createToken(StarlarkToken::DocString) {
                  var encounteredSequentialQuotes = 0
                  base.takeWhileLazy {
                    when {
                      it == '\n' -> true.also { encounteredSequentialQuotes = 0; advanceLine() }
                      it != '"' -> true.also { encounteredSequentialQuotes = 0 }
                      encounteredSequentialQuotes < 2 -> true.also { encounteredSequentialQuotes++ }
                      base.currentValue == '"' -> false
                      else -> true // The next character isn't quotes; the counter will be reset.
                    }
                  }.concatToString().also {
                    if (base.currentValue == '"') {
                      base.skip() // Skip the string's final closing quotes.
                    } else {
                      val line = currentLineIndex + 1
                      val col = currentCharacterColumn + 1
                      error("$filePath:$line:$col: Improperly terminated multi-line string.")
                    }
                  }
                }.let { token ->
                  // The final two closing quotes will get included in the comments' contents due to
                  // the LL1) nature of the tokenizer, so they need to be removed and the tracking
                  // location updated.
                  val firstCol = token.location.lineCharacterIndexes.first
                  val lastCol = token.location.lineCharacterIndexes.last
                  token.copy(
                    contents = token.contents.removeSuffix("\"\""),
                    location = token.location.copy(lineCharacterIndexes = firstCol .. (lastCol - 2))
                  )
                }
              } else stringToken
            }
            '#' -> {
              // A line comment. Ignore all other characters until a newline is reached.
              createToken(StarlarkToken::LineComment) {
                base.takeWhileLazy { it != NEWLINE }.concatToString()
              }
            }
            NEWLINE -> {
              // Skip newlines, but keep track of them for error diagnostics.
              base.skip()
              advanceLine()
              null
            }
            ' ', '\t' -> {
              // Outright ignore horizontal whitespace characters.
              base.skipWhile { it != NEWLINE && it.isWhitespace() }
              null
            }
            ',' -> createOperator(StarlarkToken::Comma)
            '=' -> createOperator(StarlarkToken::Equals)
            ':' -> createOperator(StarlarkToken::Colon)
            '.' -> createOperator(StarlarkToken::Period)
            '+' -> createOperator(StarlarkToken::Plus)
            '-' -> createOperator(StarlarkToken::Minus)
            '%' -> createOperator(StarlarkToken::Modulo)
            '(' -> createOperator(StarlarkToken::LeftParenthesis)
            ')' -> createOperator(StarlarkToken::RightParenthesis)
            '[' -> createOperator(StarlarkToken::LeftSquareBrace)
            ']' -> createOperator(StarlarkToken::RightSquareBrace)
            '{' -> createOperator(StarlarkToken::LeftCurlyBrace)
            '}' -> createOperator(StarlarkToken::RightCurlyBrace)
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
              createToken(StarlarkToken::Integer) {
                base.takeWhileLazy(Char::isDigit).concatToInt()
              }
            }
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
            'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
            'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y',
            'Z', '_' -> {
              val identifier = createToken(StarlarkToken::Identifier) {
                base.takeWhileLazy(::isIdentifierChar).concatToString()
              }
              // Parsing non-identifiers could be a bit more efficient by avoiding the identifier
              // creation, but this is a simpler implementation.
              when (identifier.value) {
                "True" -> StarlarkToken.Bool(value = true, location = identifier.location)
                "False" -> StarlarkToken.Bool(value = false, location = identifier.location)
                "if" -> StarlarkToken.If(location = identifier.location)
                "else" -> StarlarkToken.Else(location = identifier.location)
                "for" -> StarlarkToken.For(location = identifier.location)
                "in" -> StarlarkToken.In(location = identifier.location)
                else -> identifier
              }
            }
            else -> {
              val line = currentLineIndex + 1
              val col = currentCharacterColumn + 1
              error("$filePath:$line:$col: Invalid character encountered: '$nextChar'.")
            }
          } ?: continue
          yield(nextToken)
        }
      }
    }

    private fun TrackedPeekableSequence.createOperator(
      constructToken: (StarlarkToken.Location) -> StarlarkToken
    ): StarlarkToken {
      return createToken(
        constructToken = { _, location -> constructToken(location) },
        consumeStream = { base.skip() }
      )
    }

    private fun <T: StarlarkToken, V> TrackedPeekableSequence.createToken(
      constructToken: (V, StarlarkToken.Location) -> T,
      consumeStream: TrackedPeekableSequence.() -> V
    ): T {
      val startLine = currentLineIndex
      val startColumn = currentCharacterColumn
      val tokenValue = consumeStream()
      val endLine = currentLineIndex
      val endColumn = currentCharacterColumn
      val location = StarlarkToken.Location(
        filePath,
        sourceLineIndexes = startLine..endLine,
        lineCharacterIndexes = startColumn..endColumn
      )
      return constructToken(tokenValue, location)
    }

    private fun Sequence<Char>.concatToString(): String = joinToString(separator = "")

    private fun Sequence<Char>.concatToInt(): Int = concatToString().toInt()

    private fun isIdentifierChar(char: Char): Boolean = char.isLetterOrDigit() || char == '_'

    private class TrackedPeekableSequence(
      val base: PeekableSequence<Char>, val filePath: String
    ): Sequence<Char> {
      var currentLineIndex: Int = 0
      var lineStartCharacterIndex: Int = 0
      val currentCharacterIndex: Int get() = base.currentCount
      val currentCharacterColumn: Int get() = currentCharacterIndex - lineStartCharacterIndex

      override fun iterator() = base.iterator()

      fun advanceLine() {
        currentLineIndex++
        lineStartCharacterIndex = currentCharacterIndex
      }
    }
  }

  private sealed class StarlarkNode {
    abstract val sourceLineIndexRange: IntRange

    sealed class Literal: StarlarkNode() {
      data class StarlarkString(
        val value: String, override val sourceLineIndexRange: IntRange
      ): Literal()
      data class Bool(val value: Boolean, override val sourceLineIndexRange: IntRange): Literal()
      data class Integer(val value: Int, override val sourceLineIndexRange: IntRange): Literal()
    }

    sealed class Expression: StarlarkNode() {
      data class LiteralExpression(val literal: Literal): Expression() {
        override val sourceLineIndexRange get() = literal.sourceLineIndexRange
      }

      data class FunctionCall(
        val container: Expression,
        val positionalArguments: List<Expression>,
        val namedArguments: Map<String, Expression>,
        override val sourceLineIndexRange: IntRange
      ): Expression()

      data class VariableReference(
        val name: String, override val sourceLineIndexRange: IntRange
      ): Expression()

      data class MemberReference(val container: Expression, val access: Expression): Expression() {
        override val sourceLineIndexRange: IntRange
          get() = container.sourceLineIndexRange.union(access.sourceLineIndexRange)
      }

      sealed class Indexing: Expression() {
        abstract val container: Expression

        data class LowerBounded(
          override val container: Expression,
          val bounds: Expression,
          override val sourceLineIndexRange: IntRange
        ): Indexing()

        data class UpperBounded(
          override val container: Expression,
          val bounds: Expression,
          override val sourceLineIndexRange: IntRange
        ): Indexing()

        data class DualBounded(
          override val container: Expression,
          val lowerBounds: Expression,
          val upperBounds: Expression,
          override val sourceLineIndexRange: IntRange
        ): Indexing()
      }

      data class Conditional(
        val condition: Expression, val resultWhenTrue: Expression, val resultWhenFalse: Expression
      ): Expression() {
        override val sourceLineIndexRange: IntRange get() =
          listOf(condition, resultWhenTrue, resultWhenFalse).computeManifoldSourceLineRange()
      }

      data class Loop(val repeated: Expression, val condition: Contains): Expression() {
        override val sourceLineIndexRange: IntRange
          get() = listOf(repeated, condition).computeManifoldSourceLineRange()
      }

      data class Contains(val subject: Expression, val container: Expression): Expression() {
        override val sourceLineIndexRange: IntRange
          get() = listOf(subject, container).computeManifoldSourceLineRange()
      }

      sealed class BinaryOperation: Expression() {
        abstract val lhs: Expression
        abstract val rhs: Expression

        override val sourceLineIndexRange get() = listOf(lhs, rhs).computeManifoldSourceLineRange()

        data class Addition(
          override val lhs: Expression, override val rhs: Expression
        ): BinaryOperation()
        data class Subtraction(
          override val lhs: Expression, override val rhs: Expression
        ): BinaryOperation()
        data class Modulus(
          override val lhs: Expression, override val rhs: Expression
        ): BinaryOperation()
      }

      data class StarlarkList(
        val elements: List<Expression>, override val sourceLineIndexRange: IntRange
      ): Expression()

      data class Dict(
        val elements: Map<String, Expression>, override val sourceLineIndexRange: IntRange
      ): Expression()

      // Note that this can just be a single wrapped expression rather than a tuple (for the sake of
      // ensuring correct source line ranges).
      data class Tuple(
        val elements: List<Expression>, override val sourceLineIndexRange: IntRange
      ): Expression()
    }

    sealed class Statement: StarlarkNode() {
      data class ExpressionStatement(val expression: Expression): Statement() {
        override val sourceLineIndexRange: IntRange get() = expression.sourceLineIndexRange
      }

      data class Definition(
        val name: Expression.VariableReference, val result: Expression
      ): Statement() {
        override val sourceLineIndexRange: IntRange
          get() = listOf(name, result).computeManifoldSourceLineRange()
      }
    }

    private companion object {
      private fun IntRange.union(other: IntRange): IntRange =
        first.coerceAtMost(other.first)..last.coerceAtLeast(other.last)

      private fun Iterable<IntRange>.union(): IntRange = reduce { acc, range -> acc.union(range) }

      private fun Iterable<StarlarkNode>.computeManifoldSourceLineRange(): IntRange =
        map { it.sourceLineIndexRange }.union()
    }
  }

  private object StarlarkParser {
    fun parse(filePath: String, lines: Sequence<String>): Sequence<StarlarkNode> =
      StarlarkTokenizer.tokenize(filePath, lines).toPeekable().parseFile()

    private fun PeekableSequence<StarlarkToken>.parseFile(): Sequence<StarlarkNode.Statement> {
      return sequence {
        while (seekNextToken() != null) {
          yield(parseStatement())
        }
      }
    }

    private fun PeekableSequence<StarlarkToken>.seekNextToken(): StarlarkToken? {
      skipWhile { it is StarlarkToken.LineComment }
      return currentValue
    }

    private inline fun <reified T: StarlarkToken> PeekableSequence<StarlarkToken>.dropToken(): T {
      val droppedToken = skip()
      if (droppedToken !is T) {
        failOnToken(droppedToken, expected = "token of type ${T::class.java.name}")
      }
      return droppedToken
    }

    private fun PeekableSequence<StarlarkToken>.parseStatement(): StarlarkNode.Statement {
      return when (val nextToken = seekNextToken()) {
        is StarlarkToken.Bool, is StarlarkToken.DocString, is StarlarkToken.Integer,
        is StarlarkToken.SimpleString, is StarlarkToken.LeftCurlyBrace,
        is StarlarkToken.LeftSquareBrace ->
          StarlarkNode.Statement.ExpressionStatement(parseExpression())
        is StarlarkToken.Identifier -> {
          val baseExpression = parseExpression()
          // Check if the top-level identifier is actually an assignment.
          if (baseExpression is StarlarkNode.Expression.VariableReference
            && seekNextToken() is StarlarkToken.Equals) {
            dropToken<StarlarkToken.Equals>()
            val result = parseExpression()
            StarlarkNode.Statement.Definition(baseExpression, result)
          } else StarlarkNode.Statement.ExpressionStatement(baseExpression)
        }
        is StarlarkToken.Comma, is StarlarkToken.Equals, is StarlarkToken.Colon,
        is StarlarkToken.Period, is StarlarkToken.Plus, is StarlarkToken.Minus,
        is StarlarkToken.Modulo, is StarlarkToken.LeftParenthesis,
        is StarlarkToken.RightCurlyBrace, is StarlarkToken.LineComment,
        is StarlarkToken.RightParenthesis, is StarlarkToken.RightSquareBrace, is StarlarkToken.Else,
        is StarlarkToken.For, is StarlarkToken.If, is StarlarkToken.In, null ->
          failOnToken(nextToken, expected = "an expression")
      }
    }

    private fun PeekableSequence<StarlarkToken>.parseExpression(): StarlarkNode.Expression {
      var baseExpression = when (val nextToken = seekNextToken()) {
        is StarlarkToken.Bool, is StarlarkToken.DocString, is StarlarkToken.Integer,
        is StarlarkToken.SimpleString -> StarlarkNode.Expression.LiteralExpression(parseLiteral())
        is StarlarkToken.Identifier -> {
          val id = dropToken<StarlarkToken.Identifier>()
          StarlarkNode.Expression.VariableReference(id.value, id.location.sourceLineIndexes)
        }
        is StarlarkToken.LeftCurlyBrace -> parseDict()
        is StarlarkToken.LeftSquareBrace -> parseList()
        is StarlarkToken.LeftParenthesis -> parseTuple()
        is StarlarkToken.Comma, is StarlarkToken.Equals, is StarlarkToken.Colon,
        is StarlarkToken.Period, is StarlarkToken.Plus, is StarlarkToken.Minus,
        is StarlarkToken.Modulo, is StarlarkToken.LineComment, is StarlarkToken.RightCurlyBrace,
        is StarlarkToken.RightParenthesis, is StarlarkToken.RightSquareBrace, is StarlarkToken.Else,
        is StarlarkToken.For, is StarlarkToken.If, is StarlarkToken.In, null ->
          failOnToken(nextToken, expected = "a literal, list, dict, variable, or function call")
      }

      // Expressions may be part of comprehensions or nested checks, depending on what follows a
      // completed expression. These can be chained together.
      var chainOperatorToken = seekNextToken()
      while (chainOperatorToken != null) {
        // The chaining token must be on the same line as the end of the previous expression in
        // order to actually 'chain' it.
        val tokenStartLine = chainOperatorToken.location.sourceLineIndexes.first
        if (tokenStartLine !in baseExpression.sourceLineIndexRange) break

        baseExpression = when (chainOperatorToken) {
          is StarlarkToken.If -> parseIfComprehension(baseExpression)
          is StarlarkToken.For -> parseForInComprehension(baseExpression)
          is StarlarkToken.In -> parseIn(baseExpression)
          is StarlarkToken.Plus -> {
            parseBinOp<StarlarkToken.Plus>(
              baseExpression, StarlarkNode.Expression.BinaryOperation::Addition
            )
          }
          is StarlarkToken.Minus -> {
            parseBinOp<StarlarkToken.Minus>(
              baseExpression, StarlarkNode.Expression.BinaryOperation::Subtraction
            )
          }
          is StarlarkToken.Modulo -> {
            parseBinOp<StarlarkToken.Modulo>(
              baseExpression, StarlarkNode.Expression.BinaryOperation::Modulus
            )
          }
          is StarlarkToken.LeftParenthesis -> parseFunctionCall(baseExpression)
          is StarlarkToken.Period -> parseMemberReference(baseExpression)
          is StarlarkToken.LeftSquareBrace -> parseIndexing(baseExpression)
          else -> break // In all other cases just yield the expression built to this point.
        }
        chainOperatorToken = seekNextToken()
      }
      return baseExpression
    }

    private fun PeekableSequence<StarlarkToken>.parseFunctionCall(
      container: StarlarkNode.Expression
    ): StarlarkNode.Expression.FunctionCall {
      val openParen = dropToken<StarlarkToken.LeftParenthesis>()
      val arguments = parseArguments()
      val closeParen = dropToken<StarlarkToken.RightParenthesis>()

      val positional = arguments.filterIsInstance<Argument.Positional>().map { it.expression }
      val named = arguments.filterIsInstance<Argument.Named>().associate {
        it.name to it.expression
      }
      val openLoc = openParen.location.sourceLineIndexes
      val closeLoc = closeParen.location.sourceLineIndexes
      return StarlarkNode.Expression.FunctionCall(
        container, positional, named, sourceLineIndexRange = openLoc.first..closeLoc.last
      )
    }

    private fun PeekableSequence<StarlarkToken>.parseArguments(): List<Argument> =
      parseRepeated<Argument, StarlarkToken.RightParenthesis> { parseArgument() }

    private fun PeekableSequence<StarlarkToken>.parseArgument(): Argument {
      return when (val nextToken = seekNextToken()) {
        is StarlarkToken.Bool, is StarlarkToken.DocString, is StarlarkToken.Integer,
        is StarlarkToken.SimpleString, is StarlarkToken.LeftCurlyBrace,
        is StarlarkToken.LeftSquareBrace -> Argument.Positional(parseExpression())
        is StarlarkToken.Identifier -> {
          val nameOrVariable = nextToken.value
          dropToken<StarlarkToken.Identifier>() // Remove the ID to check for a positional arg.
          if (seekNextToken() is StarlarkToken.Equals) {
            dropToken<StarlarkToken.Equals>()
            Argument.Named(nameOrVariable, parseExpression())
          } else {
            Argument.Positional(
              StarlarkNode.Expression.VariableReference(
                nameOrVariable, nextToken.location.sourceLineIndexes
              )
            )
          }
        }
        is StarlarkToken.Comma, is StarlarkToken.Equals, is StarlarkToken.Colon,
        is StarlarkToken.Period, is StarlarkToken.Plus, is StarlarkToken.Minus,
        is StarlarkToken.Modulo, is StarlarkToken.LeftParenthesis, is StarlarkToken.LineComment,
        is StarlarkToken.RightCurlyBrace, is StarlarkToken.RightParenthesis,
        is StarlarkToken.RightSquareBrace, is StarlarkToken.Else, is StarlarkToken.For,
        is StarlarkToken.If, is StarlarkToken.In, null ->
          failOnToken(nextToken, expected = "expression or named argument")
      }
    }

    private fun PeekableSequence<StarlarkToken>.parseIndexing(
      container: StarlarkNode.Expression
    ): StarlarkNode.Expression.Indexing {
      val openIndex = dropToken<StarlarkToken.LeftSquareBrace>()
      val (lowerBounds, upperBounds) = if (seekNextToken() is StarlarkToken.Colon) {
        // The resulting expression is only upper-bounded.
        dropToken<StarlarkToken.Colon>()
        null to parseExpression()
      } else {
        // Otherwise, the expression is at least lower-bounded.
        val lowerBounds = parseExpression()
        if (seekNextToken() is StarlarkToken.Colon) {
          // The expression is dual-bounded.
          dropToken<StarlarkToken.Colon>()
          lowerBounds to parseExpression()
        } else lowerBounds to null
      }
      val closeIndex = dropToken<StarlarkToken.RightSquareBrace>()
      val openLoc = openIndex.location.sourceLineIndexes
      val closeLoc = closeIndex.location.sourceLineIndexes
      val sourceLines = openLoc.first..closeLoc.last
      return when {
        lowerBounds != null && upperBounds != null -> {
          StarlarkNode.Expression.Indexing.DualBounded(
            container, lowerBounds, upperBounds, sourceLines
          )
        }
        lowerBounds != null ->
          StarlarkNode.Expression.Indexing.LowerBounded(container, lowerBounds, sourceLines)
        upperBounds != null ->
          StarlarkNode.Expression.Indexing.UpperBounded(container, upperBounds, sourceLines)
        else -> error("Something went wrong internally when parsing an indexing.")
      }
    }

    private fun PeekableSequence<StarlarkToken>.parseMemberReference(
      container: StarlarkNode.Expression
    ): StarlarkNode.Expression.MemberReference {
      dropToken<StarlarkToken.Period>()
      return StarlarkNode.Expression.MemberReference(container, access = parseExpression())
    }

    private fun PeekableSequence<StarlarkToken>.parseIfComprehension(
      resultIfTrue: StarlarkNode.Expression
    ): StarlarkNode.Expression.Conditional {
      dropToken<StarlarkToken.If>()
      val condition = parseExpression()
      dropToken<StarlarkToken.Else>()
      val resultIfFalse = parseExpression()
      return StarlarkNode.Expression.Conditional(condition, resultIfTrue, resultIfFalse)
    }

    private fun PeekableSequence<StarlarkToken>.parseForInComprehension(
      repeated: StarlarkNode.Expression
    ): StarlarkNode.Expression.Loop {
      val forToken = dropToken<StarlarkToken.For>()
      val contains = parseExpression() as? StarlarkNode.Expression.Contains
        ?: failOnToken(forToken, expected = "'in' expression after 'for'.")
      return StarlarkNode.Expression.Loop(repeated, contains)
    }

    private fun PeekableSequence<StarlarkToken>.parseIn(
      subject: StarlarkNode.Expression
    ): StarlarkNode.Expression.Contains {
      dropToken<StarlarkToken.In>()
      val container = parseExpression()
      return StarlarkNode.Expression.Contains(subject, container)
    }

    private inline fun <reified T: StarlarkToken> PeekableSequence<StarlarkToken>.parseBinOp(
      leftSide: StarlarkNode.Expression,
      createNode: (
        StarlarkNode.Expression, StarlarkNode.Expression
      ) -> StarlarkNode.Expression.BinaryOperation
    ): StarlarkNode.Expression.BinaryOperation {
      dropToken<T>()
      val rightSide = parseExpression()
      return createNode(leftSide, rightSide)
    }

    private fun PeekableSequence<StarlarkToken>.parseDict(): StarlarkNode.Expression.Dict {
      val openDict = dropToken<StarlarkToken.LeftCurlyBrace>()
      val elements =
        parseRepeated<Argument.Named, StarlarkToken.RightCurlyBrace> { parseDictElement() }
      val closeDict = dropToken<StarlarkToken.RightCurlyBrace>()
      val openLoc = openDict.location.sourceLineIndexes
      val closeLoc = closeDict.location.sourceLineIndexes
      return StarlarkNode.Expression.Dict(
        elements.associate { it.name to it.expression },
        sourceLineIndexRange = openLoc.first..closeLoc.last
      )
    }

    private fun PeekableSequence<StarlarkToken>.parseList(): StarlarkNode.Expression.StarlarkList {
      val openList = dropToken<StarlarkToken.LeftSquareBrace>()
      val elements =
        parseRepeated<StarlarkNode.Expression, StarlarkToken.RightSquareBrace> { parseExpression() }
      val closeList = dropToken<StarlarkToken.RightSquareBrace>()
      val openLoc = openList.location.sourceLineIndexes
      val closeLoc = closeList.location.sourceLineIndexes
      return StarlarkNode.Expression.StarlarkList(
        elements, sourceLineIndexRange = openLoc.first..closeLoc.last
      )
    }

    private fun PeekableSequence<StarlarkToken>.parseTuple(): StarlarkNode.Expression.Tuple {
      val openTuple = dropToken<StarlarkToken.LeftParenthesis>()
      val elements =
        parseRepeated<StarlarkNode.Expression, StarlarkToken.RightParenthesis> { parseExpression() }
      val closeTuple = dropToken<StarlarkToken.RightParenthesis>()
      val openLoc = openTuple.location.sourceLineIndexes
      val closeLoc = closeTuple.location.sourceLineIndexes
      return StarlarkNode.Expression.Tuple(
        elements, sourceLineIndexRange = openLoc.first..closeLoc.last
      )
    }

    private fun PeekableSequence<StarlarkToken>.parseDictElement(): Argument.Named {
      // Dictionary elements must be <string|identifier> <colon> <expression>.
      val name = if (seekNextToken() is StarlarkToken.Identifier) {
        dropToken<StarlarkToken.Identifier>().value
      } else dropToken<StarlarkToken.SimpleString>().value
      dropToken<StarlarkToken.Colon>()
      return Argument.Named(name, expression = parseExpression())
    }

    private inline fun <E, reified T: StarlarkToken> PeekableSequence<StarlarkToken>.parseRepeated(
      parseElement: () -> E
    ): List<E> {
      return buildList {
        // Ensure there's at least one element in the list.
        if (seekNextToken() !is T) {
          add(parseElement())
          while (seekNextToken() is StarlarkToken.Comma) {
            dropToken<StarlarkToken.Comma>()
            if (seekNextToken() is T) break // Repeated elements ended.
            add(parseElement())
          }
        }
      }
    }

    private fun PeekableSequence<StarlarkToken>.parseLiteral(): StarlarkNode.Literal {
      return when (val nextToken = seekNextToken()) {
        is StarlarkToken.Bool -> parseBool()
        is StarlarkToken.DocString -> parseDocs()
        is StarlarkToken.Integer -> parseInteger()
        is StarlarkToken.SimpleString -> parseString()
        is StarlarkToken.Comma, is StarlarkToken.Equals, is StarlarkToken.Colon,
        is StarlarkToken.Period, is StarlarkToken.Plus, is StarlarkToken.Minus,
        is StarlarkToken.Modulo, is StarlarkToken.Identifier, is StarlarkToken.LeftCurlyBrace,
        is StarlarkToken.LeftParenthesis, is StarlarkToken.LeftSquareBrace,
        is StarlarkToken.LineComment, is StarlarkToken.RightCurlyBrace,
        is StarlarkToken.RightParenthesis, is StarlarkToken.RightSquareBrace, is StarlarkToken.Else,
        is StarlarkToken.For, is StarlarkToken.If, is StarlarkToken.In, null ->
          failOnToken(nextToken, expected = "literal")
      }
    }

    private fun PeekableSequence<StarlarkToken>.parseBool(): StarlarkNode.Literal.Bool =
      parseLiteral(dropToken(), StarlarkToken.Bool::value, StarlarkNode.Literal::Bool)

    private fun PeekableSequence<StarlarkToken>.parseDocs(): StarlarkNode.Literal.StarlarkString {
      return parseLiteral(
        dropToken(), StarlarkToken.DocString::contents, StarlarkNode.Literal::StarlarkString
      )
    }

    private fun PeekableSequence<StarlarkToken>.parseInteger(): StarlarkNode.Literal.Integer =
      parseLiteral(dropToken(), StarlarkToken.Integer::value, StarlarkNode.Literal::Integer)

    private fun PeekableSequence<StarlarkToken>.parseString(): StarlarkNode.Literal.StarlarkString {
      return parseLiteral(
        dropToken(), StarlarkToken.SimpleString::value, StarlarkNode.Literal::StarlarkString
      )
    }

    private inline fun <reified T: StarlarkToken, V, N: StarlarkNode> parseLiteral(
      token: T, extractValue: T.() -> V, createNode: (V, IntRange) -> N
    ): N = createNode(token.extractValue(), token.location.sourceLineIndexes)

    private fun failOnToken(token: StarlarkToken?, expected: String): Nothing {
      val location = token?.location ?: error("Something went internally wrong during parsing.")
      error("${location.computeErrorMessagePrefix()}: Expected $expected, not: $token.")
    }

    private sealed class Argument {
      data class Positional(val expression: StarlarkNode.Expression): Argument()
      data class Named(val name: String, val expression: StarlarkNode.Expression): Argument()
    }
  }

  private object StarlarkBuildFileInterpreter {
    private val IGNORED_FUNCTIONS = setOf(
      "load",
      "exports_files",
      "dagger_rules",
      "create_maven_dependency_wrappers",
      "create_direct_import_dependency_wrappers"
    )

    fun interpret(repoRoot: File, pkg: Package): BuildFile {
      val buildFile = pkg.computeBuildFile(repoRoot)
      return buildFile.inputStream().bufferedReader().use { reader ->
        BuildFile(buildFile, reader.parseTargets(repoRoot, buildFile, Workspace.Main, pkg).toList())
      }
    }

    private fun BufferedReader.parseTargets(
      repoRoot: File, buildFile: File, workspace: Workspace, pkg: Package
    ): Sequence<ParsedTarget> {
      val filePath = buildFile.toRelativeString(repoRoot)
      val nodes = StarlarkParser.parse(filePath, lineSequence())
      val expStatements = nodes.filterIsInstance<StarlarkNode.Statement.ExpressionStatement>()
      val topLevelExpressions = expStatements.map { it.expression }
      val funcCalls = topLevelExpressions.filterIsInstance<StarlarkNode.Expression.FunctionCall>()
      return funcCalls.mapNotNull { functionCall ->
        val funcRef = functionCall.container as? StarlarkNode.Expression.VariableReference
        funcRef?.name?.takeIf { it !in IGNORED_FUNCTIONS }?.let { funcName ->
          check(functionCall.positionalArguments.isEmpty()) {
            "$filePath: No support for positional arguments passed to $funcName."
          }
          check("name" in functionCall.namedArguments) {
            "$filePath: Expected 'name' parameter in macro call to $funcName."
          }
          return@let ParsedTarget(
            workspace,
            pkg,
            kind = funcName,
            properties = functionCall.namedArguments.mapValues { (_, exp) ->
              exp.parseToProperty()
            },
            functionCall.sourceLineIndexRange
          )
        }
      }
    }

    private fun StarlarkNode.Expression.parseToProperty(): PropertyValue {
      return when (this) {
        is StarlarkNode.Expression.LiteralExpression -> when (literal) {
          is StarlarkNode.Literal.Bool -> PropertyValue.BooleanValue(literal.value)
          is StarlarkNode.Literal.Integer -> PropertyValue.IntValue(literal.value)
          is StarlarkNode.Literal.StarlarkString -> PropertyValue.StringValue(literal.value)
        }
        is StarlarkNode.Expression.Dict -> {
          PropertyValue.DictionaryValue(
            elements.mapValues { (_, value) -> value.parseToProperty() }
          )
        }
        is StarlarkNode.Expression.StarlarkList ->
          PropertyValue.ListValue(elements.map { it.parseToProperty() })
        is StarlarkNode.Expression.FunctionCall -> {
          val functionName = (container as? StarlarkNode.Expression.VariableReference)?.name
          val parsedPosArgs = positionalArguments.map { it.parseToProperty() }
          val strArgs = parsedPosArgs.singleOrNull() as? PropertyValue.ListValue
          if (functionName == "glob" && namedArguments.isEmpty() && strArgs != null) {
            // Only consider globs that are simply lists of string expressions.
            PropertyValue.SimpleGlob(patterns = strArgs)
          } else PropertyValue.RuntimeEvaluated
        }
        is StarlarkNode.Expression.BinaryOperation, is StarlarkNode.Expression.Conditional,
        is StarlarkNode.Expression.Contains, is StarlarkNode.Expression.Indexing,
        is StarlarkNode.Expression.Loop, is StarlarkNode.Expression.MemberReference,
        is StarlarkNode.Expression.VariableReference, is StarlarkNode.Expression.Tuple ->
          PropertyValue.RuntimeEvaluated
      }
    }

    private fun Package.computeBuildFile(repoRoot: File): File {
      return File(File(repoRoot, path), "BUILD.bazel").also {
        check(it.exists() && it.isFile) {
          "Could not find BUILD.bazel file for target: ${this@computeBuildFile}."
        }
      }.absoluteFile.normalize()
    }
  }
}
