package org.oppia.android.testing

import dagger.Module
import dagger.Provides
import org.oppia.android.domain.auth.AuthenticationWrapper
import javax.inject.Singleton

/** Provides debug authentication dependencies. */
@Module
class TestAuthenticationModule {
  @Provides
  @Singleton
  fun provideAuthenticationController(authController: FakeAuthenticationController):
    AuthenticationWrapper = authController
}
