package org.oppia.android.util.caching.testing

import dagger.Module
import dagger.Provides
import org.oppia.android.util.caching.AssetRepository

/** Test-only module for no-op loading assets. */
@Module
class AssetTestNoOpModule {
  @Provides
  fun provideAssetRepository(impl: TestNoOpAssetRepository): AssetRepository = impl
}
