package org.oppia.android.testing.firebase

import dagger.Module
import dagger.Provides
import org.oppia.android.domain.auth.FirebaseAuthWrapper
import javax.inject.Singleton

/** Provides test authentication dependencies. */
@Module
class TestAuthenticationModule {
  @Provides
  @Singleton
  fun provideFakeFirebaseAuthWrapper(fakeFirebaseWrapperImpl: FakeFirebaseAuthWrapperImpl):
    FirebaseAuthWrapper = fakeFirebaseWrapperImpl
}
