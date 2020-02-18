package org.oppia.app.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Units(
  @field:SerializedName("name")
  @field:Expose
  var name: String, @SerializedName("aliases")
  @Expose
  var aliases: List<String>, @SerializedName("front_units")
  @Expose
  var frontUnits: List<String>, @field:SerializedName("base_unit")
  @field:Expose
  var baseUnit: String?
)
