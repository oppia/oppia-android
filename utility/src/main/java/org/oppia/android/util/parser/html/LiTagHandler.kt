package org.oppia.android.util.parser.html

import android.content.Context
import android.text.Editable
import android.text.Spannable
import android.text.Spanned
import org.oppia.android.util.locale.OppiaLocale
import org.xml.sax.Attributes
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
  private val displayLocale: OppiaLocale.DisplayLocale
) : CustomHtmlContentHandler.CustomTagHandler, CustomHtmlContentHandler.ContentDescriptionProvider {
  private val pendingLists = Stack<ListTag<*, *>>()
  private val latestPendingList: ListTag<*, *>?
    get() = pendingLists.lastOrNull()

  override fun handleOpeningTag(output: Editable, tag: String) {
    when (tag) {
      CUSTOM_LIST_UL_TAG -> {
        pendingLists += ListTag.Ul(
          parentList = latestPendingList,
          parentMark = latestPendingList?.pendingStartMark,
          indentationLevel = pendingLists.size
        )
      }
      CUSTOM_LIST_OL_TAG -> {
        pendingLists += ListTag.Ol(
          parentList = latestPendingList, parentMark = latestPendingList?.pendingStartMark
        )
      }
      CUSTOM_LIST_LI_TAG -> latestPendingList?.openItem(output)
    }
  }

  override fun handleClosingTag(output: Editable, indentation: Int, tag: String) {
    when (tag) {
      CUSTOM_LIST_UL_TAG, CUSTOM_LIST_OL_TAG -> {
        // Actually place the spans only if the root tree has been finished (as the entirety of the
        // tree is needed for analysis).
        val closingList = pendingLists.pop().also { it.recordList() }
        if (pendingLists.isEmpty()) closingList.finishListTree(output, context, displayLocale)
      }
      CUSTOM_LIST_LI_TAG -> latestPendingList?.closeItem(output)
    }
  }

  private sealed class ListTag<M : Mark<S>, S : ListItemLeadingMarginSpan>(
    private val parentList: ListTag<*, *>?,
    private val parentMark: Mark<*>?,
    private val fetchLastMark: Editable.() -> M?
  ) {
    private val markRangesToReplace = mutableListOf<MarkedRange<S>>()
    private val childrenLists = mutableMapOf<Mark<*>, ListTag<*, *>>()

    /**
     * The mark of the current item being processed for this list, if any. This is null when the
     * most recent encountered item has been finished, or if no item has yet been encountered for
     * this list.
     */
    var pendingStartMark: M? = null
    private var itemCount = 0

    /**
     * Called when an opening <li> tag is encountered.
     *
     * Inserts an invisible [ListItemMark] span that doesn't do any styling.
     * Instead, [closeItem] will later find the location of this span so it knows where the opening
     * tag was.
     */
    fun openItem(text: Editable) {
      check(pendingStartMark == null) { "Trying open new item when one is already pending." }
      text.appendNewLine()
      text.addMark(createMark(itemNumber = ++itemCount).also { pendingStartMark = it })
    }

    /**
     * Called when a closing </li> tag is encountered.
     *
     * Pops out the invisible [ListItemMark] span and uses it to get the opening tag location.
     * Then, sets a [ListItemLeadingMarginSpan] from the opening tag position to closing tag
     * position.
     */
    fun closeItem(text: Editable) {
      val startingMark =
        checkNotNull(pendingStartMark) { "Cannot close item that hasn't been started." }
      val endingMark = Mark.EndListItem()
      text.appendNewLine()
      text.addMark(endingMark)
      markRangesToReplace += MarkedRange(startingMark, endingMark)
      pendingStartMark = null
    }

    /**
     * Records this tag as a list in this tag's parent, if it has one.
     *
     * A call to this function is mandatory to ensure that list hierarchies are correctly tracked
     * for later margin calculations.
     *
     * Note that this does not actually change the original text (a call to [finishListTree] is
     * needed to do that once all list & item tags have been processed and then recorded using this
     * function).
     */
    fun recordList() {
      parentMark?.let { mark -> parentList?.childrenLists?.put(mark, this) }
    }

    /**
     * Recursively replaces all marks for this root list (and all its children) with renderable
     * spans in the provided [text].
     */
    fun finishListTree(text: Editable, context: Context, displayLocale: OppiaLocale.DisplayLocale) =
      finishListRecursively(parentSpan = null, text, context, displayLocale)

    /**
     * Returns a new mark of type [M] for this tag.
     *
     * @param itemNumber the number in the list that the returned mark represents
     */
    protected abstract fun createMark(itemNumber: Int): M

    private fun finishListRecursively(
      parentSpan: ListItemLeadingMarginSpan?,
      text: Editable,
      context: Context,
      displayLocale: OppiaLocale.DisplayLocale
    ) {
      val childrenToProcess = childrenLists.toMutableMap()
      markRangesToReplace.forEach { (startMark, endMark) ->
        val styledSpan = startMark.toSpan(
          parentSpan, context, displayLocale, peerItemCount = markRangesToReplace.size
        )
        text.replaceMarksWithSpan(startMark, endMark, styledSpan)
        childrenToProcess.remove(startMark)?.finishListRecursively(
          parentSpan = styledSpan, text, context, displayLocale
        )
      }

      // Process the remaining children that are not lists themselves.
      childrenToProcess.values.forEach {
        it.finishListRecursively(parentSpan = null, text, context, displayLocale)
      }
    }

    /** [ListTag] for unordered lists. */
    class Ul(
      parentList: ListTag<*, *>?,
      parentMark: Mark<*>?,
      private val indentationLevel: Int
    ) : ListTag<Mark.BulletListItem, ListItemLeadingMarginSpan.UlSpan>(
      parentList, parentMark, ::getLast
    ) {
      override fun createMark(itemNumber: Int) = Mark.BulletListItem(indentationLevel)
    }

    /** [ListTag] for ordered lists. */
    class Ol(
      parentList: ListTag<*, *>?,
      parentMark: Mark<*>?
    ) : ListTag<Mark.NumberListItem, ListItemLeadingMarginSpan.OlSpan>(
      parentList, parentMark, ::getLast
    ) {
      override fun createMark(itemNumber: Int) = Mark.NumberListItem(itemNumber)
    }
  }

  private data class MarkedRange<S : ListItemLeadingMarginSpan>(
    val startMark: Mark<S>,
    val endMark: Mark.EndListItem
  )

  private sealed class Mark<S : ListItemLeadingMarginSpan> {
    /** Returns a span representation of type [S] of this mark (for eventual rendering). */
    abstract fun toSpan(
      parentSpan: ListItemLeadingMarginSpan?,
      context: Context,
      displayLocale: OppiaLocale.DisplayLocale,
      peerItemCount: Int
    ): S

    /** Marks the opening tag location of a list item inside an <ul> element. */
    class BulletListItem(
      private val indentationLevel: Int
    ) : Mark<ListItemLeadingMarginSpan.UlSpan>() {
      override fun equals(other: Any?) = this === other
      override fun hashCode() = System.identityHashCode(this)

      override fun toSpan(
        parentSpan: ListItemLeadingMarginSpan?,
        context: Context,
        displayLocale: OppiaLocale.DisplayLocale,
        peerItemCount: Int
      ) = ListItemLeadingMarginSpan.UlSpan(parentSpan, context, indentationLevel, displayLocale)
    }

    /** Marks the opening tag location of a list item inside an <ol> element. */
    class NumberListItem(val number: Int) : Mark<ListItemLeadingMarginSpan.OlSpan>() {
      override fun equals(other: Any?) = this === other
      override fun hashCode() = System.identityHashCode(this)

      override fun toSpan(
        parentSpan: ListItemLeadingMarginSpan?,
        context: Context,
        displayLocale: OppiaLocale.DisplayLocale,
        peerItemCount: Int
      ): ListItemLeadingMarginSpan.OlSpan {
        return ListItemLeadingMarginSpan.OlSpan(
          parentSpan,
          context,
          numberedItemPrefix = "${displayLocale.toHumanReadableString(number)}.",
          longestNumberedItemPrefix = "${displayLocale.toHumanReadableString(peerItemCount)}.",
          displayLocale
        )
      }
    }

    /** A [Mark] that indicates the end of a list item in a developing spannable. */
    class EndListItem : Mark<ListItemLeadingMarginSpan>() {
      override fun equals(other: Any?) = this === other
      override fun hashCode() = System.identityHashCode(this)

      override fun toSpan(
        parentSpan: ListItemLeadingMarginSpan?,
        context: Context,
        displayLocale: OppiaLocale.DisplayLocale,
        peerItemCount: Int
      ) = error("Ending marks cannot be converted to spans.")
    }
  }

  /**
   * These static methods are based on the Android Html class source code.
   * https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/text/Html.java
   */
  companion object {
    /**
     * Appends a new line to [this] if it doesn't already end in a new line.
     * We want the first line of list item to start on a separate line, and other content that comes
     * after the list to also be on a separate line. We avoid appending multiple newline characters
     * in a row by first checking if the last character in output is \n.
     * https://medium.com/swlh/making-nested-lists-with-android-spannables-in-kotlin-4ad00052912c
     */
    private fun Editable.appendNewLine() {
      if (isNotEmpty() && last() != '\n') append("\n")
    }

    /**
     * Returns the most recently added span of type [T] in [text].
     *
     * Invisible marking spans are inserted to record the location of opening HTML tags in the text.
     * We do this rather than using a stack in case text is inserted and the relative location
     * shifts around.
     *
     * The last span corresponds to the top of the "stack".
     */
    private inline fun <reified T> getLast(text: Spanned) =
      text.getSpans(0, text.length, T::class.java).lastOrNull()

    /** Replaces the provided [startMark] from the [this] with the [styleSpan] span. */
    private fun <T : Mark<S>, S : ListItemLeadingMarginSpan> Spannable.replaceMarksWithSpan(
      startMark: T,
      endMark: Mark.EndListItem,
      styleSpan: S
    ) {
      // Find the range denoted by marks, remove the mark, then add the span to the same locations.
      val startMarkLocation = getSpanStart(startMark).also { removeSpan(startMark) }
      val endMarkLocation = getSpanEnd(endMark).also { removeSpan(endMark) }
      val validMarkRange = 0..length
      if (startMarkLocation in validMarkRange && endMarkLocation in validMarkRange) {
        setSpan(styleSpan, startMarkLocation, endMarkLocation, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
      }
    }

    /**
     * Inserts an invisible [Mark] that doesn't do any styling. Instead, [replaceMarksWithSpan] will
     * later find the location of this span so it knows where the opening tag was.
     */
    private fun <T : Mark<*>> Spannable.addMark(mark: T) =
      setSpan(mark, length, length, Spanned.SPAN_MARK_MARK)
  }

  override fun getContentDescription(attributes: Attributes): String {
    return ""
  }
}
