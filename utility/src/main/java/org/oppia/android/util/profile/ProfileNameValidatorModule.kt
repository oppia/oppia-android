package org.oppia.android.util.profile

import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
interface ProfileNameValidatorModule {
  @Binds
  fun bindProfileNameValidator(impl: ProfileNameValidatorImpl):
    ProfileNameValidator
}