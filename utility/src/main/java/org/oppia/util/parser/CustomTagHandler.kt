package org.oppia.util.parser

import android.content.Context
import android.text.Editable
import android.text.Html
import org.oppia.util.parser.StringUtils.LI_TAG
import org.oppia.util.parser.StringUtils.OL_TAG
import org.oppia.util.parser.StringUtils.UL_TAG
import org.xml.sax.XMLReader
import java.util.*

/**
 * Implements support for ordered ({@code <ol>}) and unordered ({@code <ul>}) lists in to Android TextView.
 *
 * <ul> and <ol> tags are added to the [listParents] array and removed when the closing tag is reached.
 *
 * <li> tags are handled corresponding to the parent tag.
 */
class CustomTagHandler(private val context: Context) : Html.TagHandler {
  private var listItemCount = 0
  private val listParents: Vector<String> = Vector<String>()
  private val listCounter: Vector<Int> = Vector<Int>()
  override fun handleTag(
    opening: Boolean,
    tag: String,
    output: Editable,
    xmlReader: XMLReader?
  ) {
    if (tag == UL_TAG || tag == OL_TAG) {
      if (opening) {
        listParents.add(listParents.size, tag)
        listCounter.add(listCounter.size, 0)
      } else {
        listParents.removeElementAt(listParents.size - 1)
        listCounter.removeElementAt(listCounter.size - 1)
      }
    } else if (tag == LI_TAG && opening) {
      handleListTag(output)
    }
  }

  private fun handleListTag(output: Editable) {
    if (listParents.lastElement().equals(UL_TAG)) {
      if (output.length != 0)
        output.append("\n")
      for (i in 1 until listCounter.size) output.append("\t")
      output.append("● ")
    } else if (listParents.lastElement().equals(OL_TAG)) {
      listItemCount = listCounter.lastElement() + 1
      if (output.length != 0)
        output.append("\n")
      for (i in 1 until listCounter.size) output.append("\t")
      output.append("$listItemCount. ")
      listCounter.removeElementAt(listCounter.size - 1)
      listCounter.add(listCounter.size, listItemCount)
    }
  }
}
