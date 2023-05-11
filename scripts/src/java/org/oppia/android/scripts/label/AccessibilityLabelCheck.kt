package org.oppia.android.scripts.label

import org.oppia.android.scripts.proto.AccessibilityLabelExemptions
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Script for ensuring that all the Activities in the codebase are defined with accessibility
 * labels.
 *
 * Usage:
 *   bazel run //scripts:accessibility_label_check -- <path_to_directory_root> [manifests/paths ...]
 *
 * Arguments:
 * - path_to_directory_root: directory path to the root of the Oppia Android repository.
 * - paths to manifest files: paths leading to the manifest files.
 *
 * Example:
 *   bazel run //scripts:accessibility_label_check -- $(pwd) app/src/main/AndroidManifest.xml
 */
fun main(vararg args: String) {
  val repoPath = "${args[0]}/"
  val accessibilityLabelExemptionList =
    ResourceLoader.loadResource("assets/accessibility_label_exemptions.pb")
      .use(InputStream::loadAccessibilityLabelExemptionsProto)
      .exemptedActivityList
  val manifestPaths = args.drop(1)
  val activityPathPrefix = "app/src/main/java/"
  val builderFactory = DocumentBuilderFactory.newInstance()
  val repoRoot = File(repoPath)

  val missingAccessibilityLabelActivities = manifestPaths.flatMap { relativePath ->
    val file = File(repoRoot, relativePath)
    val docBuilder = builderFactory.newDocumentBuilder()
    val doc = docBuilder.parse(file)
    // Normalisation results in the removal of redundancies such as whitespaces, line breaks and
    // comments.
    doc.documentElement.normalize()
    val packageName = doc.documentElement.getAttribute("package")
    return@flatMap doc.getElementsByTagName("activity").toListOfNodes().mapNotNull { activityNode ->
      computeFailureActivityPath(
        activityNode = activityNode,
        activityPathPrefix = activityPathPrefix,
        packageName = packageName
      )
    }
  }

  val redundantExemptions = accessibilityLabelExemptionList - missingAccessibilityLabelActivities
  val failureActivitiesAfterExemption = missingAccessibilityLabelActivities -
    accessibilityLabelExemptionList

  logRedundantExemptions(redundantExemptions)
  logFailures(failureActivitiesAfterExemption)

  if (failureActivitiesAfterExemption.isNotEmpty()) {
    println(
      "Refer to https://github.com/oppia/oppia-android/wiki/Static-Analysis-Checks" +
        "#accessibility-label-check for more details on how to fix this.\n"
    )
  }
  if (failureActivitiesAfterExemption.isNotEmpty() || redundantExemptions.isNotEmpty()) {
    throw Exception("ACCESSIBILITY LABEL CHECK FAILED")
  } else {
    println("ACCESSIBILITY LABEL CHECK PASSED")
  }
}

/**
 * Computes path of the activity which fails the accesssibility label check.
 *
 * @param activityNode the activity node
 * @param activityPathPrefix the path prefix for the activities
 * @param packageName the package attribute value of the manifest element
 * @return path of the failing activity relative to the root repository. This returns null if the
 *     activity has an accessibility label present.
 */
private fun computeFailureActivityPath(
  activityNode: Node,
  activityPathPrefix: String,
  packageName: String
): String? {
  val attributesList = activityNode.getAttributes()
  val activityName = attributesList.getNamedItem("android:name").getNodeValue()
  val activityPath = computeActivityPathFromName(
    activityPathPrefix = activityPathPrefix,
    activityName = activityName,
    packageName = packageName
  )
  if (attributesList.getNamedItem("android:label") != null) {
    return null
  }
  return activityPath
}

/**
 * Computes the activity path from the name attribute value of the activity element.
 *
 * @param activityPathPrefix the path prefix for the activities
 * @param activityName the name attribute value of the activity element
 * @param packageName the package attribute value of the manifest element
 * @return the activity path relative to the root repository
 */
private fun computeActivityPathFromName(
  activityPathPrefix: String,
  activityName: String,
  packageName: String
): String {
  if (activityName.startsWith(".")) {
    return activityPathPrefix + (packageName + activityName).replace(".", "/")
  } else {
    return activityPathPrefix + activityName.replace(".", "/")
  }
}

/**
 * Converts [NodeList] to list of nodes, since [NodeList] is not iterable.
 *
 * @return the list of nodes
 */
private fun NodeList.toListOfNodes(): List<Node> = (0 until getLength()).map(this::item)

/**
 * Logs the failures for accessibility label check.
 *
 * @param missingAccessibilityLabelActivities list of Activities missing the accessibility label
 */
private fun logFailures(missingAccessibilityLabelActivities: List<String>) {
  if (missingAccessibilityLabelActivities.isNotEmpty()) {
    println("Accessibility label missing for Activities:")
    missingAccessibilityLabelActivities.sorted().forEach { activityPath ->
      println("- $activityPath")
    }
    println()
  }
}

/**
 * Logs the redundant exemptions.
 *
 * @param redundantExemptions list of redundant exemptions
 */
private fun logRedundantExemptions(redundantExemptions: List<String>) {
  if (redundantExemptions.isNotEmpty()) {
    println("Redundant exemptions:")
    redundantExemptions.sorted().forEach { exemption ->
      println("- $exemption")
    }
    println("Please remove them from accessibility_label_exemptions.textproto")
    println()
  }
}

private fun InputStream.loadAccessibilityLabelExemptionsProto(): AccessibilityLabelExemptions =
  AccessibilityLabelExemptions.newBuilder().mergeFrom(this).build()

private object ResourceLoader {
  fun loadResource(name: String): InputStream {
    return checkNotNull(ResourceLoader::class.java.getResourceAsStream(name)) {
      "Failed to find resource corresponding to name: $name."
    }
  }
}
