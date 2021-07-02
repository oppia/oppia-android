package org.oppia.android.domain.platformparameter

import org.oppia.android.app.model.PlatformParameter
import org.oppia.android.util.platformparameter.PlatformParameterSingleton
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject
import javax.inject.Singleton

/** Singleton which helps in storing and providing Platform Parameters at runtime. */
@Singleton
class PlatformParameterSingletonImpl @Inject constructor() : PlatformParameterSingleton {
  private var platformParameterMap: Map<String, PlatformParameter> = mapOf()

  override fun getPlatformParameterMap() = platformParameterMap

  override fun setPlatformParameterMap(platformParameterMap: Map<String, PlatformParameter>) {
    if (this.platformParameterMap.isEmpty()) this.platformParameterMap = platformParameterMap
  }

  override fun getStringPlatformParameter(
    platformParameterName: String
  ): PlatformParameterValue<String>? {
    if (platformParameterMap.isEmpty()) return null
    val parameter = platformParameterMap[platformParameterName] ?: return null
    if (!parameter.valueTypeCase.equals(PlatformParameter.ValueTypeCase.STRING)) return null
    return object : PlatformParameterValue<String> {
      override val value: String
        get() = parameter.string
    }
  }

  override fun getIntegerPlatformParameter(
    platformParameterName: String
  ): PlatformParameterValue<Int>? {
    if (platformParameterMap.isEmpty()) return null
    val parameter = platformParameterMap[platformParameterName] ?: return null
    if (!parameter.valueTypeCase.equals(PlatformParameter.ValueTypeCase.INTEGER)) return null
    return object : PlatformParameterValue<Int> {
      override val value: Int
        get() = parameter.integer
    }
  }

  override fun getBooleanPlatformParameter(
    platformParameterName: String
  ): PlatformParameterValue<Boolean>? {
    if (platformParameterMap.isEmpty()) return null
    val parameter = platformParameterMap[platformParameterName] ?: return null
    if (!parameter.valueTypeCase.equals(PlatformParameter.ValueTypeCase.BOOLEAN)) return null
    return object : PlatformParameterValue<Boolean> {
      override val value: Boolean
        get() = parameter.boolean
    }
  }
}
