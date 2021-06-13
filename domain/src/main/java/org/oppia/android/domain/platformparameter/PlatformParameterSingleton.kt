package org.oppia.android.domain.platformparameter

import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.app.model.PlatformParameter as ParameterValue
import org.oppia.android.util.platformparameter.PlatformParameter

/** Singleton class which helps in storing and providing Platform Parameters at runtime. */
@Singleton
class PlatformParameterSingleton @Inject constructor() {
  lateinit var platformParameterMap: Map<String, ParameterValue>

  /** Initialize platformParameterMap if it is not yet initialised. */
  fun initPlatformParameterMap(map: Map<String, ParameterValue>) {
    if (platformParameterMap.isEmpty()) platformParameterMap = map
  }

  /** Retrieve individual PlatformParameter, if it exists in the platformParameterMap. */
  inline fun <reified T> getPlatformParameter(name: String): PlatformParameter<T>? {
    if (platformParameterMap.isEmpty()) return null
    val parameter = platformParameterMap[name] ?: return null
    return object : PlatformParameter<T> {
      override val value: T
        get() = when (T::class) {
          String::class -> parameter.string as T
          Int::class -> parameter.integer as T
          else -> parameter.boolean as T
        }
    }
  }
}