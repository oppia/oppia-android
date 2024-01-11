package org.oppia.android.domain.auth

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/** Provides an implementation of [FirebaseAuthWrapper]. */
@Module
class AuthenticationModule {
  @Provides
  @Singleton
  fun provideFirebaseAuthWrapper(firebaseAuthInstanceWrapperImpl: FirebaseAuthInstanceWrapperImpl):
    FirebaseAuthWrapper = FirebaseAuthWrapperImpl(firebaseAuthInstanceWrapperImpl)
}
