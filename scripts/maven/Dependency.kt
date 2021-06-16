import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Dependency(

  @Json(name = "coord") val coord: String,
  @Json(name = "dependencies") val dependencies: List<String>? = null,
  @Json(name = "directDependencies") val directDependencies: List<String>? = null,
  @Json(name = "file") val file: String? = null,
  @Json(name = "mirror_urls") val mirrorUrls: List<String>? = null,
  @Json(name = "sha256") val sha: String? = null,
  @Json(name = "url") val url: String? = null

)