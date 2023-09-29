package org.oppia.android.domain.auth

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/** Provides an implementation of [AuthenticationListener]. */
@Module
class AuthenticationModule {
  @Provides
  @Singleton
  fun provideAuthenticationController():
    AuthenticationListener = AuthenticationController(Firebase.auth)
}
