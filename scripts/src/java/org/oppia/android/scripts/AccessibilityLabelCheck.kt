package org.oppia.android.scripts

import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.util.stream.Collectors
import java.util.stream.IntStream
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Script for ensuring that all the Activities the
 * repo are defined with accessibility labels.
 */
class AccessibilityLabelCheck {
  companion object {
    @JvmStatic
    fun main(vararg args: String) {
      val repoPath = args[0] + "/"

      val manifesFilePath = args[1]

      val fullPathToManifestFile = repoPath + manifesFilePath

      val accessibilityLabelCheck: AccessibilityLabelCheck = AccessibilityLabelCheck()

      val builderFactory = DocumentBuilderFactory.newInstance()

      val docBuilder = builderFactory.newDocumentBuilder()

      val doc = docBuilder.parse(File(fullPathToManifestFile))
      doc.getDocumentElement().normalize()

      val completeActivityList = accessibilityLabelCheck.convertNodeListToListOfNode(
        doc.getElementsByTagName("activity")
      )

      val activityListWithoutLabel = completeActivityList.filter { node ->
        accessibilityLabelCheck.checkIfActivityHasMissingLabel(node)
      }

      accessibilityLabelCheck.logFailures(activityListWithoutLabel)

      if (activityListWithoutLabel.size != 0) {
        throw Exception(ScriptResultConstants.ACCESSIBILITY_LABEL_CHECK_FAILED)
      } else {
        println(ScriptResultConstants.ACCESSIBILITY_LABEL_CHECK_PASSED)
      }
    }
  }

  /**
   * Checks whether a activity element has a missing label.
   *
   * @param activityNode instance of Node
   * @return label is present or not
   */
  private fun checkIfActivityHasMissingLabel(activityNode: Node): Boolean {
    val attributesList = activityNode.getAttributes()
    val activityPath = attributesList.getNamedItem("android:name").getNodeValue()
    return activityPath !in ExemptionsList.ACCESSIBILITY_LABEL_CHECK_EXEMPTIONS_LIST &&
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
   * @return log the failures
   */
  private fun logFailures(
    matchedNodes: List<Node>
  ) {
    if (matchedNodes.size != 0) {
      println("Accessiblity labels missing for Activities:")
      matchedNodes.forEach { node ->
        println(node.getAttributes().getNamedItem("android:name").getNodeValue())
      }
      println()
    }
  }
}
