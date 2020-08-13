package org.oppia.app.application

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.multidex.MultiDexApplication
import org.oppia.app.activity.ActivityComponent
import org.oppia.domain.oppialogger.exceptions.OppiaUncaughtExceptionHandler
import org.oppia.util.logging.ConsoleLogger
import javax.inject.Inject

/** The root [Application] of the Oppia app. */
class OppiaApplication : MultiDexApplication(), ActivityComponentFactory {
  /** The root [ApplicationComponent]. */

  private val component: ApplicationComponent by lazy {
    DaggerApplicationComponent.builder()
      .setApplication(this)
      .build()
  }

  private val handler: HandlerComponent by lazy {
    DaggerHandlerComponent.builder()
      .setApplication(this)
      .build()
  }

  @Inject lateinit var oppiaUncaughtExceptionHandler: OppiaUncaughtExceptionHandler

  override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {

    return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
  }

/*  */
  override fun onCreate() {
    super.onCreate()
    handler.inject(oppiaUncaughtExceptionHandler)
    setOppiaUncaughtExceptionHandler()
  }

  private fun setOppiaUncaughtExceptionHandler(){
    try {
      Thread.currentThread().uncaughtExceptionHandler = oppiaUncaughtExceptionHandler
    }catch (e: Exception) {
      e.printStackTrace()
      Log.e("TAG", e.toString())
    }finally {
      Thread.currentThread().uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
    }
  }
}
