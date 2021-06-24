package org.oppia.android.domain.platformparameter

import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import org.oppia.android.util.platformparameter.Param1
import org.oppia.android.util.platformparameter.Param1Default
import org.oppia.android.util.platformparameter.Param1Name
import org.oppia.android.util.platformparameter.Param2
import org.oppia.android.util.platformparameter.Param2Default
import org.oppia.android.util.platformparameter.Param2Name
import org.oppia.android.util.platformparameter.Param3
import org.oppia.android.util.platformparameter.Param3Default
import org.oppia.android.util.platformparameter.Param3Name
import org.oppia.android.util.platformparameter.PlatformParameter

/* Platform Parameter Module that provides values for the individual Platform Parameters. */
@Module
class PlatformParameterModule {

  @Provides
  @Singleton
  @Param1
  fun provideParam1(singleton: PlatformParameterSingleton): PlatformParameter<String> {
    return singleton.getStringPlatformParameter(Param1Name)
      ?: PlatformParameter.createDefaultParameter(Param1Default)
  }

  @Provides
  @Singleton
  @Param2
  fun provideParam2(singleton: PlatformParameterSingleton): PlatformParameter<Int> {
    return singleton.getIntegerPlatformParameter(Param2Name)
      ?: PlatformParameter.createDefaultParameter(Param2Default)
  }

  @Provides
  @Singleton
  @Param3
  fun provideParam3(singleton: PlatformParameterSingleton): PlatformParameter<Boolean> {
    return singleton.getBooleanPlatformParameter(Param3Name)
      ?: PlatformParameter.createDefaultParameter(Param3Default)
  }
}
