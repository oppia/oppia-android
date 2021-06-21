package org.oppia.android.scripts

import javax.xml.parsers.DocumentBuilderFactory
import java.io.File
import org.oppia.android.scripts.ScriptResultConstants
import org.oppia.android.scripts.ExemptionsList

class AccessibilityLabelCheck {
  companion object {
    @JvmStatic
    fun main(vararg args: String) {
      val repoPath = args[0] + "/"

      val manifesFilePath = args[1]

      val fullPathToManifestFile = repoPath + manifesFilePath

      val builderFactory = DocumentBuilderFactory.newInstance()

      val docBuilder = builderFactory.newDocumentBuilder()

      var scriptFailedFlag = false

      val doc = docBuilder.parse(File(fullPathToManifestFile))
      doc.getDocumentElement().normalize()

      val nList = doc.getElementsByTagName("activity")

      for (i in 1 until nList.length) {
        val attributesList = nList.item(i).getAttributes()
        val activityPath = attributesList.getNamedItem("android:name").getNodeValue()

        if (activityPath !in ExemptionsList.ACCESSIBILITY_LABEL_CHECK_EXEMPTIONS_LIST) {

          if (attributesList.getNamedItem("android:label") == null) {
            println("No accessibility label found for: $activityPath\n")
            scriptFailedFlag = true
          }
        }
      }

      if (scriptFailedFlag) {
        throw Exception(ScriptResultConstants.ACCESSIBILITY_LABEL_CHECK_FAILED)
      } else {
        println(ScriptResultConstants.ACCESSIBILITY_LABEL_CHECK_PASSED)
      }
    }
  }
}

