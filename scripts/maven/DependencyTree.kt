import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DependencyTree(

  @Json(name = "dependency_tree") val dependencies: Dependencies

)