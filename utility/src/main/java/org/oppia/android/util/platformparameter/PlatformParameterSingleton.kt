package org.oppia.android.util.platformparameter

import org.oppia.android.app.model.PlatformParameter

/** Singleton which helps in storing and providing Platform Parameters at runtime. */
interface PlatformParameterSingleton {

  /**
   * Gets the current platformParameterMap.
   *
   * @return [Map<String, PlatformParameter>]
   */
  fun getPlatformParameterMap(): Map<String, PlatformParameter>

  /**
   * Initializes a platformParameterMap in case it is empty.
   *
   * @param platformParameterMap [Map<String, PlatformParameter>]
   * @return [Unit]
   */
  fun setPlatformParameterMap(platformParameterMap: Map<String, PlatformParameter>)

  /**
   * Retrieves a String type Platform Parameter, if it exists in the platformParameterMap.
   *
   * @param platformParameterName [String], Name of the String type Platform Parameter.
   * @return [PlatformParameterValue]? which contains the value for String type Platform Parameter
   */
  fun getStringPlatformParameter(platformParameterName: String): PlatformParameterValue<String>?

  /**
   * Retrieves a Integer type Platform Parameter, if it exists in the platformParameterMap.
   *
   * @param platformParameterName [String], Name of the Integer type Platform Parameter.
   * @return [PlatformParameterValue]? which contains the value for Integer type Platform Parameter
   */
  fun getIntegerPlatformParameter(platformParameterName: String): PlatformParameterValue<Int>?

  /**
   * Retrieves a Boolean type Platform Parameter, if it exists in the platformParameterMap.
   *
   * @param platformParameterName [String], Name of the Boolean type Platform Parameter.
   * @return [PlatformParameterValue]? which contains the value for Boolean type Platform Parameter
   */
  fun getBooleanPlatformParameter(platformParameterName: String): PlatformParameterValue<Boolean>?
}
