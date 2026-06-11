package org.oppia.android.scripts.build

import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.GitClient
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.io.StringWriter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

private const val USAGE_STRING =
  "Usage: bazel run //scripts:transform_android_manifest -- </absolute/path/to/repo/root:Path> " +
    "</absolute/path/to/input/AndroidManifest.xml:Path> " +
    "</absolute/path/to/output/AndroidManifest.xml:Path> " +
    "<build_flavor:String> <major_app_version:Int> <minor_app_version:Int> " +
    "<application_relative_qualified_class:String> <enable_firebase_analytics:Boolean> " +
    "<enable_app_expiration:Boolean>"

/**
 * The main entrypoint for transforming an AndroidManifest to include both a version code and
 * generated version name (for production releases of the Oppia Android app).
 *
 * Note that this script is primarily meant to be run as part of the Bazel pipeline for AAB (Android
 * App Bundle) builds of the app, but it can also be run standalone. See build_flavors.bzl for
 * specifics on how this is run within the build pipeline. The example below is meant to be for
 * standalone uses. Note that the argument documentation below is also geared towards standalone
 * usage (the Bazel run of the script occurs within a runfiles sandbox folder & certain paths are
 * intentionally relative to that working directory). Finally, the Bazel runtime version of this
 * also does not actually run within the local Git repository (since it doesn't have access to it).
 * Instead, it copies just the .git folder of the local repository to create a sufficient copy to
 * compute a build hash.
 *
 * Usage:
 *   bazel run //scripts:transform_android_manifest -- <root_path>> \\
 *     <input_manifest_path> \\
 *     <output_manifest_path> \\
 *     <build_flavor> \\
 *     <major_app_version> \\
 *     <minor_app_version> \\
 *     <qualified_application_class_relative_to_app_package> \\
 *     <enable_firebase_analytics> \\
 *     <enable_app_expiration>
 *
 * Arguments:
 * - root_path: directory path to the root of the Oppia Android repository.
 * - input_manifest_path: directory path to the manifest to be processed.
 * - output_manifest_path: directory path to where the output manifest should be written.
 * - build_flavor: the flavor of the build corresponding to this manifest (e.g. 'dev' or 'alpha').
 * - major_app_version: the major version of the app.
 * - minor_app_version: the minor version of the app.
 * - qualified_application_class_relative_to_app_package: class path for custom Application class.
 * - enable_firebase_analytics: whether to enable Firebase Analytics.
 * - enable_app_expiration: whether to enable app expiration.
 *
 * Example:
 *   bazel run //scripts:transform_android_manifest -- $(pwd) \\
 *     $(pwd)/app/src/main/AndroidManifest.xml $(pwd)/TransformedAndroidManifest.xml alpha 0 6 \\
 *     .app.application.alpha.AlphaOppiaApplication false false
 */
fun main(args: Array<String>) {
  check(args.size >= 9) { USAGE_STRING }

  val repoRoot = File(args[0]).also { if (!it.exists()) error("File doesn't exist: ${args[0]}") }
  val srcManifest = File(args[1]).also { if (!it.exists()) error("File doesn't exist: ${args[1]}") }
  ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
    TransformAndroidManifest(
      repoRoot = repoRoot,
      sourceManifestFile = srcManifest,
      outputManifestFile = File(args[2]),
      buildFlavor = BuildFlavor.parseFlavor(args[3]),
      majorVersion = args[4].toIntOrNull() ?: error(USAGE_STRING),
      minorVersion = args[5].toIntOrNull() ?: error(USAGE_STRING),
      relativelyQualifiedApplicationClass = args[6],
      enableFirebaseAnalytics = args[7].toBoolean(),
      enableAppExpiration = args[8].toBoolean(),
      scriptBgDispatcher
    ).generateAndOutputNewManifest()
  }
}

