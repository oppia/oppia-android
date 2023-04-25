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

class SuggestBuildFixes(private val repoRoot: File, private val bazelClient: BazelClient) {
  fun suggestBuildFixes(targetPatterns: List<String>, outputMode: OutputMode) {
    val allTargets = targetPatterns.flatMap {
      // Ignore 'manual' tagged builds as they aren't meant to be included in broad patterns.
      bazelClient.query("set($it) - attr(tags, 'manual', $it)", withSkyQuery = true)
    }
    println(
      "Trying to build ${allTargets.size} total targets from ${targetPatterns.size} pattern(s):"
    )
    targetPatterns.forEach { println("- $it") }
    println()

    // Only pre-build if it's actually useful (i.e. there's more than 1 target to build).
    if (allTargets.size > 1) {
      println("Prebuilding for faster analysis...")
      targetPatterns.forEach { bazelClient.build(it, keepGoing = true, allowFailures = true) }
      println()
    }

    val allFailures = allTargets.mapIndexed { index, target ->
      printTargetLine(prefix = "Inspecting (${index + 1}/${allTargets.size}) ", target, suffix = "")
      findIssuesWithTarget(target)
    }
    println()

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
          printTargetLine("Add deps to ", failure.target, " for missing imports:")
          failure.unresolvableImports.forEach { println(" - $it") }
          println()
        }
        is DetectedFailure.StrictDeps -> {
          when (outputMode) {
            OutputMode.DELTAS -> {
              printTargetLine("Add strict deps to ", failure.target, ":")
              val buildFile = failure.target.targetToBuildFile()
              println("  in ${buildFile.toRelativeString(repoRoot)}")
              failure.targetsToAdd.forEach { println("    \"${it.correctedTarget}\",") }
            }
            OutputMode.REPLACEMENT -> {
              val buildFile = failure.target.targetToBuildFile()
              val originalDeps = buildFile.retrieveDeps(failure.target)
              val updatedDeps =
                originalDeps.addDeps(failure.targetsToAdd.mapToSet { it.correctedTarget })
              printTargetLine("Replace deps for ", failure.target, ":")
              println("  in ${buildFile.toRelativeString(repoRoot)}")
              println(updatedDeps.deps.depsToList(prefix = "    "))
            }
            OutputMode.FIX -> {
              val buildFile = failure.target.targetToBuildFile()
              val originalDeps = buildFile.retrieveDeps(failure.target)
              val updatedDeps =
                originalDeps.addDeps(failure.targetsToAdd.mapToSet { it.correctedTarget })
              printTargetLine("Adding strict deps to ", failure.target, ":")
              println("  in ${buildFile.toRelativeString(repoRoot)}")
              failure.targetsToAdd.forEach { println("    \"${it.correctedTarget}\",") }
              buildFile.replaceDeps(updatedDeps)
            }
          }
          println()
        }
        is DetectedFailure.UnusedDeps -> {
          when (outputMode) {
            OutputMode.DELTAS -> {
              printTargetLine("Remove unused deps from ", failure.target, ":")
              val buildFile = failure.target.targetToBuildFile()
              println("  in ${buildFile.toRelativeString(repoRoot)}")
              failure.targetsToRemove.forEach { println("    \"${it.correctedTarget}\",") }
            }
            OutputMode.REPLACEMENT -> {
              val buildFile = failure.target.targetToBuildFile()
              val originalDeps = buildFile.retrieveDeps(failure.target)
              val updatedDeps =
                originalDeps.removeDeps(failure.targetsToRemove.mapToSet { it.correctedTarget })
              printTargetLine("Replace deps for ", failure.target, ":")
              println("  in ${buildFile.toRelativeString(repoRoot)}")
              println(updatedDeps.deps.depsToList(prefix = "    "))
            }
            OutputMode.FIX -> {
              val buildFile = failure.target.targetToBuildFile()
              val originalDeps = buildFile.retrieveDeps(failure.target)
              val updatedDeps =
                originalDeps.removeDeps(failure.targetsToRemove.mapToSet { it.correctedTarget })
              printTargetLine("Removing unused deps from ", failure.target, ":")
              println("  in ${buildFile.toRelativeString(repoRoot)}")
              failure.targetsToRemove.forEach { println("    \"${it.correctedTarget}\",") }
              buildFile.replaceDeps(updatedDeps)
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
      outputMode == OutputMode.FIX -> println("Please verify the issues described above are fixed.")
      else -> println("Please fix the issues described above and try again.")
    }
  }

  private fun findIssuesWithTarget(target: String): DetectedFailure =
    DetectedFailure.detectFailures(bazelClient, target)

  private fun String.targetToBuildFile(): File {
    val baseRelativePath = normalizeTarget().removePrefix("//")
    val basePath = if (':' in baseRelativePath) {
      baseRelativePath.replace(":", "/").substringBeforeLast('/')
    } else baseRelativePath
    return File(File(repoRoot, basePath), "BUILD.bazel").also {
      check(it.exists() && it.isFile) {
        "Could not find BUILD.bazel file for target: ${this@targetToBuildFile}."
      }
    }
  }

  private fun File.retrieveDeps(target: String): ParsedDeps {
    val targetName = target.replace(":", "/").substringAfterLast('/')
    return inputStream().bufferedReader().use { reader ->
      reader.lineSequence().withIndex().map { (index, line) ->
        index to line.trim()
      }.dropUntil { (_, line) ->
        line == "name = \"$targetName\","
      }.dropUntil { (_, line) ->
        line == "deps = ["
      }.takeUntil { (_, line) -> !line.startsWith("\"") }.mapToSet { (index, line) ->
        index to line.removePrefix("\"").removeSuffix("\",")
      }.let { indexedLines ->
        val indexes = indexedLines.map { (index, _) -> index }.sorted()
        return@let ParsedDeps(
          deps = indexedLines.mapToSet { (_, line) -> line },
          indexes.first() .. indexes.last()
        )
      }
    }
  }

  private fun File.replaceDeps(deps: ParsedDeps) {
    val updatedLines = readLines().asSequence().replaceRange(deps.lineRange) { oldLines ->
      val indent = oldLines.first().substringBefore('"')
      return@replaceRange deps.deps.map { "$indent\"$it\"," }
    }
    outputStream().bufferedWriter().use { writer -> updatedLines.forEach(writer::appendLine) }
  }

  private fun Iterable<String>.depsToList(prefix: String): String =
    joinToString(separator = "\n") { "$prefix\"$it\"," }

  enum class OutputMode {
    DELTAS,
    REPLACEMENT,
    FIX
  }

  private data class ParsedDeps(val deps: Set<String>, val lineRange: IntRange) {
    fun addDeps(deps: Set<String>) = copy(deps = this.deps + deps)
    fun removeDeps(deps: Set<String>) = copy(deps = this.deps - deps)
  }

  private sealed class DetectedFailure {
    abstract val target: String

    data class NoFailure(override val target: String) : DetectedFailure()

    data class Unknown(override val target: String) : DetectedFailure()

    data class UnresolvedReferences(
      override val target: String,
      val unresolvableImports: Set<String>
    ): DetectedFailure()

    data class StrictDeps(
      override val target: String, val targetsToAdd: Set<InterpretedTarget>
    ): DetectedFailure()

    data class UnusedDeps(
      override val target: String, val targetsToRemove: Set<InterpretedTarget>
    ): DetectedFailure()

    companion object {
      fun detectFailures(bazelClient: BazelClient, target: String): DetectedFailure {
        val failureLines = bazelClient.build(target, allowFailures = true)
        val correctedTarget = target.normalizeTarget()

        val targetForDepAdding = failureLines.singleOrNull {
          it.trim().startsWith("** Please add the following dependencies to")
        }?.trim()?.removePrefix("** Please add the following dependencies to")
          ?.trim()?.removeSuffix(":")?.normalizeTarget() ?: correctedTarget
        val depsToAdd = failureLines.asSequence().map { it.trim() }.dropUntil {
          it.trim().startsWith("** Please add the following dependencies to")
        }.takeUntil { !it.startsWith("- ") }.map { it.removePrefix("- ") }.map {
          if ("Target //" in it) it.substringBefore("Target //") else it
        }.mapToSet {
          InterpretedTarget.interpretTarget(bazelClient, it)
        }

        val targetForDepRemoval = failureLines.asSequence().map {
          it.removePrefix("INFO:").trim()
        }.singleOrNull {
          it.startsWith("** Please remove the following dependencies from")
        }?.removePrefix("** Please remove the following dependencies from")
          ?.trim()?.removeSuffix(":")?.normalizeTarget() ?: correctedTarget
        val depsToRemove = failureLines.asSequence().map {
          it.removePrefix("INFO:").trim()
        }.dropUntil {
          it.trim().startsWith("** Please remove the following dependencies from")
        }.takeUntil { !it.startsWith("- ") }.map { it.removePrefix("- ") }.mapToSet {
          InterpretedTarget.interpretTarget(bazelClient, it)
        }

        val targetForJavaDepAdding = failureLines.asSequence().dropUntil {
          it.trim().startsWith("** Please add the following dependencies:")
        }.firstOrNull()?.substringAfterLast(" to ")?.trim()?.removeSuffix(":")?.normalizeTarget()
          ?: correctedTarget
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
    abstract val correctedTarget: String

    data class Unknown(val originalTarget: String): InterpretedTarget() {
      override val correctedTarget: String get() = error("Unknown target: $originalTarget.")
    }

    data class Resolved(override val correctedTarget: String): InterpretedTarget()

    companion object {
      private val cachedTargetMapping = mutableMapOf<String, InterpretedTarget>()
      private val mavenThirdPartyPrefixMapping = mapOf(
        "maven_app" to "//third_party",
        "maven_scripts" to "//scripts:third_party"
      )

      fun interpretTarget(bazelClient: BazelClient, target: String): InterpretedTarget {
        return when {
          target.endsWith("_proto") -> bazelClient.interpretProtoTarget(target)
          target.startsWith("//") || target.startsWith("@//") ->
            Resolved(target.removePrefix("@").normalizeTarget())
          mavenThirdPartyPrefixMapping.keys.any { target.startsWith("@$it//") } ->
            bazelClient.interpretMavenTarget(target)
          "/_aar/" in target -> bazelClient.interpretAarTarget(target)
          // Special case re-interpretation since deps use a different target.
          target.startsWith("@com_google_protobuf_protobuf_javalite") ->
            Resolved("//third_party:com_google_protobuf_protobuf-javalite")
          "kotlinx-coroutines-core-jvm" in target ->
            Resolved("//third_party:kotlinx-coroutines-core-jvm")
          target.startsWith("@kotlitex") -> Resolved("//third_party:io_github_karino2_kotlitex")
          target.startsWith("@guava_android") -> Resolved("//third_party:com_google_guava_guava")
          target.startsWith("@circularimageview") ->
            Resolved("//third_party:circularimageview_circular_image_view")
          target.startsWith("@androidsvg") -> Resolved("//third_party:com_caverock_androidsvg")
          target.startsWith("@android-spotlight") ->
            Resolved("//third_party:com_github_takusemba_spotlight")
          else -> {
            bazelClient.interpretThirdPartyWrappedDependency(
              target, expectedThirdPartyPrefix = "//third_party"
            )
          }
        }
      }

      // The first wrapper library which depends on the proto should be the one exporting it (and
      // there should be just one).
      private fun BazelClient.interpretProtoTarget(target: String): InterpretedTarget {
        return resolveTarget(target) {
          query("kind(java_lite_proto_library,allrdeps($target,1))", withSkyQuery = true).single()
        }
      }

      private fun BazelClient.interpretAarTarget(target: String): InterpretedTarget {
        val mavenType = target.substringBefore("/_aar/").substringAfterLast('/')
        val targetName = target.substringAfter("/_aar/").substringBefore('/')
        return interpretMavenTarget(target = "@$mavenType//:$targetName")
      }

      private fun BazelClient.interpretMavenTarget(target: String): InterpretedTarget {
        val mavenType = target.removePrefix("@").substringBefore("//")
        return interpretThirdPartyWrappedDependency(
          target, expectedThirdPartyPrefix = mavenThirdPartyPrefixMapping.getValue(mavenType)
        )
      }

      private fun BazelClient.interpretThirdPartyWrappedDependency(
        target: String, expectedThirdPartyPrefix: String
      ): InterpretedTarget {
        return resolveTarget(target) {
          val queryResult =
            query("somepath($expectedThirdPartyPrefix/..., $target)", withSkyQuery = false)
          checkNotNull(queryResult.firstOrNull()) {
            "Failed to find third-party wrapper for Maven target: $target."
          }.also {
            require(it.startsWith(expectedThirdPartyPrefix)) {
              "Expected resolved target $it (for $target) to start with $expectedThirdPartyPrefix."
            }
          }
        }
      }

      private fun resolveTarget(originalTarget: String, query: () -> String?): InterpretedTarget {
        return cachedTargetMapping.getOrPut(originalTarget) {
          query()?.normalizeTarget()?.let(::Resolved) ?: Unknown(originalTarget)
        }
      }
    }
  }

  private companion object {
    private const val CONSOLE_LINE_LIMIT = 80

    private fun printTargetLine(prefix: String, target: String, suffix: String) {
      val unshortenedString = "$prefix$target$suffix"
      val unshortenedLength = unshortenedString.length
      val targetWithoutStart = target.substringAfter("//")

      if (unshortenedLength <= CONSOLE_LINE_LIMIT) {
        // No shortening needed.
        println(unshortenedString)
        return
      }

      if ("/" !in targetWithoutStart || !target.startsWith("//") || ":" !in target) {
        println(unshortenedString)
        return
      }

      val firstColon = target.indexOf(':')
      val firstSlash = target.substringAfter("//").indexOf('/') + 2
      val lastColon = target.indexOfLast { it == ':' }
      val lastSlash = target.indexOfLast { it == '/' }
      val initialDelimiter = firstColon.coerceAtMost(firstSlash)
      val finalDelimiter = lastColon.coerceAtLeast(lastSlash)
      val requiredTargetPrefix = target.substring(0 .. initialDelimiter)
      val targetMiddle = target.substring(initialDelimiter + 1 until finalDelimiter)
      val requiredTargetSuffix = target.substring(finalDelimiter)
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

    private fun String.normalizeTarget(): String {
      val baseNormalized = replace("Test_lib", "Test").removeSuffix("_kt")
      val endingPackage = baseNormalized.substringAfterLast('/')
      if (':' in endingPackage) {
        // If the target looks like: //path/to/target:target, then it can be simplified to just
        // path/to/target.
        val (lastPackage, target) = endingPackage.split(':', limit = 2)
        if (lastPackage == target) return "${baseNormalized.substringBeforeLast('/')}/$lastPackage"
      }
      return baseNormalized
    }
  }
}
