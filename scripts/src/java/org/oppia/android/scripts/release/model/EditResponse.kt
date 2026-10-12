package org.oppia.android.scripts.release.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Represents the response from the Google Play Developer API's `edits.insert` endpoint.
 *
 * @property id the unique identifier for the newly created edit session
 */
@JsonClass(generateAdapter = true)
data class EditResponse(@Json(name = "id") val id: String)
