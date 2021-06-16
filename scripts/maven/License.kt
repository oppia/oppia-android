import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class License(

  @Json(name = "artifact_name") val artifactName: String,
  @Json(name = "license_names") val licenseNames: MutableList<String>,
  @Json(name = "license_links") val licenseLinks: MutableList<String>

)