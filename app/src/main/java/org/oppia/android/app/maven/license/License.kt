package org.oppia.android.app.maven.license

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Data class that stores the information about license text. */
@JsonClass(generateAdapter = true)
data class License(

  @Json(name = "name") val name: String,
  @Json(name = "extracted_link") val extractedLink: String,
  @Json(name = "alternate_link") val alternateLink: String,
  @Json(name = "link_type") val linkType: LinkType = LinkType.UNSPECIFIED

)
