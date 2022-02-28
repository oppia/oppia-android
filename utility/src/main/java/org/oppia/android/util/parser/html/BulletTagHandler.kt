package org.oppia.android.util.parser.html

import android.content.Context
import android.text.Editable
import android.text.Spannable
import android.text.Spanned

/** The custom tag corresponding to [BulletTagHandler]. */
const val CUSTOM_BULLET_LIST_TAG = "oppia-li"
const val CUSTOM_BULLET_UL_LIST_TAG = "oppia-ul"
const val CUSTOM_BULLET_OL_LIST_TAG = "oppia-ol"

/**
 * A custom tag handler for properly formatting bullet items in HTML parsed with
 * [CustomHtmlContentHandler].
 */
class BulletTagHandler(private val context: Context, private val tag: String) :
  CustomHtmlContentHandler.CustomTagHandler {

  private var index = 1

  override fun handleOpeningTag(output: Editable) {
    appendNewLine(output)
    when (tag) {
      CUSTOM_BULLET_UL_LIST_TAG -> {
        start(output, BulletListItem())
      }
      CUSTOM_BULLET_OL_LIST_TAG -> {
        start(output, NumberListItem(index))
        index++
      }
    }
  }

  override fun handleClosingTag(output: Editable, indentation: Int) {
    appendNewLine(output)

    when (tag) {
      CUSTOM_BULLET_UL_LIST_TAG -> {
        getLast<BulletListItem>(output)?.let { mark ->
          setSpanFromMark(output, mark, CustomBulletSpan(context, indentation, "•", tag))
        }
      }
      CUSTOM_BULLET_OL_LIST_TAG -> {
        getLast<NumberListItem>(output)?.let { mark ->
          setSpanFromMark(
            output,
            mark,
            CustomBulletSpan(context, indentation, "${mark.number}.", tag)
          )
        }
      }
    }
  }

  /**
   * These static methods are based on the Android Html class source code.
   * https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/text/Html.java
   */
  companion object {
    /**
     * Appends a new line to [text] if it doesn't already end in a new line.
     * We want the first line of list item to start on a separate line, and other content that comes
     * after the list to also be on a separate line. We avoid appending multiple newline characters
     * in a row by first checking if the last character in output is \n.
     * https://medium.com/swlh/making-nested-lists-with-android-spannables-in-kotlin-4ad00052912c
     */
    private fun appendNewLine(text: Editable) {
      if (text.isNotEmpty() && text.last() != '\n') {
        text.append("\n")
      }
    }

    /**
     * Returns the most recently added span of type [T] in [text].
     *
     * Invisible marking spans are inserted to record the location of opening HTML tags in the text.
     * We do this rather than using a stack in case text is inserted and the relative location shifts around.
     *
     * The last span corresponds to the top of the "stack".
     */
    private inline fun <reified T : Mark> getLast(text: Spanned) =
      text.getSpans(0, text.length, T::class.java).lastOrNull()

    /**
     * Pops out the invisible [mark] span and uses it to get the opening tag location.
     * Then, sets a span from the opening tag position to closing tag position.
     */
    private fun setSpanFromMark(text: Spannable, mark: Mark, styleSpan: Any) {
      // Find the location where the mark is inserted in the string.
      val markerLocation = text.getSpanStart(mark)
      // Remove the mark now that the location is saved
      text.removeSpan(mark)

      val end = text.length
      if (markerLocation != end) {
        text.setSpan(styleSpan, markerLocation, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
      }
    }

    /**
     * Inserts an invisible [mark] span that doesn't do any styling.
     * Instead, [setSpanFromMark] will later find the location of this span so it knows where the opening tag was.
     */
    private fun start(text: Spannable, mark: Mark) {
      val currentPosition = text.length
      text.setSpan(mark, currentPosition, currentPosition, Spanned.SPAN_MARK_MARK)
    }
  }
}
