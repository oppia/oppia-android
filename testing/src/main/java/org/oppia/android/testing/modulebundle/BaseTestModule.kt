package org.oppia.android.testing.modulebundle

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * The fundamental Dagger test [Module] needed for essentially all tests as it provides
 * [Application] as a [Context].
 */
@Module
class BaseTestModule {
  @Provides
  @Singleton
  fun provideContext(application: Application): Context {
    return application
  }
}
