package org.oppia.app.application

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import org.oppia.app.activity.ActivityComponent
import org.oppia.util.threading.DispatcherModule
import javax.inject.Provider
import javax.inject.Singleton

/** Root Dagger component for the application. All application-scoped modules should be included in this component. */
@Singleton
@Component(modules = [ApplicationModule::class, DispatcherModule::class])
interface ApplicationComponent {
  @Component.Builder
  interface Builder {
    @BindsInstance fun setApplication(application: Application): Builder
    fun build(): ApplicationComponent
  }

  fun getActivityComponentBuilderProvider(): Provider<ActivityComponent.Builder>
}
