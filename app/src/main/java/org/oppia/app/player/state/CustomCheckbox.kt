package org.oppia.app.player.state

import android.content.Context
import android.text.Html
import android.text.Spannable
import android.text.Spanned
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import org.oppia.app.R
import org.oppia.util.data.UrlImageParser
import android.content.res.ColorStateList
import android.os.Build
import android.view.Gravity

// TODO(#163): Move this to a StateCardFragment Low-fi PR.
/** Custom Checkbox for MultipleSelectionInputInteractionView. */
class CustomCheckbox(context: Context?, private val optionContents: String) : CheckBox(context) {

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
        )
      );
    }
    setHighlightColor(ContextCompat.getColor(context, R.color.oppiaDarkBlue))
    textSize = 16f
    setPadding(paddingDp, paddingDp, paddingDp, paddingDp)
    id = View.generateViewId()
    text = convertHtmlToString(optionContents, rootView).toString()

    setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
      val msg = "You have " + (if (isChecked) "checked" else "unchecked") + " this Check it Checkbox."
      Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    })
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
