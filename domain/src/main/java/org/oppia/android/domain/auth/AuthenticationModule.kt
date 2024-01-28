package org.oppia.android.domain.auth

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/** Provides an implementation of [FirebaseAuthWrapper]. */
@Module
class AuthenticationModule {
  @Provides
  @Singleton
  fun provideFirebaseAuthWrapper(firebaseAuthInstanceWrapper: FirebaseAuthInstanceWrapper):
    FirebaseAuthWrapper = FirebaseAuthWrapperImpl(firebaseAuthInstanceWrapper)

  @Provides
  @Singleton
  fun provideFirebaseAuthInstanceWrapper(): FirebaseAuthInstanceWrapper =
    FirebaseAuthInstanceWrapperImpl()
}
