package org.oppia.app.player.state

import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.text.Html
import android.text.Spannable
import android.text.Spanned
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import org.oppia.app.R
import org.oppia.util.data.UrlImageParser

const val CUSTOM_TAG = "oppia-noninteractive-image"
const val HTML_TAG = "img"
const val CUSTOM_ATTRIBUTE = "filepath-with-value"
const val HTML_ATTRIBUTE = "src"

// TODO(#190): Move this to a custom.inputinteractionview.
/** Custom Checkbox for MultipleSelectionInputInteractionView. */
class CustomRadioButton(context: Context?,private val optionContents: String) : RadioButton(context) {

  init {
    initViews()
  }

  // Update default attributes of ItemSelectionInputInteractionView here.
  fun initViews() {

    val paddingPixel = 2
    val density = resources.displayMetrics.density
    val paddingDp = (paddingPixel * density).toInt()

    gravity = Gravity.LEFT
    setTextColor(ContextCompat.getColor(context, R.color.oppiaDarkBlue))
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      setButtonTintList(ColorStateList(
        arrayOf(
          intArrayOf(android.R.attr.state_enabled)
        ),
        intArrayOf(ContextCompat.getColor(context, R.color.oppiaDarkBlue))
      ));
    }
    setHighlightColor(ContextCompat.getColor(context, R.color.oppiaDarkBlue))
    textSize = 16f
    setPadding(paddingDp, paddingDp, paddingDp, paddingDp)
    id = View.generateViewId()
    text = convertHtmlToString(optionContents, rootView).toString()
  }

  fun convertHtmlToString(rawResponse: Any?, rdbtn: View): Spanned {

    var htmlContent = rawResponse as String;
    var result: Spanned

    if (htmlContent!!.contains(CUSTOM_TAG)) {

      htmlContent = htmlContent.replace(CUSTOM_TAG, HTML_TAG, false);
      htmlContent = htmlContent.replace(CUSTOM_ATTRIBUTE, HTML_ATTRIBUTE, false);
      htmlContent = htmlContent.replace("&amp;quot;", "")
    }

    var imageGetter = UrlImageParser(rdbtn as TextView, context)
    val html: Spannable
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
      html = Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_LEGACY, imageGetter, null) as Spannable
    } else {
      html = Html.fromHtml(htmlContent, imageGetter, null) as Spannable
    }
    result = html
    return result;
  }
}
