package org.oppia.android.app.testing.activity

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import org.oppia.android.app.activity.ActivityIntentFactories
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.activity.route.ActivityRouter
import org.oppia.android.app.databinding.R
import org.oppia.android.app.translation.ActivityLanguageLocaleHandler
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
open class TestActivity : InjectableAutoLocalizedAppCompatActivity() {
  @Inject lateinit var appLanguageResourceHandler: AppLanguageResourceHandler
  @Inject lateinit var dateTimeUtil: DateTimeUtil

  @Inject
  lateinit var topicActivityIntentFactory: ActivityIntentFactories.TopicActivityIntentFactory

  @Inject
  lateinit var recentlyPlayedActivityIntentFactory:
    ActivityIntentFactories.RecentlyPlayedActivityIntentFactory

  @Inject lateinit var appLanguageWatcherMixin: AppLanguageWatcherMixin
  @Inject lateinit var mathExpressionAccessibilityUtil: MathExpressionAccessibilityUtil
  @Inject lateinit var activityRouter: ActivityRouter
  @Inject lateinit var activityLanguageLocaleHandler: ActivityLanguageLocaleHandler

  override fun attachBaseContext(newBase: Context?) {
    super.attachBaseContext(newBase)
    (activityComponent as Injector).inject(this)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.test_activity)
  }

  override fun initializeMixin(appLanguageWatcherMixin: AppLanguageWatcherMixin) {
    if (!forceDisableLanguageWatcherMixinInitialization) {
      super.initializeMixin(appLanguageWatcherMixin)
    }
  }

  /** Activity injector for [TestActivity]. */
  interface Injector {
    /** Injects the prerequisite dependencies into [TestActivity]. */
    fun inject(testActivity: TestActivity)
  }

  companion object {
    private val shadowsClass by lazy { Class.forName("org.robolectric.Shadows") }
    private val shadowPackageManagerClass by lazy {
      Class.forName("org.robolectric.shadows.ShadowPackageManager")
    }
    private val shadowOfPackageManagerMethod by lazy {
      shadowsClass.getDeclaredMethod("shadowOf", PackageManager::class.java)
    }
    private val addOrUpdateActivityMethod by lazy {
      shadowPackageManagerClass.getDeclaredMethod("addOrUpdateActivity", ActivityInfo::class.java)
    }

    /**
     * A singleton control variable for tests to disable [AppLanguageWatcherMixin] initialization
     * for all future [TestActivity]s until set back to false. This is useful for cases when tests
     * want to manage mixin initialization directly.
     *
     * This value is false by default.
     */
    var forceDisableLanguageWatcherMixinInitialization = false

    /** Returns a new [Intent] for the given [Context] to launch new [TestActivity]s. */
    fun createIntent(context: Context): Intent = Intent(context, TestActivity::class.java)

    /**
     * Registers the activity of type [T] with [context]'s Robolectric shadow [PackageManager] which
     * makes it available to intent to during a test (such that it doesn't need to be registered in
     * an AndroidManifest.xml file). This is especially useful to create custom activities on a
     * per-test basis (such as for cases when a test requires its activity to implement a particular
     * listener class).
     */
    inline fun <reified T : TestActivity> registerWithPackageManager(context: Context) {
      // Reference: https://github.com/robolectric/robolectric/pull/4736#issuecomment-1831034882.
      val activityInfo = ActivityInfo().apply {
        this.name = T::class.java.name
        packageName = context.packageName
      }
      registerActivityInfo(context, activityInfo)
    }

    /**
     * Registers the specified [ActivityInfo] with [context]'s Robolectric shadow [PackageManager].
     * Generally, this shouldn't be used directly; it's made accessible for the sake of
     * [registerWithPackageManager].
     */
    fun registerActivityInfo(context: Context, activityInfo: ActivityInfo) {
      // TODO(#1607): Make this depend on Robolectric dependencies directly to avoid reflection.
      val shadowPackageManager =
        shadowOfPackageManagerMethod.invoke(/* obj= */ null, context.packageManager)
      addOrUpdateActivityMethod.invoke(shadowPackageManager, activityInfo)
    }
  }
}
