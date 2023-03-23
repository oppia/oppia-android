package org.oppia.android.util.parser.extention

import android.content.Intent
import android.net.Uri
import android.text.Selection
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat

fun TextView.makeAutoLinks(links: List<String>) {
  if (links.isNullOrEmpty())
    return

  val spannableString = SpannableString(this.text)
  var startIndexOfLink = -1

  for (tag in links) {

    val clickableSpan = object : ClickableSpan() {
      override fun updateDrawState(textPaint: TextPaint) {
        textPaint.color = textPaint.linkColor
        textPaint.isUnderlineText=true
      }

      override fun onClick(view: View) {
        Selection.setSelection((view as TextView).text as Spannable, 0)
        view.invalidate()
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tag))
        ContextCompat.startActivity(context, intent, null)
      }
    }

    startIndexOfLink = this.text.toString().indexOf(tag)

    if (startIndexOfLink > -1)
      try {
        spannableString.setSpan(
          clickableSpan, startIndexOfLink, startIndexOfLink + tag.length,
          Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
      } catch (e: Exception) {
        e.printStackTrace()
      }
  }

  this.movementMethod = LinkMovementMethod.getInstance()
  this.setText(spannableString, TextView.BufferType.SPANNABLE)
}

fun String.getUrls(): List<String> {
  val hashtagPattern = "^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|.]\$".toRegex()
  val words  = this.replace("\n", " ").split(" ")
  return words.filter { it.contains(hashtagPattern) }
}