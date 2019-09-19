package org.oppia.data.backends.gae

/**
 * [Exception] that corresponds to an HTTP failure as determined by OkHttp and is thrown when such a
 * failure is encountered.
 */
class HttpFailureException(
  errorCode: Int, errorString: String?
) : Exception("HTTP status code: $errorCode. Error response: $errorString.")
