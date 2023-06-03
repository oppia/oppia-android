package org.oppia.android.app.activity

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.android.app.fragment.FragmentComponent
import org.oppia.android.app.fragment.FragmentComponentBuilderInjector
import org.oppia.android.app.fragment.FragmentComponentFactory
import org.oppia.android.app.translation.AppLanguageActivityInjector
import org.oppia.android.app.translation.AppLanguageActivityInjectorProvider
import org.oppia.android.app.translation.AppLanguageWatcherMixin
/**
 * An [AppCompatActivity] that facilitates field injection to child activities and constituent
 * fragments that extend [org.oppia.android.app.fragment.InjectableFragment].
 */
abstract class InjectableAppCompatActivity :
  AppCompatActivity(), FragmentComponentFactory, AppLanguageActivityInjectorProvider {
  /**
   * The [ActivityComponent] corresponding to this activity. This cannot be used before
   * [attachBaseContext] is called, and can be used to inject lateinit fields in child activities
   * during activity creation (which is recommended to be done in an override of [onCreate]).
   */
  lateinit var activityComponent: ActivityComponent

  override fun attachBaseContext(newBase: Context?) {
    val applicationContext = checkNotNull(newBase?.applicationContext) {
      "Expected attached Context to have an application context defined."
    }
    onInitializeActivityComponent(applicationContext)
    val newConfiguration = onInitializeLocalization(newBase)
    super.attachBaseContext(newBase?.createConfigurationContext(newConfiguration))
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    ensureLayoutDirection()
    super.onCreate(savedInstanceState)
  }

  override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
    ensureLayoutDirection()
    super.onCreate(savedInstanceState, persistentState)
  }

  override fun createFragmentComponent(fragment: Fragment): FragmentComponent {
    val builderInjector = activityComponent as FragmentComponentBuilderInjector
    return builderInjector.getFragmentComponentBuilderProvider().get().setFragment(fragment).build()
  }

  override fun getAppLanguageActivityInjector(): AppLanguageActivityInjector = activityComponent

  private fun onInitializeActivityComponent(applicationContext: Context) {
    val componentFactory = applicationContext as ActivityComponentFactory
    activityComponent = componentFactory.createActivityComponent(this)
  }

  private fun onInitializeLocalization(
    newBase: Context?
  ): Configuration {
    // Given how DataProviders work (i.e. by resolving data races using eventual consistency), it's
    // possible to miss some updates in really unlikely situations. No additional work will be done
    // to prevent these data races unless they're actually hit by users. It shouldn't, in practice,
    // be possible since it requires changing the system language between activity transitions, and
    // in most cases that should result in an activity recreation by the mixin, anyway.
    val activityLanguageInjectorProvider = this as AppLanguageActivityInjectorProvider
    val activityLanguageActivityInjector =
      activityLanguageInjectorProvider.getAppLanguageActivityInjector()
    val activityLanguageLocaleHandler =
      activityLanguageActivityInjector.getActivityLanguageHandler()

    val appLanguageWatcherMixin = activityLanguageActivityInjector.getAppLanguageWatcherMixin()
    initializeMixin(appLanguageWatcherMixin)

    return Configuration(newBase?.resources?.configuration).also { newConfiguration ->
      activityLanguageLocaleHandler.initializeLocaleForActivity(newConfiguration)
    }
  }

  /**
   * Initializes [appLanguageWatcherMixin] based on the localization requirements of the implementation.
   * This should never be directly implemented by non-abstract activity classes.
   */
  abstract fun initializeMixin(appLanguageWatcherMixin: AppLanguageWatcherMixin)

  private fun ensureLayoutDirection() {
    // Ensure the root decor view has the correct layout direct setup per the base context. In some
    // cases, Android will let the app recreate the activity & properly update the layout direction
    // of the base context's configuration based on the selected Locale but not update the decor
    // view. This ensures that views use the correct layout in these situations.
    window.decorView.layoutDirection = baseContext.resources.configuration.layoutDirection
  }
}
