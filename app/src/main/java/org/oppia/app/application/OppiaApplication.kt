package org.oppia.app.application

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.multidex.MultiDexApplication
import androidx.work.Configuration
import com.google.firebase.FirebaseApp
import org.oppia.app.activity.ActivityComponent
import org.oppia.domain.oppialogger.ApplicationStartupListener

/** The root [Application] of the Oppia app. */
class OppiaApplication :
  MultiDexApplication(),
  ActivityComponentFactory,
  ApplicationInjectorProvider,
  Configuration.Provider {
  /** The root [ApplicationComponent]. */
  private val component: ApplicationComponent by lazy {
    DaggerApplicationComponent.builder()
      .setApplication(this)
      .build()
  }

  override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
    return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
  }

  override fun getApplicationInjector(): ApplicationInjector = component

  override fun onCreate() {
    super.onCreate()
    FirebaseApp.initializeApp(applicationContext)
    component.getApplicationStartupListeners().forEach(ApplicationStartupListener::onCreate)
  }

  override fun getWorkManagerConfiguration(): Configuration {
    return component.getWorkManagerConfiguration()
  }
}
