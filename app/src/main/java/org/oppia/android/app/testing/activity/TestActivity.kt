package org.oppia.android.app.testing.activity

import android.content.Context
import android.content.Intent
import org.oppia.android.app.activity.ActivityIntentFactories
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.translation.AppLanguageWatcherMixin
import org.oppia.android.app.utility.datetime.DateTimeUtil
import org.oppia.android.app.utility.math.MathExpressionAccessibilityUtil
import javax.inject.Inject

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
open class TestActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var appLanguageResourceHandler: AppLanguageResourceHandler

  @Inject
  lateinit var dateTimeUtil: DateTimeUtil

  @Inject
  lateinit var topicActivityIntentFactory: ActivityIntentFactories.TopicActivityIntentFactory

  @Inject
  lateinit var recentlyPlayedActivityIntentFactory:
    ActivityIntentFactories.RecentlyPlayedActivityIntentFactory

  @Inject
  lateinit var appLanguageWatcherMixin: AppLanguageWatcherMixin

  @Inject
  lateinit var mathExpressionAccessibilityUtil: MathExpressionAccessibilityUtil

  override fun attachBaseContext(newBase: Context?) {
    super.attachBaseContext(newBase)
    (activityComponent as Injector).inject(this)
  }

  /** Activity injector for [TestActivity]. */
  interface Injector {
    /** Injects the prerequisite dependencies into [TestActivity]. */
    fun inject(testActivity: TestActivity)
  }

  companion object {
    /** Returns a new [Intent] for the given [Context] to launch new [TestActivity]s. */
    fun createIntent(context: Context): Intent = Intent(context, TestActivity::class.java)
  }
}
