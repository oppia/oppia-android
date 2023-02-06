package org.oppia.android.app.activity

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.android.app.fragment.FragmentComponent
import org.oppia.android.app.fragment.FragmentComponentBuilderInjector
import org.oppia.android.app.fragment.FragmentComponentFactory
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.translation.AppLanguageActivityInjector
import org.oppia.android.app.translation.AppLanguageActivityInjectorProvider
import org.oppia.android.app.translation.AppLanguageApplicationInjectorProvider
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId

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
  lateinit var profileId: ProfileId

  override fun attachBaseContext(newBase: Context?) {
    val applicationContext = checkNotNull(newBase?.applicationContext) {
      "Expected attached Context to have an application context defined."
    }
    onInitializeActivityComponent(applicationContext)
    val newConfiguration = onInitializeLocalization(applicationContext, newBase)
    super.attachBaseContext(newBase?.createConfigurationContext(newConfiguration))
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    ensureLayoutDirection()
    super.onCreate(savedInstanceState)
    profileId = intent.extractCurrentUserProfileId()
    Log.e("current profile id", profileId.internalId.toString())
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
    applicationContext: Context,
    newBase: Context?
  ): Configuration {
    // Given how DataProviders work (i.e. by resolving data races using eventual consistency), it's
    // possible to miss some updates in really unlikely situations. No additional work will be done
    // to prevent these data races unless they're actually hit by users. It shouldn't, in practice,
    // be possible since it requires changing the system language between activity transitions, and
    // in most cases that should result in an activity recreation by the mixin, anyway.
    val appLanguageAppInjectorProvider =
      applicationContext as AppLanguageApplicationInjectorProvider
    val appLanguageAppInjector = appLanguageAppInjectorProvider.getAppLanguageApplicationInjector()
    val appLanguageActivityInjector = activityComponent as AppLanguageActivityInjector
    val appLanguageLocaleHandler = appLanguageAppInjector.getAppLanguageHandler()
    val appLanguageWatcherMixin = appLanguageActivityInjector.getAppLanguageWatcherMixin()
    appLanguageWatcherMixin.initialize()
    return Configuration(newBase?.resources?.configuration).also { newConfiguration ->
      appLanguageLocaleHandler.initializeLocaleForActivity(newConfiguration)
    }
  }

  private fun ensureLayoutDirection() {
    // Ensure the root decor view has the correct layout direct setup per the base context. In some
    // cases, Android will let the app recreate the activity & properly update the layout direction
    // of the base context's configuration based on the selected Locale but not update the decor
    // view. This ensures that views use the correct layout in these situations.
    window.decorView.layoutDirection = baseContext.resources.configuration.layoutDirection
  }
}
