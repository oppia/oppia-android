package org.oppia.android.app.richtext

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ImageSpan
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import org.oppia.android.app.shim.ViewComponentFactory
import org.oppia.android.util.R
import org.oppia.android.util.parser.DeferredImageDrawable
import org.oppia.android.util.parser.HtmlParser
import javax.inject.Inject
import kotlin.math.max

/**
 * A custom TextView that supports Oppia rich-text components. This should be used for all text
 * views in the app that need to support Oppia's custom rich-text elements (including concept card
 * links, embedded images, and others).
 *
 * It's also advisable to use this for any HTML-supporting text views that have embedded images
 * since it will automatically size the images to fit, and supports image binding in a way that does
 * not interfere with data-binding (that is, binding this view's text property will properly handle
 * HTML and embedded image binding).
 */
class RteTextView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyle: Int = android.R.attr.textViewStyle
) : AppCompatTextView(context, attrs, defStyle) {
  @Inject
  lateinit var htmlParserFactory: HtmlParser.Factory

  private val viewLifecycleOwner = ViewLifecycleOwner()
  private val minimumImageSize by lazy {
    context.resources.getDimensionPixelSize(R.dimen.minimum_image_size)
  }
  private val maxContentItemPadding by lazy {
    context.resources.getDimensionPixelSize(R.dimen.maximum_content_item_padding)
  }

  private var supportsConceptCards: Boolean = false
  private var centerAlignImages: Boolean = false
  private var boundRawHtml: CharSequence? = null
  private var drawablesRequireLayOut: Boolean = false
  private var lastViewWidthUsedForDrawables: Int = -1
  private var htmlParser: HtmlParser? = null
  private var customOppiaTagActionListener: HtmlParser.CustomOppiaTagActionListener? = null
  private lateinit var gcsResourceName: String
  private lateinit var entityType: String
  private lateinit var entityId: String

  /**
   * Sets whether this view supports clickable links. Passing true to this will result in links
   * automatically linkifying for bound HTML, including for custom links like concept cards. The
   * default behavior is that links are not linkified.
   */
  fun setSupportsLinks(supportsLinks: Boolean) {
    // Reference: https://stackoverflow.com/a/8662457.
    movementMethod = if (supportsLinks) LinkMovementMethod.getInstance() else null
  }

  /**
   * Sets whether this view supports clickable links. Passing true to this will result in links
   * automatically linkifying for bound HTML, including for custom links like concept cards. The
   * default behavior is false (concept cards are not linkified).
   *
   * Note that [setSupportsLinks] also needs to be set to true, otherwise the concept card tags will
   * be handled by the resulting spans will not actually be clickable by the user.
   */
  fun setSupportsConceptCards(supportsConceptCards: Boolean) {
    this.supportsConceptCards = supportsConceptCards
    reBindHtml()
  }

  /**
   * Sets whether images embedded within this text view should automatically be centered, or else
   * left aligned. The default behavior is false which leads to images, by default, being
   * left-aligned.
   */
  fun setCenterAlignImages(centerAlignImages: Boolean) {
    this.centerAlignImages = centerAlignImages
    reBindHtml()
  }

  // TODO: doc
  fun setCustomOppiaTagActionListener(
    customOppiaTagActionListener: HtmlParser.CustomOppiaTagActionListener
  ) {
    this.customOppiaTagActionListener = customOppiaTagActionListener
    maybeReinitializeHtmlParser()
    rebindHtmlIfGcsIsSetup()
  }

  // TODO: doc
  fun setGcsResourceName(gcsResourceName: String) {
    this.gcsResourceName = gcsResourceName
    maybeReinitializeHtmlParser()
    rebindHtmlIfGcsIsSetup()
  }

  // TODO: doc
  fun setEntityType(entityType: String) {
    this.entityType = entityType
    maybeReinitializeHtmlParser()
    rebindHtmlIfGcsIsSetup()
  }

  // TODO: doc
  fun setEntityId(entityId: String) {
    this.entityId = entityId
    maybeReinitializeHtmlParser()
    rebindHtmlIfGcsIsSetup()
  }

  private fun rebindHtmlIfGcsIsSetup() {
    if (isReadyForHtmlParsing()) {
      reBindHtml()
    }
  }

  private fun maybeReinitializeHtmlParser() {
    if (isReadyForHtmlParsing()) {
      reinitializeHtmlParser()
    }
  }

  // TODO: doc
  private fun isReadyForHtmlParsing(): Boolean {
    return this::gcsResourceName.isInitialized
      && this::entityType.isInitialized
      && this::entityId.isInitialized
  }

  // TODO: simplify
  private fun reinitializeHtmlParser() {
    // TODO: remove imageCenterAlign here since it's unused.
    check(this::gcsResourceName.isInitialized) { "Expected GCS resource name to be set." }
    check(this::entityType.isInitialized) { "Expected GCS entity type to be set." }
    check(this::entityId.isInitialized) { "Expected GCS entity ID to be set." }
    htmlParser = htmlParserFactory.create(gcsResourceName, entityType, entityId, imageCenterAlign = false, customOppiaTagActionListener = customOppiaTagActionListener)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    (FragmentManager.findFragment<Fragment>(this) as ViewComponentFactory)
      .createViewComponent(this)
      .inject(this)
    viewLifecycleOwner.setAttachedToWindow()
  }

  override fun onDetachedFromWindow() {
    viewLifecycleOwner.setDetachedFromWindow()
    super.onDetachedFromWindow()
  }

  override fun setText(text: CharSequence?, type: BufferType?) {
    val currentText = getText()
    // Guard against 'text = text' scenarios stripping away rich-text by ensuring that text isn't
    // rebound if the text isn't actually different (otherwise the parsed text can override
    // boundRawHtml resulting in the actual markup being lost). Also, ensure that the HTML isn't
    // re-parsed since it will trigger new spans to be created which will result in many extra and
    // unnecessary image drawables to be bound below.
    if (currentText != text) {
      // Null strings are treated as empty string since TextView expects that null is never bound.
      // Note that HTML strings are not parsed until all of the GCS properties are properly set up.
      val styledHtmlText = htmlParser?.parseOppiaRichText(
        context, text?.toString() ?: "", supportsConceptCards
      ) ?: ""
      super.setText(styledHtmlText, type)
      boundRawHtml = text // Save for later rebinds.

      // After the text is set & before re-layout, make sure all deferred drawables are observed so
      // that, when they're loaded, the text view is re-laid out to fit the newly loaded image. Note
      // that this operation has some room for performance improvements: it triggers a full
      // re-layout of the TextView for quickly subsequent loads. It may be preferable in the future
      // to batch load all assets when they're guaranteed to be stored locally and do a single
      // layout to avoid flickering or perceived latency.
      val needsToLayOut = getImageDrawables().fold(initial = false) { needsToReLayOut, drawable ->
        if (drawable is DeferredImageDrawable<*>) {
          // Only listen for the drawable if it isn't already loaded.
          val loadedDrawable = drawable.getLoadedDrawable()
          if (loadedDrawable.value == null) {
            viewLifecycleOwner.observeOnce(loadedDrawable) {
              // This drawable needs to be re-laid out.
              drawablesRequireLayOut = true

              // The drawable being finished loading requires another re-layout since the layout
              // step is responsible for fitting images within the text view.
              requestLayout()
            }
          } else {
            return@fold true // The image is already available. Ensure it's measured immediately.
          }
        }
        return@fold needsToReLayOut
      }
      if (needsToLayOut) {
        // There are drawables that are immediately available for laying out the view.
        drawablesRequireLayOut = true
        requestLayout()
      }
    }  else {
      // Trigger internal updates by setting the same text again. This may happen during layout and
      // is not guaranteed to trigger a re-layout or re-draw.
      super.setText(currentText, type)
    }
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)

    val viewWidth = width
    if (drawablesRequireLayOut || viewWidth != lastViewWidthUsedForDrawables) {
      layOutImageDrawables(getImageDrawables(), viewWidth)
      drawablesRequireLayOut = false
      lastViewWidthUsedForDrawables = viewWidth

      // Re-measure the text view in case the drawables lead to the text view taking up more/less
      // space than before. Note that only height is expected to ever change since the drawables are
      // constrained based on the view's computed width.
      super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
  }

  private fun layOutImageDrawables(drawables: List<Drawable>, viewWidth: Int) {
    val layoutParams = layoutParams
    val maxAvailableWidth = if (layoutParams.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
      // Assume that wrap_content cases means that the view cannot exceed its parent's width minus
      // margins.
      val parent = parent
      if (parent is View && layoutParams is ViewGroup.MarginLayoutParams) {
        // Only pick the computed space if it allows the view to expand to accommodate larger
        // images.
        max(viewWidth, parent.width - (layoutParams.leftMargin + layoutParams.rightMargin))
      } else viewWidth
    } else viewWidth

    val atLeastOneUpdate = drawables.fold(initial = false) { prevDrawableUpdated, drawable ->
      // Note 'or' is used to make sure each drawable is evaluated and there's no short-circuiting.
      prevDrawableUpdated or layOutImageDrawable(drawable, maxAvailableWidth)
    }
    if (atLeastOneUpdate) {
      // Ensure the view is redrawn if at least one drawable has new bounds. Note that the
      // 'text = text' bit is needed to properly account for bounds changes happening during layout.
      text = text
      invalidate()
    }
  }

  // TODO: doc. Mention returns whether the drawable has had its bounds updated.
  private fun layOutImageDrawable(drawable: Drawable, maxAvailableWidth: Int): Boolean {
    var drawableHeight = drawable.intrinsicHeight
    var drawableWidth = drawable.intrinsicWidth
    if (drawableWidth == 0 || drawableHeight == 0) {
      // Either the drawable is invalid, or it hasn't finished loading yet.
      return false
    }
    if (drawableHeight <= minimumImageSize || drawableWidth <= minimumImageSize) {
      // The multipleFactor value is used to make sure that the aspect ratio of the image remains the same.
      // Example: Height is 90, width is 60 and minimumImageSize is 120.
      // Then multipleFactor will be 2 (120/60).
      // The new height will be 180 and new width will be 120.
      val multipleFactor = if (drawableHeight <= drawableWidth) {
        // If height is less then the width, multipleFactor value is determined by height.
        (minimumImageSize.toDouble() / drawableHeight.toDouble())
      } else {
        // If height is less then the width, multipleFactor value is determined by width.
        (minimumImageSize.toDouble() / drawableWidth.toDouble())
      }
      drawableHeight = (drawableHeight.toDouble() * multipleFactor).toInt()
      drawableWidth = (drawableWidth.toDouble() * multipleFactor).toInt()
    }
    val maximumImageSize = maxAvailableWidth - maxContentItemPadding
    if (drawableWidth >= maximumImageSize) {
      // The multipleFactor value is used to make sure that the aspect ratio of the image remains the same.
      // Example: Height is 420, width is 440 and maximumImageSize is 200.
      // Then multipleFactor will be (200/440).
      // The new height will be 191 and new width will be 200.
      val multipleFactor = if (drawableHeight >= drawableWidth) {
        // If height is greater then the width, multipleFactor value is determined by height.
        (maximumImageSize.toDouble() / drawableHeight.toDouble())
      } else {
        // If height is greater then the width, multipleFactor value is determined by width.
        (maximumImageSize.toDouble() / drawableWidth.toDouble())
      }
      drawableHeight = (drawableHeight.toDouble() * multipleFactor).toInt()
      drawableWidth = (drawableWidth.toDouble() * multipleFactor).toInt()
    }
    val initialDrawableMargin = if (centerAlignImages) {
      ((maxAvailableWidth - drawableWidth) / 2).coerceAtLeast(0)
    } else {
      0
    }

    val newDrawableBounds = Rect(
      /* left= */ initialDrawableMargin,
      /* top= */ 0,
      /* right= */ drawableWidth + initialDrawableMargin,
      /* bottom= */ drawableHeight
    )
    if (newDrawableBounds != drawable.bounds) {
      drawable.bounds = newDrawableBounds
      return true // New bounds have been computed.
    }
    // The drawable doesn't need to be redrawn since it's the same. Note that this could result in a
    // new drawable not being drawn, but that's not expected since the default bounds is
    // (0, 0 - 0, 0) which, if computed here, won't lead to anything being drawn, anyway.
    return false
  }

  private fun getImageDrawables(): List<Drawable> {
    val imageSpans = (text as? Spanned)?.getSpans(0, text.length, ImageSpan::class.java)
    return imageSpans?.map(ImageSpan::getDrawable) ?: listOf()
  }

  /**
   * If raw HTML was previously bound, rebinds it. This should be used in situations when settings
   * have changed that may lead to the HTML being interpreted differently.
   *
   * Calling this method should guarantee all image drawable bounds are recomputed.
   */
  private fun reBindHtml() {
    boundRawHtml?.let { text = it }
  }

  /**
   * A custom [LifecycleOwner] that hackily superimposes a view's lifecycle into the buckets of an
   * activity's lifecycle. This is done privately since it should only ever be used for LiveData
   * observers whose results can be processed within the window this owner classifies as "resumed"
   * in activity terminology (view is attached).
   *
   * One limitation is that this class treats view detachment as a destruction. Views can be
   * detached/reattached in legitimate view hierarchy organizations. This class works around this
   * limitation by returning a new lifecycle after detachment that's again in the initialized state.
   * If the view gets attached again, new observers should correctly be processed. Observers must
   * not be long-lived, and will need to reset themselves to work against this situation. To
   * simplify management, callers should just use [observeOnce].
   *
   * Note: the internal lifecycle events processed by this class SHOULD NOT BE CONSTRUED to mean
   * their corresponding value (e.g. a state of RESUMED does NOT mean that the fragment/activity to
   * which this view is bound to, if any, is in a RESUMED state--that is not guaranteed nor can it
   * be).
   *
   * Reference for this implementation:
   * https://developer.android.com/topic/libraries/architecture/lifecycle#implementing-lco.
   */
  private class ViewLifecycleOwner: LifecycleOwner {
    /**
     * Tracks the lifecycle state. Note that this is not a proper lifecycle since Views do not
     * follow the same lifecycle patterns as activities. However, this is emulating a
     * fragment/activity's lifecycle for the sake of allowing the view to safely observe LiveDatas.
     */
    private var lifecycleRegistry = LifecycleRegistry(this)

    override fun getLifecycle(): Lifecycle = lifecycleRegistry

    internal fun setAttachedToWindow() {
      // Treat attached to window as "ready" by combining created, started, and resumed.
      lifecycleRegistry.currentState = Lifecycle.State.CREATED
      lifecycleRegistry.currentState = Lifecycle.State.STARTED
      lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    internal fun setDetachedFromWindow() {
      // Treat detachment from the window as destroyed. Note that this means moving the view around
      // in the hierarchy will effectively look like configuration changes to observers relying on
      // the lifecycle maintained by this class.
      lifecycleRegistry.currentState = Lifecycle.State.STARTED
      lifecycleRegistry.currentState = Lifecycle.State.DESTROYED

      // Recreate the lifecycle registry so that new observers can be added in the event the view is
      // reattached. Note that no memory should ever be leaked since moving the lifecycle to a
      // destroyed state automatically unregisters downstream observers, even for long-lived
      // LiveData objects.
      lifecycleRegistry = LifecycleRegistry(this)
    }

    /**
     * Observes that exactly one value is received from the [LiveData], only if the view is
     * currently attached, or when the view is next attached. If the view is detached before the
     * observed value is observed, it will never be observed even after the view is attached.
     * Callers should call this function again after re-attachment if no result is observed.
     */
    internal fun <T> observeOnce(liveData: LiveData<T>, callback: (T) -> Unit) {
      return liveData.observe(this, object: Observer<T> {
        override fun onChanged(result: T) {
          callback(result)
          liveData.removeObserver(this)
        }
      })
    }
  }
}
