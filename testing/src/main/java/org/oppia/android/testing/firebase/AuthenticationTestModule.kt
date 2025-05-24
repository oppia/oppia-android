package org.oppia.android.testing.firebase

import dagger.Module
import dagger.Provides
import org.oppia.android.domain.auth.FirebaseAuthInstanceWrapper
import org.oppia.android.domain.auth.FirebaseAuthWrapper
import javax.inject.Singleton

/** Provides test authentication dependencies. */
@Module
class AuthenticationTestModule {
  @Provides
  @Singleton
  fun provideFakeFirebaseAuthWrapper(fakeFirebaseWrapperImpl: FakeFirebaseAuthWrapperImpl):
    FirebaseAuthWrapper = fakeFirebaseWrapperImpl

  @Provides
  @Singleton
  fun provideFirebaseAuthInstanceWrapper(): FirebaseAuthInstanceWrapper =
    error("FirebaseAuthInstanceWrapper should never be used in tests binding this module.")
}
