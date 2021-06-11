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

  /** Boolean variable to enable dev mode. It will decide which application component to use */
  private val enableDevMode = true

  /** The root [ApplicationComponent]. */
  private val component: ApplicationComponent by lazy {
    DaggerApplicationComponent.builder()
      .setApplication(this)
      .build()
  }

  /** The root [ApplicationComponentForDevMode]. */
  private val componentForDevMode: ApplicationComponentForDevMode by lazy {
    DaggerApplicationComponentForDevMode.builder()
      .setApplication(this)
      .build()
  }

  override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
    return if (enableDevMode)
      componentForDevMode.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    else
      component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
  }

  override fun getApplicationInjector(): ApplicationInjector =
    if (enableDevMode) componentForDevMode
    else component

  override fun onCreate() {
    super.onCreate()
    FirebaseApp.initializeApp(applicationContext)
    WorkManager.initialize(applicationContext, workManagerConfiguration)
    if (enableDevMode)
      componentForDevMode.getApplicationStartupListeners()
        .forEach(ApplicationStartupListener::onCreate)
    else
      component.getApplicationStartupListeners().forEach(ApplicationStartupListener::onCreate)
  }

  override fun getWorkManagerConfiguration(): Configuration {
    return if (enableDevMode)
      componentForDevMode.getWorkManagerConfiguration()
    else
      component.getWorkManagerConfiguration()
  }
}
