package org.oppia.data.backends.gae

/** An object that contains functions and constants specifically related to network only. */
object NetworkSettings {

  private var isDeveloperMode: Boolean = true

  /** DEVELOPER URL which connects to development server */
  // TODO(#74): Move this to DI graph
  private const val DEVELOPER_URL = "https://oppia.org"
  /**  PRODUCTION URL which connects to production server */
  private const val PROD_URL = "https://oppia.org"
  /**
   * Prefix in Json response for extra layer of security in API calls
   * https://github.com/oppia/oppia/blob/8f9eed/feconf.py#L319
   * Remove this prefix from every Json response which is achieved in [NetworkInterceptor]
   */
  const val XSSI_PREFIX = ")]}\'"

  fun getBaseUrl(): String {
    return if (isDeveloperMode) {
      DEVELOPER_URL
    } else {
      PROD_URL
    }
  }
}
