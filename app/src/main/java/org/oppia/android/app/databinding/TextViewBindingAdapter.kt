package org.oppia.android.app.databinding

import android.app.Activity
import android.content.ContextWrapper
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import org.oppia.android.R
import org.oppia.android.app.translation.AppLanguageActivityInjectorProvider
import org.oppia.android.app.translation.AppLanguageResourceHandler

@BindingAdapter("profile:lastVisited")
fun TextView.setProfileLastVisitedText(timestamp: Long?) {
  timestamp?.let {
    // text =
  }
}

@BindingAdapter("profile:created")
fun setProfileDataText(textView: TextView, timestamp: Long) {
  val resourceHandler = getResourceHandler(textView)
  val time = resourceHandler.computeDateString(timestamp)
  textView.text = resourceHandler.getStringInLocaleWithWrapping(
    R.string.profile_edit_created,
    time
  )
}

// TODO: Move this to a common place.
fun getResourceHandler(view: View): AppLanguageResourceHandler {
  val provider =
    getAttachedActivity(view) as AppLanguageActivityInjectorProvider
  return provider.getAppLanguageActivityInjector().getAppLanguageResourceHandler()
}

private fun getAttachedActivity(view: View): Activity {
  var context = view.context
  while (context != null && context !is Activity) {
    check(context is ContextWrapper) {
      (
        "Encountered context in view (" + view + ") that doesn't wrap a parent context: " +
          context
        )
    }
    context = context.baseContext
  }
  checkNotNull(context) { "Failed to find base Activity for view: $view" }
  return context as Activity
}
