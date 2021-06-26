package org.oppia.android.domain.platformparameter

import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.app.model.PlatformParameter as ParameterValue
import org.oppia.android.util.platformparameter.PlatformParameter

/** Singleton class which helps in storing and providing Platform Parameters at runtime. */
@Singleton
class PlatformParameterSingleton @Inject constructor() {
  private var platformParameterMap: Map<String, ParameterValue> = mapOf()

  /** Get the current [platformParameterMap]. */
  fun getPlatformParameterMap() = platformParameterMap

  /** Initialize [platformParameterMap] if it is not yet initialised. */
  fun setPlatformParameterMap(map: Map<String, ParameterValue>) {
    if (platformParameterMap.isEmpty()) platformParameterMap = map
  }

  /** Retrieve a String type [ParameterValue], if it exists in the [platformParameterMap]. */
  fun getStringPlatformParameter(name: String): PlatformParameter<String>? {
    if (platformParameterMap.isEmpty()) return null
    val parameter = platformParameterMap[name] ?: return null
    if (!parameter.valueTypeCase.equals(ParameterValue.ValueTypeCase.STRING)) return null
    return object : PlatformParameter<String> {
      override val value: String
        get() = parameter.string
    }
  }

  /** Retrieve an Integer type [ParameterValue], if it exists in the [platformParameterMap]. */
  fun getIntegerPlatformParameter(name: String): PlatformParameter<Int>? {
    if (platformParameterMap.isEmpty()) return null
    val parameter = platformParameterMap[name] ?: return null
    if (!parameter.valueTypeCase.equals(ParameterValue.ValueTypeCase.INTEGER)) return null
    return object : PlatformParameter<Int> {
      override val value: Int
        get() = parameter.integer
    }
  }

  /** Retrieve a Boolean type [ParameterValue], if it exists in the [platformParameterMap]. */
  fun getBooleanPlatformParameter(name: String): PlatformParameter<Boolean>? {
    if (platformParameterMap.isEmpty()) return null
    val parameter = platformParameterMap[name] ?: return null
    if (!parameter.valueTypeCase.equals(ParameterValue.ValueTypeCase.BOOLEAN)) return null
    return object : PlatformParameter<Boolean> {
      override val value: Boolean
        get() = parameter.boolean
    }
  }
}
