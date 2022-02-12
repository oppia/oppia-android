package org.oppia.android.scripts.xml

import org.oppia.android.scripts.common.RepositoryFile
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Node

fun main(vararg args: String) {
  // Path of the repo to be analyzed.
  val repoPath = "${args[0]}/"

  // A list of all XML files in the repo to be analyzed.
  val searchFiles = RepositoryFile.collectSearchFiles(
    repoPath = repoPath, expectedExtension = ".xml"
  )
  val profileEditFragmentFile = searchFiles[452]
  val builderFactory = DocumentBuilderFactory.newInstance()
  val builder = builderFactory.newDocumentBuilder()
  val doc = builder.parse(profileEditFragmentFile)
  val root = doc.getElementsByTagName("TextView")
  for (i in 0 until root.length) {
    val node = root.item(i)
    textViewRTL(node as Node)
  }
}

private fun textViewRTL(node: Node) {
  val attributes = node.attributes
  for (i in 0 until attributes.length) {
    val attribute = attributes.item(i)
    when (attribute.nodeName) {
      "android:layout_marginLeft" -> {
        throw Exception("TextView has layout_marginLeft")
      }
      "android:layout_marginRight" -> {
        throw Exception("TextView has layout_marginRight")
      }
      "android:layout_paddingLeft" -> {
        throw Exception("TextView has layout_paddingLeft")
      }
      "android:layout_paddingRight" -> {
        throw Exception("TextView has layout_paddingRight")
      }
    }
  }
}
