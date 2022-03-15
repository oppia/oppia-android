package org.oppia.android.testing.logging

import dagger.Binds
import dagger.Module
import org.oppia.android.util.system.UserIdGenerator

/** Module for providing test-only user ID logging utilities. */
@Module
interface UserIdTestModule {
  @Binds
  fun bindUserIdGenerator(impl: FakeUserIdGenerator): UserIdGenerator
}
