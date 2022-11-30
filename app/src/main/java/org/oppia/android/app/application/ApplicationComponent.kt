package org.oppia.android.app.application

import android.app.Application
import androidx.work.Configuration
import dagger.BindsInstance
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.domain.oppialogger.ApplicationStartupListener
import org.oppia.android.domain.oppialogger.analytics.AnalyticsStartupListener
import javax.inject.Provider

/**
 * Root Dagger component for the application. All application-scoped modules should be included in
 * this component.
 *
 * This component will be subclasses for specific contexts (such as test builds, or specific build
 * flavors of the app).
 */
interface ApplicationComponent : ApplicationInjector {
  interface Builder {
    @BindsInstance
    fun setApplication(application: Application): Builder

    fun build(): ApplicationComponent
  }

  fun getActivityComponentBuilderProvider(): Provider<ActivityComponentImpl.Builder>

  fun getApplicationStartupListeners(): Set<ApplicationStartupListener>

  fun getAnalyticsStartupListenerStartupListeners(): Set<AnalyticsStartupListener>

  fun getWorkManagerConfiguration(): Configuration
}
