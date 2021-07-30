package org.oppia.android.util.parser.html

import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.Html
import android.text.Spannable
import androidx.core.text.HtmlCompat
import org.json.JSONException
import org.json.JSONObject
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
  private val customTagHandlers: Map<String, CustomTagHandler>,
  private val imageRetriever: ImageRetriever
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
        customTagHandlers.getValue(tag)
          .handleTag(attributes, openTagIndex, output.length, output, imageRetriever)
        customTagHandlers.getValue(tag)
          .handleContentDescription(attributes, openTagIndex, output.length, output)
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
     * @param imageGetter a utility to load image drawables if needed by the handler
     */
    fun handleTag(
      attributes: Attributes,
      openIndex: Int,
      closeIndex: Int,
      output: Editable,
      imageRetriever: ImageRetriever
    ) {
    }

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

    /**
     * Called when a custom tag is encountered. This is always called after the closing tag.
     *
     * @param attributes the tag's attributes
     * @param openIndex the index in the output [Editable] at which this tag begins
     * @param closeIndex the index in the output [Editable] at which this tag ends
     * @param output the destination [Editable] to which spans can be added
     */
    fun handleContentDescription(
      attributes: Attributes,
      openIndex: Int,
      closeIndex: Int,
      output: Editable
    ) {}
  }

  /**
   * Retriever of images for custom tag handlers. The built-in Android analog for this class is
   * Html's ImageGetter.
   */
  interface ImageRetriever {
    /** Returns a new [Drawable] corresponding to the specified image filename and [Type]. */
    fun loadDrawable(filename: String, type: Type): Drawable

    /** Corresponds to the types of images that can be retrieved. */
    enum class Type {
      /**
       * Corresponds to an image that can be rendered in-line (such as LaTeX). Only SVGs are
       * currently supported.
       */
      INLINE_TEXT_IMAGE,
      /**
       * Corresponds to a block image that should be positioned in a way that may break text, and
       * potentially centered depending on the configuration of the implementation.
       */
      BLOCK_IMAGE
    }
  }

  companion object {
    /**
     * Returns a new [Spannable] with HTML parsed from [html] using the specified [imageRetriever]
     * for handling image retrieval, and map of tags to [CustomTagHandler]s for handling custom
     * tags. All possible custom tags must be registered in the [customTagHandlers] map.
     */
    fun <T> fromHtml(
      html: String,
      imageRetriever: T,
      customTagHandlers: Map<String, CustomTagHandler>
    ): Spannable where T : Html.ImageGetter, T : ImageRetriever {
      // Adjust the HTML to allow the custom content handler to properly initialize custom tag
      // tracking.
      return HtmlCompat.fromHtml(
        "<init-custom-handler/>$html",
        HtmlCompat.FROM_HTML_MODE_LEGACY,
        imageRetriever,
        CustomHtmlContentHandler(customTagHandlers, imageRetriever),
      ) as Spannable
    }
  }
}

/**
 * Returns a string value from this [Attributes] object, but interpreted as a JSON string, or null
 * if the corresponding value doesn't exist in this attributes map.
 */
fun Attributes.getJsonStringValue(name: String): String? {
  // Note that the attribute is actually an encoded JSON string (so it has escaped quotes around
  // it). Since it's only a source string, the quotes can simply be removed in order to extract
  // the string value.
  return getValue(name)?.replace("&quot;", "")
}

/**
 * Returns a [JSONObject] value from this [Attributes] object that was encoded as a string, or null
 * if the corresponding value cannot be interpreted as a JSON object (or doesn't exist).
 */
fun Attributes.getJsonObjectValue(name: String): JSONObject? {
  // The raw content value is a JSON blob with escaped quotes.
  return try {
    getValue(name)?.replace("&quot;", "\"")?.let { JSONObject(it) }
  } catch (e: JSONException) { return null }
}
