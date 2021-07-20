package org.oppia.android.scripts.label

import org.oppia.android.scripts.proto.AccessibilityLabelExemptions
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.io.FileInputStream
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Script for ensuring that all the Activities in the codebase are defined with accessibility
 * labels.
 *
 * Usage:
 *   bazel run //scripts:accessibility_label_check -- <path_to_directory_root> <path_to_manifest
 *   _files>
 *
 * Arguments:
 * - path_to_directory_root: directory path to the root of the Oppia Android repository.
 * - path_to_manifest_files: path to the manifest files
 *
 * Example:
 *   bazel run //scripts:accessibility_label_check -- $(pwd) app/src/main/AndroidManifest.xml
 */
fun main(vararg args: String) {
  val repoPath = "${args[0]}/"

  val accessibilityLabelExemptionTextProto = "scripts/assets/accessibility_label_exemptions"

  val accessibilityLabelExemptionList = loadAccessibilityLabelExemptionsProto(
    accessibilityLabelExemptionTextProto
  ).getExemptedActivityList()

  val manifestPaths = args.drop(1)

  val activityPathPrefix = "app/src/main/java/"

  val builderFactory = DocumentBuilderFactory.newInstance()

  val missingAccessibilityLabelActivities = manifestPaths.flatMap { relativePath ->
    val file = File(repoPath + relativePath)
    val docBuilder = builderFactory.newDocumentBuilder()
    val doc = docBuilder.parse(file)
    doc.getDocumentElement().normalize()
    val packageName = doc.getDocumentElement().getAttribute("package")
    doc.getElementsByTagName("activity").toListOfNodes().mapNotNull { activityNode ->
      computeFailureActivityPath(
        activityNode = activityNode,
        accessibilityLabelExemptionList = accessibilityLabelExemptionList,
        activityPathPrefix = activityPathPrefix,
        packageName = packageName
      )
    }
  }

  logFailures(
    repoPath,
    missingAccessibilityLabelActivities,
    accessibilityLabelExemptionTextProto
  )

  if (missingAccessibilityLabelActivities.isNotEmpty()) {
    throw Exception("ACCESSIBILITY LABEL CHECK FAILED")
  } else {
    println("ACCESSIBILITY LABEL CHECK PASSED")
  }
}

/**
 * Computes path of the activity which fails the accesssibility label check.
 *
 * @param activityNode the activity node
 * @param accessibilityLabelExemptionList list of the accessibility label check exemptions
 * @param activityPathPrefix the path prefix for the activities
 * @param packageName the package attribute value of the manifest element
 * @return path of the failing activity relative to the root repository. This returns null if the
 *     activity has an accessibility label present.
 */
private fun computeFailureActivityPath(
  activityNode: Node,
  accessibilityLabelExemptionList: List<String>,
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
  if (activityPath in accessibilityLabelExemptionList) {
    return null
  } else if (attributesList.getNamedItem("android:label") != null) {
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
    return activityPathPrefix.plus(packageName.plus(activityName).replace(".", "/"))
  } else {
    return activityPathPrefix.plus(activityName.replace(".", "/"))
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
 * @param repoPath path of the repo to be analyzed
 * @param missingAccessibilityLabelActivities list of Activities missing accessibility label
 * @param accessibilityLabelExemptionTextProto the location of the accessibility label exemption
 *     textproto file.
 */
private fun logFailures(
  repoPath: String,
  missingAccessibilityLabelActivities: List<String>,
  accessibilityLabelExemptionTextProto: String
) {
  if (missingAccessibilityLabelActivities.isNotEmpty()) {
    println("Accessibility label missing for Activities:")
    missingAccessibilityLabelActivities.sorted().forEach { activityPath ->
      println("- ${repoPath.plus(activityPath)}")
    }
    println()
    println("If this is correct, please update $accessibilityLabelExemptionTextProto.textproto")
    println(
      "Note that, in general, all Activities should have labels. If you choose to add an" +
        " exemption, please specifically call this out in your PR description."
    )
    println()
  }
}

/**
 * Loads the test file exemptions list to proto.
 *
 * @param accessibilityLabelExemptionTextProto the location of the accessibility label exemption
 *     textproto file.
 * @return proto class from the parsed textproto file
 */
private fun loadAccessibilityLabelExemptionsProto(
  accessibilityLabelExemptionTextProto: String
): AccessibilityLabelExemptions {
  val protoBinaryFile = File("$accessibilityLabelExemptionTextProto.pb")
  val builder = AccessibilityLabelExemptions.getDefaultInstance().newBuilderForType()

  // This cast is type-safe since proto guarantees type consistency from mergeFrom(),
  // and this method is bounded by the generic type T.
  @Suppress("UNCHECKED_CAST")
  val protoObj: AccessibilityLabelExemptions =
    FileInputStream(protoBinaryFile).use {
      builder.mergeFrom(it)
    }.build() as AccessibilityLabelExemptions
  return protoObj
}
