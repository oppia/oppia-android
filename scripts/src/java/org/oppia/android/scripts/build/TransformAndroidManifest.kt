package org.oppia.android.scripts.build

import org.oppia.android.scripts.common.GitClient
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import java.io.File
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

private const val USAGE_STRING =
  "Usage: bazel run //scripts:transform_android_manifest -- </absolute/path/to/repo/root:Path> " +
    "</absolute/path/to/input/AndroidManifest.xml:Path> " +
    "</absolute/path/to/output/AndroidManifest.xml:Path> " +
    "<build_flavor:String> <major_app_version:Int> <minor_app_version:Int> <version_code:Int> " +
    "<application_relative_qualified_class:String> <base_develop_branch_reference:String>"

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
 *     <version_code> \\
 *     <qualified_application_class_relative_to_app_package> \\
 *     <base_develop_branch_reference>
 *
 * Arguments:
 * - root_path: directory path to the root of the Oppia Android repository.
 * - input_manifest_path: directory path to the manifest to be processed.
 * - output_manifest_path: directory path to where the output manifest should be written.
 * - build_flavor: the flavor of the build corresponding to this manifest (e.g. 'dev' or 'alpha').
 * - major_app_version: the major version of the app.
 * - minor_app_version: the minor version of the app.
 * - version_code: the next version code to use.
 * - base_develop_branch_reference: the reference to the local develop branch that should be use.
 *     Generally, this is 'origin/develop'.
 *
 * Example:
 *   bazel run //scripts:transform_android_manifest -- $(pwd) \\
 *     $(pwd)/app/src/main/AndroidManifest.xml $(pwd)/TransformedAndroidManifest.xml alpha 0 6 6 \\
 *     .app.application.alpha.AlphaOppiaApplication origin/develop
 */
fun main(args: Array<String>) {
  check(args.size >= 9) { USAGE_STRING }

  val repoRoot = File(args[0]).also { if (!it.exists()) error("File doesn't exist: ${args[0]}") }
  TransformAndroidManifest(
    repoRoot = repoRoot,
    sourceManifestFile = File(args[1]).also {
      if (!it.exists()) {
        error("File doesn't exist: ${args[1]}")
      }
    },
    outputManifestFile = File(args[2]),
    buildFlavor = args[3],
    majorVersion = args[4].toIntOrNull() ?: error(USAGE_STRING),
    minorVersion = args[5].toIntOrNull() ?: error(USAGE_STRING),
    versionCode = args[6].toIntOrNull() ?: error(USAGE_STRING),
    relativelyQualifiedApplicationClass = args[7],
    baseDevelopBranchReference = args[8]
  ).generateAndOutputNewManifest()
}

private class TransformAndroidManifest(
  private val repoRoot: File,
  private val sourceManifestFile: File,
  private val outputManifestFile: File,
  private val buildFlavor: String,
  private val majorVersion: Int,
  private val minorVersion: Int,
  private val versionCode: Int,
  private val relativelyQualifiedApplicationClass: String,
  private val baseDevelopBranchReference: String
) {
  private val gitClient by lazy {
    GitClient(repoRoot, baseDevelopBranchReference)
  }
  private val documentBuilderFactory by lazy { DocumentBuilderFactory.newInstance() }
  private val transformerFactory by lazy { TransformerFactory.newInstance() }

  /**
   * Generates a new manifest by inserting the version code & computed version name, and then
   * outputs it to the defined [outputManifestFile].
   */
  fun generateAndOutputNewManifest() {
    // Parse the manifest & add the version code & name.
    val manifestDocument = documentBuilderFactory.parseXmlFile(sourceManifestFile)
    val versionCodeAttribute = manifestDocument.createAttribute("android:versionCode").apply {
      value = versionCode.toString()
    }
    val versionNameAttribute = manifestDocument.createAttribute("android:versionName").apply {
      value = computeVersionName(
        buildFlavor, majorVersion, minorVersion, commitHash = gitClient.branchMergeBase
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
    val manifestNode = manifestDocument.childNodes.item(0)
    manifestNode.attributes.apply {
      setNamedItem(versionCodeAttribute)
      setNamedItem(versionNameAttribute)
    }
    val applicationNode =
      manifestNode.childNodes.asSequence().find { it.nodeName == "application" }
        ?: error("Failed to find an 'application' tag in manifest.")
    applicationNode.attributes.apply {
      setNamedItem(applicationNameAttribute)
      setNamedItem(replaceNameAttribute)
    }

    // Output the new transformed manifest.
    outputManifestFile.writeText(manifestDocument.toSource())
  }

  // The format here is defined as part of the app's release process.
  private fun computeVersionName(
    flavor: String,
    majorVersion: Int,
    minorVersion: Int,
    commitHash: String
  ): String = "$majorVersion.$minorVersion-$flavor-${commitHash.take(10)}"

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
    private fun NodeList.asSequence() = (0 until length).asSequence().map { item(it) }
  }
}
