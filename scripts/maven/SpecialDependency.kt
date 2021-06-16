import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SpecialDependency(

  @Json(name = "artifacts_list") val artifactsList: MutableList<License> = mutableListOf<License>()

)