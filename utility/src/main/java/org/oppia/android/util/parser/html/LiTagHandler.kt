package org.oppia.android.util.parser.html

import android.content.Context
import android.text.Editable
import android.text.Spannable
import android.text.Spanned
import org.oppia.android.util.locale.OppiaLocale
import java.util.Stack

/** The custom <li> tag corresponding to [LiTagHandler]. */
const val CUSTOM_LIST_LI_TAG = "oppia-li"

/** The custom <ul> tag corresponding to [LiTagHandler]. */
const val CUSTOM_LIST_UL_TAG = "oppia-ul"

/** The custom <ol> tag corresponding to [LiTagHandler]. */
const val CUSTOM_LIST_OL_TAG = "oppia-ol"

/**
 * A custom tag handler for properly formatting bullet items in HTML parsed with
 * [CustomHtmlContentHandler].
 */
class LiTagHandler(
  private val context: Context,
  private val machineLocale: OppiaLocale.MachineLocale
) :
  CustomHtmlContentHandler.CustomTagHandler {

  override fun handleOpeningTag(
    output: Editable,
    tag: String,
    lists: Stack<CustomHtmlContentHandler.ListTag>
  ) {
    when (tag) {
      CUSTOM_LIST_UL_TAG ->
        lists.push(Ul(context, tag))

      CUSTOM_LIST_OL_TAG ->
        lists.push(Ol(context, tag, machineLocale))

      CUSTOM_LIST_LI_TAG ->
        lists.peek().openItem(output)

    }
  }

  override fun handleClosingTag(
    output: Editable,
    indentation: Int,
    tag: String,
    lists: Stack<CustomHtmlContentHandler.ListTag>
  ) {
    when (tag) {
      CUSTOM_LIST_UL_TAG ->
        lists.pop()

      CUSTOM_LIST_OL_TAG ->
        lists.pop()

      CUSTOM_LIST_LI_TAG ->
        lists.peek().closeItem(output, indentation = lists.size - 1)
    }
  }

  /**
   * Subclass of [ListTag] for unordered lists.
   */
  private class Ul(private val context: Context, private val tag: String) :
    CustomHtmlContentHandler.ListTag {

    override fun openItem(text: Editable) {
      appendNewLine(text)
      start(text, BulletListItem())
    }

    override fun closeItem(text: Editable, indentation: Int) {
      appendNewLine(text)

      getLast<BulletListItem>(text)?.let { mark ->
        setSpanFromMark(text, mark, ListItemLeadingMarginSpan(context, indentation, "•", tag))
      }
    }
  }

  /**
   * Subclass of [ListTag] for ordered lists.
   */
  private class Ol(
    private val context: Context,
    private val tag: String,
    private val machineLocale: OppiaLocale.MachineLocale
  ) : CustomHtmlContentHandler.ListTag {

    private var index = 1

    override fun openItem(text: Editable) {
      appendNewLine(text)
      start(text, NumberListItem(index))
      index++
    }

    override fun closeItem(text: Editable, indentation: Int) {
      appendNewLine(text)

      getLast<NumberListItem>(text)?.let { mark ->
        setSpanFromMark(
          text, mark,
          ListItemLeadingMarginSpan(
            context,
            indentation,
            "${machineLocale.numberFormatter(mark.number)}.",
            tag
          )
        )
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
    private inline fun <reified T : ListItemMark> getLast(text: Spanned) =
      text.getSpans(0, text.length, T::class.java).lastOrNull()

    /**
     * Pops out the invisible [listItemMark] span and uses it to get the opening tag location.
     * Then, sets a span from the opening tag position to closing tag position.
     */
    private fun setSpanFromMark(text: Spannable, listItemMark: ListItemMark, styleSpan: Any) {
      // Find the location where the mark is inserted in the string.
      val markerLocation = text.getSpanStart(listItemMark)
      // Remove the mark now that the location is saved
      text.removeSpan(listItemMark)

      val end = text.length
      if (markerLocation != end) {
        text.setSpan(styleSpan, markerLocation, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
      }
    }

    /**
     * Inserts an invisible [listItemMark] span that doesn't do any styling.
     * Instead, [setSpanFromMark] will later find the location of this span so it knows where the opening tag was.
     */
    private fun start(text: Spannable, listItemMark: ListItemMark) {
      val currentPosition = text.length
      text.setSpan(listItemMark, currentPosition, currentPosition, Spanned.SPAN_MARK_MARK)
    }
  }
}
