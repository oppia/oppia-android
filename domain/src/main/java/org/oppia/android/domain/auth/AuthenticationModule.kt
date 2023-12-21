package org.oppia.android.domain.auth

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/** Provides an implementation of [FirebaseAuthWrapper]. */
@Module
class AuthenticationModule {
  @Provides
  @Singleton
  fun provideFirebaseAuthWrapper():
    FirebaseAuthWrapper = FirebaseAuthWrapperImpl(Firebase.auth)
}
