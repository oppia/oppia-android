package org.oppia.app.application

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.multidex.MultiDexApplication
import com.google.firebase.FirebaseApp
import org.oppia.app.activity.ActivityComponent
import org.oppia.domain.oppialogger.exceptions.OppiaUncaughtExceptionHandler

/** The root [Application] of the Oppia app. */
class OppiaApplication : MultiDexApplication(), ActivityComponentFactory {
  /** The root [ApplicationComponent]. */

  lateinit var oppiaUncaughtExceptionHandler: OppiaUncaughtExceptionHandler

  private val component: ApplicationComponent by lazy {
    DaggerApplicationComponent.builder()
      .setApplication(this)
      .build()
  }

  override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
    return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
  }

  override fun onCreate() {
    super.onCreate()
    FirebaseApp.initializeApp(applicationContext)
    oppiaUncaughtExceptionHandler = component.getOppiaExceptionHandler()
    setOppiaUncaughtExceptionHandler()
  }

  private fun setOppiaUncaughtExceptionHandler() {
    try {
      Thread.currentThread().uncaughtExceptionHandler = oppiaUncaughtExceptionHandler
    } catch (e: Exception) {
      e.printStackTrace()
    } finally {
      Thread.currentThread().uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
    }
  }
}
