package org.oppia.android.testing

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/** Provides debug authentication dependencies. */
@Module
class TestAuthenticationModule {
  @Provides
  @Singleton
  fun provideFirebaseAuthWrapper(authController: FakeAuthenticationController) = authController
}
