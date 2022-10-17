package org.oppia.android.domain.oppialogger.analytics

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import org.oppia.android.domain.oppialogger.ApplicationStartupListener

/** Binds [ActivityLifecycleObserver] as an [ApplicationStartupListener] */
@Module
interface ActivityLifecycleObserverModule {
  @Binds
  @IntoSet
  fun bindActivityLifecycleObserver(
    activityLifecycleObserver: ActivityLifecycleObserver
  ): ApplicationStartupListener
}
