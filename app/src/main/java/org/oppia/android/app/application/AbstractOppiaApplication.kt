package org.oppia.android.app.application

import android.annotation.SuppressLint
import android.app.Application
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import androidx.work.Configuration
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.domain.oppialogger.ApplicationStartupListener

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
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      // Ensure vector drawables can be properly loaded on KitKat devices. Note that this can
      // introduce memory issues, but it's an easier-to-maintain solution that replacing all image
      // binding with custom hook-ins (especially when it comes to databinding which isn't
      // configurable in how it loads drawables), or building a custom vector drawable->PNG pipeline
      // in Bazel.
      AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }
    // The current WorkManager version doesn't work in SDK 31+, so disable it.
    // TODO(#4751): Re-enable WorkManager for S+.
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
      FirebaseApp.initializeApp(applicationContext)
      Firebase.firestore
      WorkManager.initialize(applicationContext, workManagerConfiguration)
      val workManager = WorkManager.getInstance(applicationContext)
      component.getAnalyticsStartupListenerStartupListeners().forEach { it.onCreate(workManager) }
    }
    component.getApplicationStartupListeners().forEach(ApplicationStartupListener::onCreate)
  }

  override fun getWorkManagerConfiguration(): Configuration {
    return component.getWorkManagerConfiguration()
  }
}
