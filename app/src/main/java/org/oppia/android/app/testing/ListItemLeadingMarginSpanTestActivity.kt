package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.InjectableAppCompatActivity

/** This is a dummy activity to test unordered <ul> and ordered <ol> lists leading margin span. */
class ListItemLeadingMarginSpanTestActivity : InjectableAppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)
    setContentView(R.layout.test_list_item_leading_margin_activity)
  }

  /** Dagger injector for [ListItemLeadingMarginSpanTestActivity]. */
  interface Injector {
    /** Injects dependencies into the [activity]. */
    fun inject(activity: ListItemLeadingMarginSpanTestActivity)
  }

  companion object {
    /** Returns an [Intent] for opening new instances of [ListItemLeadingMarginSpanTestActivity]. */
    fun createIntent(context: Context): Intent =
      Intent(context, ListItemLeadingMarginSpanTestActivity::class.java)
  }
}
