package org.oppia.android.scripts.release.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Represents the response from the Google Play Developer API's `edits.bundles.upload` endpoint.
 *
 * @property versionCode the version code assigned to the uploaded AAB by the Play Console
 */
@JsonClass(generateAdapter = true)
data class BundleResponse(@Json(name = "versionCode") val versionCode: Long)
