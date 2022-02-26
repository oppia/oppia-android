package org.oppia.android.util.profile

import dagger.Binds
import dagger.Module

@Module
interface ProfileNameValidatorModule {
  @Binds
  fun bindProfileNameValidator(impl: ProfileNameValidatorImpl):
    ProfileNameValidator
}
