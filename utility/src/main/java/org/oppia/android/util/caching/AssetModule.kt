package org.oppia.android.util.caching

import dagger.Module
import dagger.Provides

/** Provides dependencies corresponding to loading assets. */
@Module
class AssetModule {
  @Provides
  fun provideAssetRepository(impl: AssetRepositoryImpl): AssetRepository = impl
}
