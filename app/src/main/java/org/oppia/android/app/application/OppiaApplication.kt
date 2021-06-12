package org.oppia.android.app.application

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.multidex.MultiDexApplication
import androidx.work.Configuration
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.domain.oppialogger.ApplicationStartupListener

/** The root [Application] of the Oppia app. */
class OppiaApplication :
  MultiDexApplication(),
  ActivityComponentFactory,
  ApplicationInjectorProvider,
  Configuration.Provider {

  /** Boolean variable to enable Debug mode. It will decide which application component to use */
  private val enableDebugMode = true

  /** The root [ApplicationComponent]. */
  private val component by lazy {
    if (enableDebugMode) {
      /** [ApplicationComponent] for Debug mode. */
      DaggerDebugApplicationComponent.builder()
        .setApplication(this)
        .build() as DebugApplicationComponent
    } else {
      /** [ApplicationComponent] for Prod mode. */
      DaggerApplicationComponent.builder()
        .setApplication(this)
        .build()
    }
  }

  override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
    return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
  }

  override fun getApplicationInjector(): ApplicationInjector = component

  override fun onCreate() {
    super.onCreate()
    FirebaseApp.initializeApp(applicationContext)
    WorkManager.initialize(applicationContext, workManagerConfiguration)
    component.getApplicationStartupListeners().forEach(ApplicationStartupListener::onCreate)
  }

  override fun getWorkManagerConfiguration(): Configuration {
    return component.getWorkManagerConfiguration()
  }
}
