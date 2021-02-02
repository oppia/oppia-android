package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for StateClassifier model
 * @link https://github.com/oppia/oppia/blob/develop/core/controllers/reader.py#L239
 *
 */
@JsonClass(generateAdapter = true)
data class GaeStateClassifier(

  @Json(name = "algorithm_id") val algorithmId: String?,
  @Json(name = "classifier_data") val classifierData: Any?,
  @Json(name = "data_schema_version") val dataSchemaVersion: Int?

)
