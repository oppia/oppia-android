package org.oppia.util.parser

import android.text.Editable
import android.text.Html
import android.util.Log
import org.oppia.util.parser.StringUtils.LI_TAG
import org.oppia.util.parser.StringUtils.OL_TAG
import org.oppia.util.parser.StringUtils.UL_TAG
import org.xml.sax.XMLReader

// TODO(#562): Add screenshot tests to check whether the drawing logic works correctly on all devices.
/**
 * Implements support for ordered ({@code <ol>}) and unordered ({@code <ul>}) lists in to Android TextView.
 *
 * <ul> and <ol> tags are added to the [listParents] array and removed when the closing tag is reached.
 *
 * <li> tags are handled corresponding to the parent tag.
 *
 * Reference: https://github.com/daphliu/android-spannable-list-sample/tree/master/app/src/main/java/com/daphneliu/sample/listspansample
 */
class CustomTagHandler() : Html.TagHandler {
  private val listParents = mutableListOf<String>()
  private val listCounter = mutableListOf<Int>()

  override fun handleTag(
    opening: Boolean,
    tag: String,
    output: Editable,
    xmlReader: XMLReader?
  ) {
    when {
      tag == UL_TAG || tag == OL_TAG -> {
        if (opening) {
          listParents.add(listParents.size, tag)
          listCounter.add(listCounter.size, 0)
        } else {
          listParents.removeAt(listParents.size - 1)
          listCounter.removeAt(listCounter.size - 1)
        }
      }
      tag == LI_TAG && opening -> {
        handleListTag(output)
      }
    }
  }

  private fun handleListTag(output: Editable) {
    try {
      var listItemCount = 0
      when (listParents.last()) {
        UL_TAG -> {
          ensureEndsWithNewLine(output)
          output.append("● ")
        }
        OL_TAG -> {
          listItemCount = listCounter.last() + 1
          ensureEndsWithNewLine(output)
          output.append("$listItemCount. ")
          listCounter.removeAt(listCounter.size - 1)
          listCounter.add(listCounter.size, listItemCount)
        }
      }
    } catch (e: NoSuchElementException) {
      Log.d("HtmlTagHandler", "No Such Element Found" + listParents.isEmpty())
    }
  }

  private fun ensureEndsWithNewLine(output: Editable) {
    if (output.isNotEmpty()) {
      output.append("\n")
    }
    for (i in 1 until listCounter.size) output.append("\t")
  }
}