/** Transformer for the main AndroidManifest.xml used in app binary builds. */
private class TransformAndroidManifest(
  private val repoRoot: File,
  private val sourceManifestFile: File,
  private val outputManifestFile: File,
  private val buildFlavor: BuildFlavor,
  private val majorVersion: Int,
  private val minorVersion: Int,
  private val relativelyQualifiedApplicationClass: String,
  private val enableFirebaseAnalytics: Boolean,
  private val enableAppExpiration: Boolean,
  private val scriptBgDispatcher: ScriptBackgroundCoroutineDispatcher
) {
  private val commandExecutor by lazy { CommandExecutorImpl(scriptBgDispatcher) }
  private val gitClient by lazy { GitClient(repoRoot, "origin/develop", commandExecutor) }
  private val documentBuilderFactory by lazy { DocumentBuilderFactory.newInstance() }
  private val transformerFactory by lazy { TransformerFactory.newInstance() }
  private val currentBranch by lazy { gitClient.currentBranch }
  private val releaseCandidateNumber: Int by lazy { computeReleaseCandidateNumber() }

  /**
   * Generates a new manifest by inserting the version code & computed version name, and then
   * outputs it to the defined [outputManifestFile].
   */
  fun generateAndOutputNewManifest() {
    // Parse the manifest & add the version code & name.
    val manifestDocument = documentBuilderFactory.parseXmlFile(sourceManifestFile)
    val versionCodeAttribute = manifestDocument.createAttribute("android:versionCode").apply {
      value = computeVersionCode().toString()
    }
    val versionNameAttribute = manifestDocument.createAttribute("android:versionName").apply {
      value = computeVersionName(
        buildFlavor, majorVersion, minorVersion, commitHash = gitClient.currentCommit
      )
    }
    val applicationNameAttribute = manifestDocument.createAttribute("android:name").apply {
      value = relativelyQualifiedApplicationClass
    }
    val replaceNameAttribute = manifestDocument.createAttribute("tools:replace").apply {
      // Other manifests may define duplicate names. Make sure the manifest merger knows to
      // prioritize this name.
      value = "android:name"
    }
    val manifestNode =
      manifestDocument.childNodes.asSequence().find { it.nodeName == "manifest" }
        ?: error("Failed to find top-level 'manifest' element in manifest file.")
    manifestNode.attributes.apply {
      setNamedItem(versionCodeAttribute)
      setNamedItem(versionNameAttribute)
    }
    val applicationNode =
      manifestNode.childNodes.asSequence().find { it.nodeName == "application" }
        ?: error("Failed to find an 'application' element in manifest.")
    applicationNode.attributes.apply {
      setNamedItem(applicationNameAttribute)
      setNamedItem(replaceNameAttribute)
    }

    if (enableFirebaseAnalytics) {
      println("WARNING: Firebase Analytics and Crashlytics are ENABLED in this build.")
      updateMetaData(applicationNode, "firebase_analytics_collection_deactivated", "false")
      updateMetaData(applicationNode, "firebase_crashlytics_collection_enabled", "true")
    } else {
      updateMetaData(applicationNode, "firebase_analytics_collection_deactivated", "true")
      updateMetaData(applicationNode, "firebase_crashlytics_collection_enabled", "false")
    }

    if (enableAppExpiration) {
      val expirationDate = LocalDate.now().plusMonths(12)
      val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
      val expirationDateString = expirationDate.format(formatter)
      updateMetaData(applicationNode, "automatic_app_expiration_enabled", "true")
      updateMetaData(applicationNode, "expiration_date", expirationDateString)
    } else {
      updateMetaData(applicationNode, "automatic_app_expiration_enabled", "false")
    }

    // Output the new transformed manifest.
    outputManifestFile.writeText(manifestDocument.toSource())
  }

  private fun updateMetaData(applicationNode: Node, name: String, value: String) {
    val metaDataNode = applicationNode.childNodes.asSequence()
      .filter { it.nodeName == "meta-data" }
      .find { it.attributes?.getNamedItem("android:name")?.nodeValue == name }
    if (metaDataNode != null) {
      metaDataNode.attributes.getNamedItem("android:value")?.nodeValue = value
      val document = metaDataNode.ownerDocument
      val replaceAttr = document.createAttribute("tools:replace").apply {
        nodeValue = "android:value"
      }
      metaDataNode.attributes.setNamedItem(replaceAttr)
    } else {
      error("Failed to find meta-data tag with name '$name' in manifest application node.")
    }
  }

  private fun computeReleaseCandidateNumber(): Int {
    // Each commit on the release branch is a potential release candidate.
    return if (currentBranch.startsWith("release-")) {
      (gitClient.countCommits("HEAD") - gitClient.countCommits(gitClient.branchMergeBase) + 1).also {
        check(it <= MAX_RCS_PER_RELEASE) {
          "Too many release candidates: $it. Max is $MAX_RCS_PER_RELEASE."
        }
      }
    } else 1 // Non-release branches are always considered the first RC of a release.
  }

  private fun computeVersionCode(): Int {
    val developCommitCount = gitClient.countCommits(gitClient.branchMergeBase)
    // The number of potential releases since the new version strategy was introduced.
    val possibleReleaseCount = developCommitCount - NEW_VERSION_STRATEGY_STARTING_COMMIT_NUMBER
    val releaseVersionOffset = possibleReleaseCount * VERSION_CODES_PER_RELEASE
    val rcVersionOffset = (releaseCandidateNumber - 1) * MAX_FLAVORS_PER_RC
    val flavorVersionOffset = buildFlavor.index
    return BASE_VERSION_CODE + releaseVersionOffset + rcVersionOffset + flavorVersionOffset
  }

  // The format here is defined as part of the app's release process.
  private fun computeVersionName(
    buildFlavor: BuildFlavor,
    majorVersion: Int,
    minorVersion: Int,
    commitHash: String
  ): String {
    val twoCharMinor = "%02d".format(minorVersion)
    val twoCharRc = "%02d".format(releaseCandidateNumber)
    val tenCharHash = commitHash.take(10)
    return "$majorVersion.$twoCharMinor-rc$twoCharRc-${buildFlavor.readableName}-$tenCharHash"
  }

  private fun DocumentBuilderFactory.parseXmlFile(file: File): Document =
    newDocumentBuilder().parse(file)

  private fun Document.toSource(): String {
    // Reference: https://stackoverflow.com/a/5456836.
    val transformer = transformerFactory.newTransformer()
    return StringWriter().apply {
      transformer.transform(DOMSource(this@toSource), StreamResult(this@apply))
    }.toString()
  }

  private companion object {
    // The version code beginning the new version code strategy.
    private const val BASE_VERSION_CODE = 300
    // The commit count at the time the new version code strategy was introduced.
    private const val NEW_VERSION_STRATEGY_STARTING_COMMIT_NUMBER = 2280
    private const val VERSION_CODES_PER_RELEASE = 1000
    private const val MAX_FLAVORS_PER_RC = 25
    private const val MAX_RCS_PER_RELEASE = VERSION_CODES_PER_RELEASE / MAX_FLAVORS_PER_RC

    private fun NodeList.asSequence() = (0 until length).asSequence().map { item(it) }
  }
}

/** Represents the active build flavors and their indices for versioning. */
private enum class BuildFlavor(val readableName: String, val index: Int) {
  /** Corresponds to the globally available flavor of the app. */
  GA(readableName = "ga", index = 0),
  /** Corresponds to the beta (open testing) flavor of the app. */
  BETA(readableName = "beta", index = 1),
  /** Corresponds to the alpha (closed testing and user study) flavor of the app. */
  ALPHA(readableName = "alpha", index = 2),
  /** Corresponds to the developer-only flavor of the app. */
  DEV(readableName = "dev", index = 3);

  companion object {
    /** Returns the [BuildFlavor] corresponding to the human-readable [argValue]. */
    fun parseFlavor(argValue: String): BuildFlavor {
      return checkNotNull(values().find { it.readableName == argValue }) {
        "Unknown build flavor: $argValue. Expected one of: " +
          values().joinToString { it.readableName }
      }
    }
  }
}
