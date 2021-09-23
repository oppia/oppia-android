package org.oppia.android.testing.activity

import android.content.Context
import android.content.Intent
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.utility.datetime.DateTimeUtil

// TODO(#3830): Migrate all test activities over to using this test activity & make this closed.
/**
 * General-purpose test activity for app layer tests.
 *
 * This can be used in one of two ways:
 * 1. As a standalone launchable activity (which can be useful if activity-level components like
 *   [AppLanguageResourceHandler] or [DateTimeUtil] are needed in tests
 * 2. As a superclass for other test activities where specific activity-level components are needed
 *   in tests
 */
open class TestActivity: InjectableAppCompatActivity() {
  companion object {
    /** Returns a new [Intent] for the given [Context] to launch new [TestActivity]s. */
    fun createIntent(context: Context): Intent = Intent(context, TestActivity::class.java)
  }

  /** Returns the [AppLanguageResourceHandler] corresponding to this activity. */
  fun getAppLanguageResourceHandler(): AppLanguageResourceHandler =
    activityComponent.getAppLanguageResourceHandler()

  /** Returns the [DateTimeUtil] corresponding to this activity. */
  fun getDateTimeUtil(): DateTimeUtil = activityComponent.getDateTimeUtil()
}
