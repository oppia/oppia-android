package org.oppia.android.instrumentation

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.application.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider

/** The root [Application] of the Oppia app. */
class OppiaTestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
  private val component: TestApplicationComponent by lazy {
    DaggerTestApplicationComponent.builder()
      .setApplication(this)
      .build()
  }

  override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
    return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
  }

  override fun getApplicationInjector(): ApplicationInjector = component
}
