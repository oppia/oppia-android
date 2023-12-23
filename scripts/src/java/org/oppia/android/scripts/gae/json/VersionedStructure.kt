package org.oppia.android.scripts.gae.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VersionedStructure<T>(
  @Json(name = "id") val id: String,
  @Json(name = "payload") val payload: T,
  @Json(name = "language_code") val languageCode: String?,
  @Json(name = "version") val version: Int?
) {
  val expectedVersion: Int
    get() = checkNotNull(version) { "Expected activity $id to be versioned." }
}
