package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StateClassifier(

  @Json(name = "algorithm_id") val algorithmId: String?,
  @Json(name = "classifier_data") val classifierData: Any?,
  @Json(name = "data_schema_version") val dataSchemaVersion: Int?

)
