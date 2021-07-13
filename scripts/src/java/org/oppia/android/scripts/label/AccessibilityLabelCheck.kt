package org.oppia.android.scripts.label

import org.oppia.android.scripts.proto.AccessibilityLabelExemptions
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.io.FileInputStream
import java.util.stream.Collectors
import java.util.stream.IntStream
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Script for ensuring that all the Activities in the codebase are defined with accessibility
 * labels.
 *
 * Usage:
 *   bazel run //scripts:accessibility_label_check -- <path_to_directory_root> <path_to_app_level
 *   _manifest_file>
 *
 * Arguments:
 * - path_to_directory_root: directory path to the root of the Oppia Android repository.
 * - path_to_app_level_manifest_file: path to the manifest file of app layer.
 *
 * Example:
 *   bazel run //scripts:accessibility_label_check -- $(pwd) app/src/main/AndroidManifest.xml
 */
fun main(vararg args: String) {
  val repoPath = "${args[0]}/"

  val accessibilityLabelExemptiontextProto = "scripts/assets/accessibility_label_exemptions"

  val accessibilityLabelExemptionList = loadAccessibilityLabelExemptionsProto(
    accessibilityLabelExemptiontextProto
  ).getExemptedActivityList()

  val manifesFilePath = args[1]

  val fullPathToManifestFile = repoPath + manifesFilePath

  val builderFactory = DocumentBuilderFactory.newInstance()

  val docBuilder = builderFactory.newDocumentBuilder()

  val doc = docBuilder.parse(File(fullPathToManifestFile))
  doc.getDocumentElement().normalize()

  val completeActivityList = convertNodeListToListOfNode(doc.getElementsByTagName("activity"))

  val activityListWithoutLabel = completeActivityList.filter { node ->
    checkIfActivityHasMissingLabel(node, accessibilityLabelExemptionList)
  }

  logFailures(activityListWithoutLabel, repoPath)

  if (activityListWithoutLabel.isNotEmpty()) {
    throw Exception("ACCESSIBILITY LABEL CHECK FAILED")
  } else {
    println("ACCESSIBILITY LABEL CHECK PASSED")
  }
}

/**
 * Checks whether an activity element has a missing label.
 *
 * @param activityNode instance of Node
 * @param accessibilityLabelExemptionList list of all the exemptions of the label check
 * @return whether the label is present or not
 */
private fun checkIfActivityHasMissingLabel(
  activityNode: Node,
  accessibilityLabelExemptionList: List<String>
): Boolean {
  val attributesList = activityNode.getAttributes()
  val activityPath = attributesList.getNamedItem("android:name").getNodeValue()
  if (activityPath
    .removePrefix(".")
    .replace(".", "/") in accessibilityLabelExemptionList
  ) {
    return false
  }
  return attributesList.getNamedItem("android:label") == null
}

/**
 * The [nodeList] is not iterable. This helper
 * function converts it to List<Node>.
 *
 * @param nodeList instance of NodeList
 * @return a list of nodes
 */
private fun convertNodeListToListOfNode(nodeList: NodeList): List<Node> {
  return IntStream.range(0, nodeList.getLength())
    .mapToObj(nodeList::item)
    .collect(Collectors.toList())
}

/**
 * Logs the failures for accessibility label check.
 *
 * @param matchedNodes a list of nodes having missing label
 * @param repoPath path of the repo to be analyzed
 */
private fun logFailures(matchedNodes: List<Node>, repoPath: String) {
  val pathPrefix = "${repoPath}app/src/main/java/org/oppia/android"
  if (matchedNodes.isNotEmpty()) {
    println("Accessiblity labels missing for Activities:")
    matchedNodes.sortedBy {
      it.getAttributes()
        .getNamedItem("android:name")
        .getNodeValue()
    }.forEach { node ->
      println(
        "- $pathPrefix" +
          "${
          node.getAttributes()
            .getNamedItem("android:name")
            .getNodeValue()
            .replace(".", "/")
          }"
      )
    }
    println()
  }
}

/**
 * Loads the test file exemptions list to proto.
 *
 * @param accessibilityLabelExemptiontextProto the location of the accessibility label exemption
 *     textproto file.
 * @return proto class from the parsed textproto file
 */
private fun loadAccessibilityLabelExemptionsProto(accessibilityLabelExemptiontextProto: String):
  AccessibilityLabelExemptions {
    val protoBinaryFile = File("$accessibilityLabelExemptiontextProto.pb")
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
