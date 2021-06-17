import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class that contains the list of artifacts whose license names and links have
 * to be provided manually.
 */
@JsonClass(generateAdapter = true)
data class BackupDependency(

  @Json(name = "artifacts") val artifacts: MutableSet<License> = mutableSetOf<License>()

)
