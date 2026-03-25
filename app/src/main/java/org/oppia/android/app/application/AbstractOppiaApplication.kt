package org.oppia.android.app.application

import android.annotation.SuppressLint
import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.multidex.MultiDexApplication
import androidx.work.Configuration
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.domain.oppialogger.ApplicationStartupListener

/**
 * The root base [Application] of the Oppia app.
 *
 * @param createComponentBuilder the [ApplicationComponent.Builder] used to construct the root
 *     Dagger class for the implementation's flavor of the app
 * @property firebaseAppCheckProviderFactory the [AppCheckProviderFactory] used when initializing
 *     FirebaseAppCheck. This is defaulted for production use cases, but implementations may choose
 *     to provide a different one for improved debugging support.
 */
abstract class AbstractOppiaApplication(
  createComponentBuilder: () -> ApplicationComponent.Builder,
  private val firebaseAppCheckProviderFactory: AppCheckProviderFactory =
    PlayIntegrityAppCheckProviderFactory.getInstance()
) : MultiDexApplication(),
  ActivityComponentFactory,
  ApplicationInjectorProvider,
  Configuration.Provider {

  /** The root [ApplicationComponent]. */
  private val component: ApplicationComponent by lazy {
    createComponentBuilder().setApplication(this).build()
  }

  override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
    return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
  }

  override fun getApplicationInjector(): ApplicationInjector = component

  @SuppressLint("ObsoleteSdkInt") // Incorrect warning.
  override fun onCreate() {
    super.onCreate()

    // Allow startup listeners to early initialize.
    val startupListeners = component.getApplicationStartupListeners()
    startupListeners.forEach(ApplicationStartupListener::onCreateStarted)

    // Initialize high-level third-party systems. Note that WorkManager doesn't need to be
    // initialized here because it will automatically initialize itself due to the application being
    // a Configuration provider.
    FirebaseApp.initializeApp(applicationContext)
    // FirebaseAppCheck protects our API resources from abuse. It works with Firebase
    // services, Google Cloud services, and can also be implemented for our own APIs. See
    // https://firebase.google.com/docs/app-check for currently supported Firebase products.
    // Note that as of this code being checked in, only the app's Firestore usage is affected
    // by App Check (Analytics is NOT affected).
    FirebaseAppCheck.getInstance().installAppCheckProviderFactory(firebaseAppCheckProviderFactory)

    // Kick off a background task to finish startup initialization. Nothing at this stage should be
    // startup-state sensitive. It's also fine for parameters to not be fully initialized at this
    // point because each app entry point already accounts for potentially uninitialized parameters:
    // splash, direct activity recreation, and waking up the app to kick off a worker. Finally,
    // since this is using 'launch' any uncaught exceptions should correctly trigger a failure in
    // app startup (which is ideal in this case because we can't reliably and safely start the app).
    CoroutineScope(component.getBackgroundDispatcher()).launch {
      // Wait for parameters to load before running any startup routines that may depend on them.
      component.getPlatformParameterController().loadParametersAsync().await()
      startupListeners.forEach(ApplicationStartupListener::onCompletedInitialization)
    }
  }

  override fun getWorkManagerConfiguration(): Configuration =
    component.getWorkManagerConfiguration()
}
