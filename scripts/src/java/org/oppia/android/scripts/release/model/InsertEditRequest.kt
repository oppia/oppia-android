package org.oppia.android.scripts.release.model

import com.squareup.moshi.JsonClass

/**
 * Represents the (empty) request body for the Google Play Developer API's `edits/insert` endpoint.
 *
 * The Play Console API accepts an empty JSON object `{}` as the body for edit creation. This class
 * exists to keep the Retrofit service interface consistent with other endpoints that use typed
 * Moshi objects for their request bodies.
 *
 * API reference: https://developers.google.com/android-publisher/api-ref/rest/v3/edits/insert
 */
@JsonClass(generateAdapter = true)
class InsertEditRequest
