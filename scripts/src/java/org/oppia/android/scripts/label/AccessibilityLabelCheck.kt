package org.oppia.android.scripts.label

import org.oppia.android.scripts.common.ACCESSIBILITY_LABEL_CHECK_FAILED_OUTPUT_INDICATOR
import org.oppia.android.scripts.common.ACCESSIBILITY_LABEL_CHECK_PASSED_OUTPUT_INDICATOR
import org.oppia.android.scripts.common.ScriptExemptions
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
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
  val repoPath = args[0] + "/"

  val manifesFilePath = args[1]

  val fullPathToManifestFile = repoPath + manifesFilePath

  val builderFactory = DocumentBuilderFactory.newInstance()

  val docBuilder = builderFactory.newDocumentBuilder()

  val doc = docBuilder.parse(File(fullPathToManifestFile))
  doc.getDocumentElement().normalize()

  val completeActivityList = convertNodeListToListOfNode(doc.getElementsByTagName("activity"))

  val activityListWithoutLabel = completeActivityList.filter { node ->
    checkIfActivityHasMissingLabel(node)
  }

  logFailures(activityListWithoutLabel)

  if (activityListWithoutLabel.isNotEmpty()) {
    throw Exception(ACCESSIBILITY_LABEL_CHECK_FAILED_OUTPUT_INDICATOR)
  } else {
    println(ACCESSIBILITY_LABEL_CHECK_PASSED_OUTPUT_INDICATOR)
  }
}

/**
 * Checks whether an activity element has a missing label.
 *
 * @param activityNode instance of Node
 * @return label is present or not
 */
private fun checkIfActivityHasMissingLabel(activityNode: Node): Boolean {
  val attributesList = activityNode.getAttributes()
  val activityPath = attributesList.getNamedItem("android:name").getNodeValue()
  return activityPath !in ScriptExemptions.ACCESSIBILITY_LABEL_CHECK_EXEMPTIONS_LIST &&
    attributesList.getNamedItem("android:label") == null
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
 */
private fun logFailures(matchedNodes: List<Node>) {
  if (matchedNodes.isNotEmpty()) {
    println("Accessiblity labels missing for Activities:")
    matchedNodes.forEach { node ->
      println(
        node.getAttributes()
          .getNamedItem("android:name")
          .getNodeValue()
      )
    }
    println()
  }
}
