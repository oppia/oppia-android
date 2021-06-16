import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Dependencies(

  @Json(name = "dependencies") val dependencyList: MutableList<Dependency>

)