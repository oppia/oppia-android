package org.oppia.android.app.application

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.multidex.MultiDexApplication
import androidx.work.Configuration
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.model.BuildFlavor
import org.oppia.android.domain.oppialogger.ApplicationStartupListener
import org.oppia.android.util.extensions.safeForEach

/** The root base [Application] of the Oppia app. */
abstract class AbstractOppiaApplication(
  createComponentBuilder: () -> ApplicationComponent.Builder
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

    // Platform parameter initialization must happen very early since even workers can use both
    // feature flags and platform parameters.
    component.getPlatformParameterController().loadParametersAsync().invokeOnCompletion { e ->
      if (e != null) {
        throw Exception("Failed to initialize platform parameters.", e)
      }

      // Continue initializing the startup state. Due to the asynchronous nature of platform
      // parameter initialization, this happens after onCreate() finishes at the application level.
      // This can introduce some inconsistencies in SplashActivity, though by the time
      // SplashActivity completes the following should be fully initialized.
      CoroutineScope(Dispatchers.Main).async {
        FirebaseApp.initializeApp(applicationContext)
        // FirebaseAppCheck protects our API resources from abuse. It works with Firebase
        // services, Google Cloud services, and can also be implemented for our own APIs. See
        // https://firebase.google.com/docs/app-check for currently supported Firebase products.
        // Note that as of this code being checked in, only the app's Firestore usage is affected
        // by App Check (Analytics is NOT affected).
        if (component.getCurrentBuildFlavor() == BuildFlavor.DEVELOPER) {
          FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance(),
          )
        } else {
          FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance(),
          )
        }
        android.util.Log.e("@@@@@@@", "before initialize WorkManager")
        try {
          val wm = WorkManager.getInstance(applicationContext)

          // 2. Prune finished work to force a database write/cleanup
          wm.pruneWork()

          // 3. Enqueue a "Dummy" job with a unique name to force schema creation
          val dummy = OneTimeWorkRequestBuilder<DummyWorker>()
            .addTag("SCHEMA_FORCE")
            .build()
          wm.enqueueUniqueWork("force_db", ExistingWorkPolicy.REPLACE, dummy)
        } catch (e: Exception) {
          android.util.Log.e("WM_FATAL", "WorkManager failed to initialize schema", e)
        }

        val workManager = WorkManager.getInstance(applicationContext)
        android.util.Log.e("@@@@@@@", "after initialize WorkManager")
        component.getAnalyticsStartupListenerStartupListeners().safeForEach {
          it.onCreate(workManager)
        }
        android.util.Log.e("@@@@@@@", "after initialize WorkManager and enqueing")
        component.getApplicationStartupListeners().forEach(ApplicationStartupListener::onCreate)
      }.invokeOnCompletion {
        if (it != null) {
          throw Exception("Failed to continue application initialization.", it)
        }
      }
    }
  }

  class DummyWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
      // Just a log to prove it ran
      android.util.Log.e("@@@@@@@", "DummyWorker executed")
      return Result.success()
    }
  }

  override fun getWorkManagerConfiguration(): Configuration {
    android.util.Log.e("@@@@@", "getWorkManagerConfiguration()", Exception("trace"))
    return component.getWorkManagerConfiguration()
  }
}
