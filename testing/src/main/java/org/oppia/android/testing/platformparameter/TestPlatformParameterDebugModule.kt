package testing.src.main.java.org.oppia.android.testing.platformparameter

import dagger.Module
import dagger.Provides
import org.oppia.android.domain.platformparameter.PlatformParameterController
import org.oppia.android.domain.platformparameter.PlatformParameterControllerDebugImpl
import org.oppia.android.domain.platformparameter.PlatformParameterDebugController
import javax.inject.Singleton

@Module
class TestPlatformParameterDebugModule {

  @Provides
  @Singleton
  fun providePlatformParameterController(
    impl: PlatformParameterControllerDebugImpl,
  ): PlatformParameterController = impl

  @Provides
  @Singleton
  fun providePlatformParameterDebugController(
    impl: PlatformParameterControllerDebugImpl,
  ): PlatformParameterDebugController = impl
}
