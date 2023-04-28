package org.oppia.android.scripts.build

import java.io.File
import java.util.concurrent.TimeUnit
import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher

fun main(vararg args: String) {
  require(args.size > 2) {
    "Usage: bazel run //scripts:suggest_build_fixes --" +
      " <root_directory> <mode=deltas/replacement/fix> <bazel_target_exp:String> ..."
  }
  val repoRoot = File(args[0]).absoluteFile.normalize().also {
    require(it.exists() && it.isDirectory) {
      "Provided repository root doesn't exist or isn't a directory: $it."
    }
  }
  require(args[1].startsWith("mode=")) { "Expected 'mode' argument to start with 'mode='." }
  val mode = when (val modeStr = args[1].removePrefix("mode=")) {
    "deltas" -> SuggestBuildFixes.OutputMode.DELTAS
    "replacement" -> SuggestBuildFixes.OutputMode.REPLACEMENT
    "fix" -> SuggestBuildFixes.OutputMode.FIX
    else -> error("Expected mode to be one of: 'deltas', 'replacement' or 'fix', not: $modeStr.")
  }
  ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
    val commandExecutor =
      CommandExecutorImpl(
        scriptBgDispatcher, processTimeout = 30, processTimeoutUnit = TimeUnit.MINUTES
      )
    val bazelClient = BazelClient(repoRoot, commandExecutor)
    SuggestBuildFixes(repoRoot, bazelClient).suggestBuildFixes(args.drop(2).toList(), mode)
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
  fun suggestBuildFixes(targetPatterns: List<String>, outputMode: OutputMode) {
    suggestBuildFixesAux(targetPatterns, outputMode)
  }

  private fun suggestBuildFixesAux(
    targetPatterns: List<String>,
    outputMode: OutputMode,
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
    println("Pre-building for to compute which targets actually require reanalyzing...")
    targetPatterns.forEach {
      bazelClient.build(it, keepGoing = true, allowFailures = true, buildRunfileLinks = false)
    }

    // Second, determine which targets are failing and require reworking.
    val targetsRequiringAnalysis = targetPatterns.flatMap { targetPattern ->
      bazelClient.build(
        targetPattern,
        keepGoing = true,
        allowFailures = true,
        checkUpToDate = true, // Only check whether targets are up-to-date.
        buildRunfileLinks = false // Only build status matters, not runfile links.
      ).mapNotNull { outputLine ->
        outputLine.takeIf { it.startsWith("ERROR: ") }?.substringAfter("ERROR: ")
      }.mapNotNull { errorLine ->
        check(errorLine.startsWith("action '") && errorLine.endsWith("is not up-to-date")) {
          "Encountered unexpected failure while building pattern '$targetPattern':\n$errorLine" +
            "\nPlease resolve this manually."
        }
        // Ignore out-of-date actions that don't include targets, and ones that have targets which
        // aren't tied to the main repository (since these should be built incidentally).
        return@mapNotNull errorLine.takeIf { line ->
          '@' in line
        }?.substringAfter('@')?.substringBefore(' ')?.takeIf { it.startsWith("//") }
      }
    }.mapToSet(Target::parse)

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
      when (failure) {
        is DetectedFailure.UnresolvedReferences -> {
          printTargetLine("Add deps to ", failure.target, " (or a dep) for missing imports:")
          failure.unresolvableImports.forEach { println(" - $it") }
          println()
        }
        is DetectedFailure.StrictDeps -> {
          when (outputMode) {
            OutputMode.DELTAS -> {
              printTargetLine("Add strict deps to ", failure.target, ":")
              val buildFile = failure.target.computeBuildFile()
              println("  in ${buildFile.toRelativeString(repoRoot)}")
              failure.targetsToAdd.forEach {
                println("    \"${it.correctedTarget.simpleQualifiedTargetPath}\",")
              }
            }
            OutputMode.REPLACEMENT -> {
              val buildFile = failure.target.computeBuildFile()
              val originalDeps = buildFile.retrieveDeps(failure.target)
              val updatedDeps =
                originalDeps.addDeps(failure.targetsToAdd.mapToSet { it.correctedTarget })
              printTargetLine("Replace deps for ", failure.target, ":")
              println("  in ${buildFile.toRelativeString(repoRoot)}")
              println(updatedDeps.deps.depsToList(prefix = "    "))
            }
            OutputMode.FIX -> {
              val buildFile = failure.target.computeBuildFile()
              val originalDeps = buildFile.retrieveDeps(failure.target)
              val updatedDeps =
                originalDeps.addDeps(failure.targetsToAdd.mapToSet { it.correctedTarget })
              printTargetLine("Adding strict deps to ", failure.target, ":")
              println("  in ${buildFile.toRelativeString(repoRoot)}")
              failure.targetsToAdd.forEach {
                println("    \"${it.correctedTarget.simpleQualifiedTargetPath}\",")
              }
              buildFile.replaceDeps(updatedDeps, referencingPackage = failure.target.pkg)
            }
          }
          println()
        }
        is DetectedFailure.UnusedDeps -> {
          when (outputMode) {
            OutputMode.DELTAS -> {
              printTargetLine("Remove unused deps from ", failure.target, ":")
              val buildFile = failure.target.computeBuildFile()
              println("  in ${buildFile.toRelativeString(repoRoot)}")
              failure.targetsToRemove.forEach {
                println("    \"${it.correctedTarget.simpleQualifiedTargetPath}\",")
              }
            }
            OutputMode.REPLACEMENT -> {
              val buildFile = failure.target.computeBuildFile()
              val originalDeps = buildFile.retrieveDeps(failure.target)
              val updatedDeps =
                originalDeps.removeDeps(failure.targetsToRemove.mapToSet { it.correctedTarget })
              printTargetLine("Replace deps for ", failure.target, ":")
              println("  in ${buildFile.toRelativeString(repoRoot)}")
              println(updatedDeps.deps.depsToList(prefix = "    "))
            }
            OutputMode.FIX -> {
              val buildFile = failure.target.computeBuildFile()
              val originalDeps = buildFile.retrieveDeps(failure.target)
              val updatedDeps =
                originalDeps.removeDeps(failure.targetsToRemove.mapToSet { it.correctedTarget })
              printTargetLine("Removing unused deps from ", failure.target, ":")
              println("  in ${buildFile.toRelativeString(repoRoot)}")
              failure.targetsToRemove.forEach {
                println("    \"${it.correctedTarget.simpleQualifiedTargetPath}\",")
              }
              buildFile.replaceDeps(updatedDeps, referencingPackage = failure.target.pkg)
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
        println()
        suggestBuildFixesAux(
          targetPatterns, outputMode, previousFailures = noteworthyFailures.toSet()
        )
      }
      else -> println("Please fix the issues described above and try again.")
    }
  }

  private fun findIssuesWithTarget(target: Target): DetectedFailure =
    DetectedFailure.detectFailures(bazelClient, target)

  private fun Target.computeBuildFile(): File {
    require(workspace == Workspace.Main) {
      "Cannot compute build file for remote workspace: $this."
    }
    return File(File(repoRoot, pkg.path), "BUILD.bazel").also {
      check(it.exists() && it.isFile) {
        "Could not find BUILD.bazel file for target: ${this@computeBuildFile}."
      }
    }
  }

  private fun File.retrieveDeps(target: Target): ParsedDeps {
    return inputStream().bufferedReader().use { reader ->
      reader.lineSequence().withIndex().map { (index, line) ->
        index to line.trim()
      }.dropUntil { (_, line) ->
        line == "name = \"${target.name}\","
      }.dropUntil { (_, line) ->
        line == "deps = ["
      }.takeUntil { (_, line) -> !line.startsWith("\"") }.mapToSet { (index, line) ->
        index to line.removePrefix("\"").removeSuffix("\",")
      }.let { indexedLines ->
        val indexes = indexedLines.map { (index, _) -> index }.sorted()
        return@let ParsedDeps(
          deps = indexedLines.mapToSet { (_, line) ->
            Target.parseReference(targetReference = line, referencingTarget = target)
          },
          indexes.first()..indexes.last()
        )
      }
    }
  }

  private fun File.replaceDeps(deps: ParsedDeps, referencingPackage: Package) {
    val updatedLines = readLines().asSequence().replaceRange(deps.lineRange) { oldLines ->
      val indent = oldLines.first().substringBefore('"')
      return@replaceRange deps.deps.sorted().map { target ->
        "$indent\"${target.normalize().computeRelativePath(referencingPackage)}\","
      }
    }
    outputStream().bufferedWriter().use { writer -> updatedLines.forEach(writer::appendLine) }
  }

  private fun Iterable<Target>.depsToList(prefix: String): String =
    joinToString(separator = "\n") { "$prefix\"${it.simpleQualifiedTargetPath}\"," }

  enum class OutputMode {
    DELTAS,
    REPLACEMENT,
    FIX
  }

  private data class ParsedDeps(val deps: Set<Target>, val lineRange: IntRange) {
    fun addDeps(deps: Set<Target>) = copy(deps = this.deps + deps)

    fun removeDeps(deps: Set<Target>): ParsedDeps {
      // Dagger is a bit hacky in how it's referenced (since the top-level //:dagger target exports
      // a Dagger target similar to //third_party:com_google_dagger_dagger). This requires special
      // handling to ensure the auto-fixer doesn't get stuck when trying to clean up such deps.
      val oldDeps = this.deps
      val baseDaggerReference = setOf(THIRD_PARTY_DAGGER_TARGET)
      val depsToRemove = deps - baseDaggerReference
      val firstPassUpdatedDeps = oldDeps - depsToRemove
      val removingDagger = depsToRemove.size < deps.size
      val secondPassUpdatedDeps = if (removingDagger) {
        // Dagger is also being removed. First, try to remove it using the //third_party reference.
        firstPassUpdatedDeps - baseDaggerReference
      } else firstPassUpdatedDeps
      val daggerWasNotRemoved = secondPassUpdatedDeps.size == firstPassUpdatedDeps.size
      val thirdPassUpdatedDeps = if (removingDagger && daggerWasNotRemoved) {
        // If the target doesn't contain the third-party reference, try removing the generated
        // :dagger target, instead.
        secondPassUpdatedDeps - GENERATED_DAGGER_TARGET
      } else secondPassUpdatedDeps
      return copy(deps = thirdPassUpdatedDeps)
    }

    private companion object {
      private val THIRD_PARTY_DAGGER_TARGET by lazy {
        Target.parse("//third_party:com_google_dagger_dagger")
      }
      private val GENERATED_DAGGER_TARGET by lazy { Target.parse("//:dagger") }
    }
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
        }.firstOrNull()?.substringAfterLast(" to ")?.trim()?.removeSuffix(":")
          ?.parseNormalizedTarget() ?: correctedTarget
        val depsToAddForJava = failureLines.asSequence().map { it.trim() }.dropUntil {
          it.trim().startsWith("** Please add the following dependencies:")
        }.firstOrNull()?.substringBefore(" to ")?.trim()?.split(" ")?.mapToSet {
          InterpretedTarget.interpretTarget(bazelClient, it)
        } ?: emptySet()

        val unresolvedReferences = failureLines.mapIndexedNotNull { index, line ->
          index.takeIf { "unresolved reference" in line }?.let { it + 1 }
        }.mapNotNull(failureLines::getOrNull).filter {
          it.startsWith("import")
        }.mapTo(mutableSetOf()) { it.substringAfter("import ") }
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

    data class Resolved(override val correctedTarget: Target): InterpretedTarget()

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
        val mavenType = rawTarget.substringBefore("/_aar/").substringAfterLast('/')
        val targetName = rawTarget.substringAfter("/_aar/").substringBefore('/')
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
      private const val VALID_FILENAME_CHARS = ".${Package.VALID_COMPONENT_CHARS}"
      private const val VALID_FULLY_QUALIFIED_TARGET_CHARS = "@/:$VALID_FILENAME_CHARS"
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

        val localTarget = rawTarget.substringAfter("//")
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

      // 'targetReference' may be a local reference (e.g. ':package') or fully qualified.
      fun parseReference(targetReference: String, referencingTarget: Target): Target {
        return if (targetReference.startsWith(':')) {
          // The target is relative to the referencing target.
          referencingTarget.retarget { targetReference.substringAfter(':') }
        } else parse(targetReference)
      }

      fun parseNonStrict(rawTarget: String): Target? {
        return Workspace.parse(rawTarget.substringBefore("//"))?.let { workspace ->
          val localTarget = rawTarget.substringAfter("//")
          Package.parse(localTarget.substringBeforeLast(':'))?.let { pkg ->
            parseTargetName(localTarget, pkg)?.let { targetName ->
              Target(workspace, pkg, targetName)
            }
          }
        }
      }

      private fun parseTargetName(localTarget: String, pkg: Package): String? {
        val maybeTargetName = localTarget.substringAfterLast(':').takeIf(String::isNotEmpty)
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
       val workspaceName = prefix.substringAfter('@')
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

  private companion object {
    private const val CONSOLE_LINE_LIMIT = 80

    private fun printTargetLine(prefix: String, target: Target, suffix: String) {
      val rawTarget = target.simpleQualifiedTargetPath
      val unshortenedString = "$prefix$rawTarget$suffix"
      val unshortenedLength = unshortenedString.length
      val targetWithoutStart = rawTarget.substringAfter("//")

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
      val firstSlash = rawTarget.substringAfter("//").indexOf('/') + 2
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
      range: IntRange, createReplacement: (List<T>) -> Iterable<T>
    ): Sequence<T> {
      var addedReplacement = false
      val oldRangeValues = mutableListOf<T>()
      return withIndex().flatMap { (index, value) ->
        return@flatMap when {
          oldRangeValues.isEmpty() && index !in range -> listOf(value) // Values before the range.
          index in range -> emptyList<T>().also { oldRangeValues += value } // Skip old value.
          !addedReplacement ->
            (createReplacement(oldRangeValues) + value).also { addedReplacement = true }
          else -> listOf(value) // Values after the range.
        }
      }
    }

    private fun String.parseNormalizedTarget(): Target = Target.parse(this).normalize()
  }
}
