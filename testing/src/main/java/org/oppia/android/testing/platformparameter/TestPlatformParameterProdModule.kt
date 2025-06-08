package testing.src.main.java.org.oppia.android.testing.platformparameter

import dagger.Module
import dagger.Provides
import org.oppia.android.domain.platformparameter.PlatformParameterController
import org.oppia.android.domain.platformparameter.PlatformParameterControllerProdImpl
import org.oppia.android.domain.platformparameter.PlatformParameterProcessState
import javax.inject.Singleton

@Module
class TestPlatformParameterProdModule {

  private val processState by lazy { PlatformParameterProcessState() }

  @Provides
  @Singleton
  fun providePlatformParameterController(
    factory: PlatformParameterControllerProdImpl.Factory
  ): PlatformParameterController = factory.create(processState)
}
