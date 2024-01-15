package org.oppia.android.testing

import dagger.Module
import dagger.Provides
import org.oppia.android.domain.auth.FirebaseAuthWrapper

/** Provides test authentication dependencies. */
@Module
class TestAuthenticationModule {
  @Provides
  fun provideFakeFirebaseAuthWrapper(fakeFirebaseWrapperImpl: FakeFirebaseAuthWrapperImpl):
    FirebaseAuthWrapper = fakeFirebaseWrapperImpl
}
