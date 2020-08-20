package org.oppia.app.application

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.multidex.MultiDexApplication
import org.oppia.app.activity.ActivityComponent
import org.oppia.domain.oppialogger.OppiaLogUploader

/** The root [Application] of the Oppia app. */
class OppiaApplication : MultiDexApplication(), ActivityComponentFactory {
  /** The root [ApplicationComponent]. */
  private val component: ApplicationComponent by lazy {
    DaggerApplicationComponent.builder()
      .setApplication(this)
      .build()
  }

  private val oppiaLogUploader = OppiaLogUploader()

  override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
    return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
  }

  override fun onCreate() {
    super.onCreate()
    oppiaLogUploader.setWorkerRequestForEvents()
    oppiaLogUploader.setWorkerRequestForExceptions()
  }
}
