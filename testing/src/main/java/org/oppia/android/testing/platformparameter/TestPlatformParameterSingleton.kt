package org.oppia.android.testing.platformparameter

import org.oppia.android.domain.platformparameter.PlatformParameterSingleton
import org.oppia.android.util.platformparameter.PlatformParameter
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.app.model.PlatformParameter as ParameterValue

/** Fake Singleton class which helps in testing the [PlatformParameterSingleton] Logic. */
@Singleton
class TestPlatformParameterSingleton @Inject constructor() : PlatformParameterSingleton() {

  /** This [mockPlatformParameterMap] will get initialised by the Test class when needed. */
  companion object {
    var mockPlatformParameterMap: Map<String, ParameterValue> = mapOf()
  }

  /**
   * We override the [getStringPlatformParameter] function to set the [platformParameterMap] to be
   * equal to the [mockPlatformParameterMap] which was initialized by test classes.
   * */
  override fun getStringPlatformParameter(name: String): PlatformParameter<String>? {
    setPlatformParameterMap(mockPlatformParameterMap)
    return super.getStringPlatformParameter(name)
  }

  /**
   * We override the [getIntegerPlatformParameter] function to set the [platformParameterMap] to be
   * equal to the [mockPlatformParameterMap] which was initialized by test classes.
   * */
  override fun getIntegerPlatformParameter(name: String): PlatformParameter<Int>? {
    setPlatformParameterMap(mockPlatformParameterMap)
    return super.getIntegerPlatformParameter(name)
  }

  /**
   * We override the [getBooleanPlatformParameter] function to set the [platformParameterMap] to be
   * equal to the [mockPlatformParameterMap] which was initialized by test classes.
   * */
  override fun getBooleanPlatformParameter(name: String): PlatformParameter<Boolean>? {
    setPlatformParameterMap(mockPlatformParameterMap)
    return super.getBooleanPlatformParameter(name)
  }
}
