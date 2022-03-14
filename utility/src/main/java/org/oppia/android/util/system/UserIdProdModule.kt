package org.oppia.android.util.system

import dagger.Binds
import dagger.Module

/** Module for providing user ID-related system utilities. */
@Module
interface UserIdProdModule {
  @Binds
  fun bindUserIdGenerator(impl: UserIdGeneratorImpl): UserIdGenerator
}
