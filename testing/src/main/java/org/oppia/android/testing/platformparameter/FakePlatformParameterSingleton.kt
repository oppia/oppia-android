package org.oppia.android.testing.platformparameter

import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.app.model.PlatformParameter as ParameterValue
import org.oppia.android.util.platformparameter.PlatformParameter

/** Fake Singleton class which helps in testing the Platform Parameter Singleton Logic. */
@Singleton
class FakePlatformParameterSingleton @Inject constructor() {

  /** Initialize [platformParameterMap] if it is not yet initialised. */
  companion object {
    var platformParameterMap: Map<String, ParameterValue> = mapOf()
  }

  /** Retrieve a String type [ParameterValue], if it exists in the [platformParameterMap]. */
  fun getStringPlatformParameter(name: String): PlatformParameter<String>? {
    if (platformParameterMap.isEmpty()) return null
    val parameter = platformParameterMap[name]?.string ?: return null
    return object : PlatformParameter<String> {
      override val value: String
        get() = parameter
    }
  }

  /** Retrieve an Integer type [ParameterValue], if it exists in the [platformParameterMap]. */
  fun getIntegerPlatformParameter(name: String): PlatformParameter<Int>? {
    if (platformParameterMap.isEmpty()) return null
    val parameter = platformParameterMap[name]?.integer ?: return null
    return object : PlatformParameter<Int> {
      override val value: Int
        get() = parameter
    }
  }

  /** Retrieve a Boolean type [ParameterValue], if it exists in the [platformParameterMap]. */
  fun getBooleanPlatformParameter(name: String): PlatformParameter<Boolean>? {
    if (platformParameterMap.isEmpty()) return null
    val parameter = platformParameterMap[name]?.boolean ?: return null
    return object : PlatformParameter<Boolean> {
      override val value: Boolean
        get() = parameter
    }
  }
}
