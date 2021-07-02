package org.oppia.android.app.maven

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Data class to store the list of [MavenDependency]. */
@JsonClass(generateAdapter = true)
data class MavenDependencies(

  @Json(name="dependencies") val dependencies: List<MavenDependency> = arrayListOf()

)
