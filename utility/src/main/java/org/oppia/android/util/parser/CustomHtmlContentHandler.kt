package org.oppia.android.util.parser

import android.text.Editable
import android.text.Html
import android.text.Spannable
import androidx.core.text.HtmlCompat
import org.xml.sax.Attributes
import org.xml.sax.ContentHandler
import org.xml.sax.Locator
import org.xml.sax.XMLReader
import java.util.ArrayDeque

/**
 * A custom [ContentHandler] and [Html.TagHandler] for processing custom HTML tags. This class must
 * be used if a custom tag attribute must be parsed.
 *
 * This is based on the implementation provided in https://stackoverflow.com/a/36528149.
 */
class CustomHtmlContentHandler private constructor(
  private val customTagHandlers: Map<String, CustomTagHandler>
) : ContentHandler, Html.TagHandler {
  private var originalContentHandler: ContentHandler? = null
  private var currentTrackedTag: TrackedTag? = null
  private val currentTrackedCustomTags = ArrayDeque<TrackedCustomTag>()

  override fun endElement(uri: String?, localName: String?, qName: String?) {
    originalContentHandler?.endElement(uri, localName, qName)
    currentTrackedTag = null
  }

  override fun processingInstruction(target: String?, data: String?) {
    originalContentHandler?.processingInstruction(target, data)
  }

  override fun startPrefixMapping(prefix: String?, uri: String?) {
    originalContentHandler?.startPrefixMapping(prefix, uri)
  }

  override fun ignorableWhitespace(ch: CharArray?, start: Int, length: Int) {
    originalContentHandler?.ignorableWhitespace(ch, start, length)
  }

  override fun characters(ch: CharArray?, start: Int, length: Int) {
    originalContentHandler?.characters(ch, start, length)
  }

  override fun endDocument() {
    originalContentHandler?.endDocument()
  }

  override fun startElement(uri: String?, localName: String?, qName: String?, atts: Attributes?) {
    // Defer custom tag management to the tag handler so that Android's element parsing takes
    // precedence.
    currentTrackedTag = TrackedTag(checkNotNull(localName), checkNotNull(atts))
    originalContentHandler?.startElement(uri, localName, qName, atts)
  }

  override fun skippedEntity(name: String?) {
    originalContentHandler?.skippedEntity(name)
  }

  override fun setDocumentLocator(locator: Locator?) {
    originalContentHandler?.setDocumentLocator(locator)
  }

  override fun endPrefixMapping(prefix: String?) {
    originalContentHandler?.endPrefixMapping(prefix)
  }

  override fun startDocument() {
    originalContentHandler?.startDocument()
  }

  override fun handleTag(opening: Boolean, tag: String?, output: Editable?, xmlReader: XMLReader?) {
    check(output != null) { "Expected non-null editable." }
    when {
      originalContentHandler == null -> {
        check(tag == "init-custom-handler") {
          "Expected first custom tag to be initializing the custom handler."
        }
        checkNotNull(xmlReader) { "Expected reader to not be null" }
        originalContentHandler = xmlReader.contentHandler
        xmlReader.contentHandler = this
      }
      opening -> {
        if (tag in customTagHandlers) {
          val localCurrentTrackedTag = currentTrackedTag
          check(localCurrentTrackedTag != null) {
            "Expected tag details to be to be cached for current tag."
          }
          check(localCurrentTrackedTag.tag == tag) {
            "Expected tracked tag $currentTrackedTag to match custom tag: $tag"
          }
          currentTrackedCustomTags += TrackedCustomTag(
            localCurrentTrackedTag.tag, localCurrentTrackedTag.attributes, output.length
          )
          customTagHandlers.getValue(tag).handleOpeningTag(output)
        }
      }
      tag in customTagHandlers -> {
        check(currentTrackedCustomTags.isNotEmpty()) {
          "Expected tracked custom tag to be initialized."
        }
        val currentTrackedCustomTag = currentTrackedCustomTags.removeLast()
        check(currentTrackedCustomTag.tag == tag) {
          "Expected tracked tag $currentTrackedTag to match custom tag: $tag"
        }
        val (_, attributes, openTagIndex) = currentTrackedCustomTag
        customTagHandlers.getValue(tag).handleClosingTag(output)
        customTagHandlers.getValue(tag).handleTag(attributes, openTagIndex, output.length, output)
      }
    }
  }

  private data class TrackedTag(val tag: String, val attributes: Attributes)
  private data class TrackedCustomTag(
    val tag: String,
    val attributes: Attributes,
    val openTagIndex: Int
  )

  /** Handler interface for a custom tag and its attributes. */
  interface CustomTagHandler {
    /**
     * Called when a custom tag is encountered. This is always called after the closing tag.
     *
     * @param attributes the tag's attributes
     * @param openIndex the index in the output [Editable] at which this tag begins
     * @param closeIndex the index in the output [Editable] at which this tag ends
     * @param output the destination [Editable] to which spans can be added
     */
    fun handleTag(attributes: Attributes, openIndex: Int, closeIndex: Int, output: Editable) {}

    /**
     * Called when the opening of a custom tag is encountered. This does not support processing
     * attributes of the tag--[handleTag] should be used, instead.
     *
     * This function will always be called before [handleClosingTag].
     *
     * @param output the destination [Editable] to which spans can be added
     */
    fun handleOpeningTag(output: Editable) {}

    /**
     * Called when the closing of a custom tag is encountered. This does not support processing
     * attributes of the tag--[handleTag] should be used, instead.
     *
     * This function will always be called before [handleClosingTag].
     *
     * @param output the destination [Editable] to which spans can be added
     */
    fun handleClosingTag(output: Editable) {}
  }

  companion object {
    /**
     * Returns a new [Spannable] with HTML parsed from [html] using the specified [imageGetter] for
     * handling image retrieval, and map of tags to [CustomTagHandler]s for handling custom tags.
     * All possible custom tags must be registered in the [customTagHandlers] map.
     */
    fun fromHtml(
      html: String,
      imageGetter: Html.ImageGetter,
      customTagHandlers: Map<String, CustomTagHandler>
    ): Spannable {
      // Adjust the HTML to allow the custom content handler to properly initialize custom tag
      // tracking.
      return HtmlCompat.fromHtml(
        "<init-custom-handler/>$html",
        HtmlCompat.FROM_HTML_MODE_LEGACY,
        imageGetter,
        CustomHtmlContentHandler(customTagHandlers)
      ) as Spannable
    }
  }
}
