package org.oppia.android.testing

import dagger.Module
import dagger.Provides
import org.oppia.android.domain.auth.AuthenticationListener
import javax.inject.Singleton

/** Provides debug authentication dependencies. */
@Module
class TestAuthenticationModule {
  @Provides
  @Singleton
  fun provideAuthenticationController(authController: FakeAuthenticationController):
    AuthenticationListener = authController
}
