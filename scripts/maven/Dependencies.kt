import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Data class that stores the list of dependencies present in maven_install.json. */
@JsonClass(generateAdapter = true)
data class Dependencies(

  @Json(name = "dependencies") val dependencyList: MutableList<Dependency>

)
