package org.oppia.android.util.caching.testing

import dagger.Module
import dagger.Provides
import org.oppia.android.util.caching.AssetRepository

/** Test-only module for loading assets. */
@Module
class AssetTestModule {
  @Provides
  fun provideAssetRepository(impl: TestAssetRepository): AssetRepository = impl
}
