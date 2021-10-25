package org.oppia.android.instrumentation.application

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.multidex.MultiDexApplication
import androidx.work.Configuration
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.domain.oppialogger.ApplicationStartupListener

/** The root [Application] of the all end-to-end test apps. */
class OppiaTestApplication :
  MultiDexApplication(),
  ActivityComponentFactory,
  ApplicationInjectorProvider,
  Configuration.Provider {
  private val component: TestApplicationComponent by lazy {
    DaggerTestApplicationComponent.builder()
      .setApplication(this)
      .build()
  }

  override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
    return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
  }

  override fun onCreate() {
    super.onCreate()
    FirebaseApp.initializeApp(applicationContext)
    WorkManager.initialize(applicationContext, workManagerConfiguration)
    component.getApplicationStartupListeners().forEach(ApplicationStartupListener::onCreate)
  }

  override fun getApplicationInjector(): ApplicationInjector = component

  override fun getWorkManagerConfiguration(): Configuration {
    return component.getWorkManagerConfiguration()
  }
}
