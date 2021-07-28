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
 *   bazel run //scripts:accessibility_label_check -- <path_to_directory_root>
 *   <path_to_proto_binary> [paths to manifest files...]
 *
 * Arguments:
 * - path_to_directory_root: directory path to the root of the Oppia Android repository.
 * - path_to_proto_binary: relative path to the exemption .pb file.
 * - paths to manifest files: paths leading to the manifest files.
 *
 * Example:
 *   bazel run //scripts:accessibility_label_check -- $(pwd) app/src/main/AndroidManifest.xml
 *    scripts/assets/accessibility_label_exemptions.pb
 */
fun main(vararg args: String) {
  val repoPath = "${args[0]}/"

  val pathToProtoBinary = args[1]

  val accessibilityLabelExemptionTextProtoFilePath = "scripts/assets/accessibility_label_exemptions"

  val accessibilityLabelExemptionList =
    loadAccessibilityLabelExemptionsProto(pathToProtoBinary).getExemptedActivityList()

  val manifestPaths = args.drop(2)

  val activityPathPrefix = "app/src/main/java/"

  val builderFactory = DocumentBuilderFactory.newInstance()

  val repoRoot = File(repoPath)

  val missingAccessibilityLabelActivities = manifestPaths.flatMap { relativePath ->
    val file = File(repoRoot, relativePath)
    val docBuilder = builderFactory.newDocumentBuilder()
    val doc = docBuilder.parse(file)
    // Normalisation results in the removal of redundancies such as whitespaces, line breaks and
    // comments.
    doc.getDocumentElement().normalize()
    val packageName = doc.getDocumentElement().getAttribute("package")
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

  logRedundantExemptions(redundantExemptions, accessibilityLabelExemptionTextProtoFilePath)

  logFailures(failureActivitiesAfterExemption, accessibilityLabelExemptionTextProtoFilePath)

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
 * @param missingAccessibilityLabelActivities list of Activities missing accessibility label
 * @param accessibilityLabelExemptionTextProtoFilePath the location of the accessibility label
 *     exemption textproto file.
 */
private fun logFailures(
  missingAccessibilityLabelActivities: List<String>,
  accessibilityLabelExemptionTextProtoFilePath: String
) {
  if (missingAccessibilityLabelActivities.isNotEmpty()) {
    println("Accessibility label missing for Activities:")
    missingAccessibilityLabelActivities.sorted().forEach { activityPath ->
      println("- $activityPath")
    }
    println()
    println(
      "If this is correct, please update $accessibilityLabelExemptionTextProtoFilePath.textproto"
    )
    println(
      "Note that, in general, all Activities should have labels. If you choose to add an" +
        " exemption, please specifically call this out in your PR description."
    )
    println()
  }
}

/**
 * Logs the redundant exemptions.
 *
 * @param redundantExemptions list of redundant exemptions
 * @param accessibilityLabelExemptionTextProtoFilePath the location of the accessibility label
 *     exemption textproto file.
 */
private fun logRedundantExemptions(
  redundantExemptions: List<String>,
  accessibilityLabelExemptionTextProtoFilePath: String
) {
  if (redundantExemptions.isNotEmpty()) {
    println("Redundant exemptions:")
    redundantExemptions.sorted().forEach { exemption ->
      println("- $exemption")
    }
    println(
      "Please remove them from $accessibilityLabelExemptionTextProtoFilePath.textproto"
    )
    println()
  }
}

/**
 * Loads the test file exemptions list to proto.
 *
 * @param pathToProtoBinary path to the pb file to be parsed
 * @return proto class from the parsed textproto file
 */
private fun loadAccessibilityLabelExemptionsProto(
  pathToProtoBinary: String
): AccessibilityLabelExemptions {
  val protoBinaryFile = File(pathToProtoBinary)
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
