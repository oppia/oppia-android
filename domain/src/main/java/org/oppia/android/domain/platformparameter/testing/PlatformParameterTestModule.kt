package org.oppia.android.domain.platformparameter.testing

import dagger.Binds
import dagger.Module
import org.oppia.android.domain.platformparameter.PlatformParameterConfigRetriever
import org.oppia.android.domain.platformparameter.PlatformParameterController
import org.oppia.android.domain.platformparameter.PlatformParameterControllerProdImpl

@Module(includes = [PlatformParameterGeneratedTestModule::class])
interface PlatformParameterTestModule {
  @Binds
  fun providePlatformParameterController(
    impl: PlatformParameterControllerProdImpl
  ): PlatformParameterController

  @Binds
  fun providePlatformParameterConfigRetriever(
    impl: TestPlatformParameterConfigRetriever
  ): PlatformParameterConfigRetriever
}
