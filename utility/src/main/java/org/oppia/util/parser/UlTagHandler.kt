package org.oppia.util.parser

import android.text.Editable
import android.text.Html
import android.text.Html.TagHandler
import org.xml.sax.XMLReader

class UlTagHandler : Html.TagHandler {
 override fun handleTag(
    opening: Boolean, tag: String, output: Editable,
    xmlReader: XMLReader
  ) {
    if (tag == "ul" && !opening) output.append("\n")
    if (tag == "li" && opening) output.append("\n\t• ")
  }
}
