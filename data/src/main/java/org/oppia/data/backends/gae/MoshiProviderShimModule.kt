package org.oppia.data.backends.gae

import dagger.Module
import dagger.Provides

@Module
interface MoshiProviderShimModule {

  @Provides
  fun provideMoshiProviderShim(moshiProviderShimImpl: MoshiProviderShimImpl): MoshiProviderShim
}
