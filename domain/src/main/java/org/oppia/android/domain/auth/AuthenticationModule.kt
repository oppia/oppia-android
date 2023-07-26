package org.oppia.android.domain.auth

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/** Provides an implementation of FirebaseAuth. */
@Module
class AuthenticationModule {
  @Provides
  @Singleton
  fun provideAuthenticationController(factory: AuthenticationController.Factory):
    AuthenticationListener = factory.create()
}
