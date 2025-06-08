package testing.src.main.java.org.oppia.android.testing.platformparameter

import dagger.Module
import dagger.Provides
import org.oppia.android.domain.platformparameter.PlatformParameterController
import org.oppia.android.domain.platformparameter.PlatformParameterControllerDebugImpl
import org.oppia.android.domain.platformparameter.PlatformParameterControllerProdImpl
import org.oppia.android.domain.platformparameter.PlatformParameterDebugController
import org.oppia.android.domain.platformparameter.PlatformParameterProcessState
import javax.inject.Singleton

@Module
class TestPlatformParameterDebugModule {

  private val processState by lazy { PlatformParameterProcessState() }
  @Provides
  @Singleton
  fun providePlatformParameterController(
    impl: PlatformParameterControllerDebugImpl,
  ): PlatformParameterController = impl

  @Provides
  @Singleton
  fun providePlatformParameterControllerProdImpl(
    factory: PlatformParameterControllerProdImpl.Factory
  ): PlatformParameterControllerProdImpl = factory.create(processState)
  @Provides
  @Singleton
  fun providePlatformParameterDebugController(
    impl: PlatformParameterControllerDebugImpl,
  ): PlatformParameterDebugController = impl
}
