package org.oppia.android.scripts.gae.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

@JsonClass(generateAdapter = false)
data class GaeRuleSpec(
  val ruleType: String,
  val inputs: Map<String, @JvmSuppressWildcards GaeInteractionObject>
) {
  class Adapter(private val typeResolutionContext: TypeResolutionContext) {
    @FromJson
    fun parseFromJson(
      jsonReader: JsonReader,
      parsableRuleSpecAdapter: JsonAdapter<ParsableRuleSpec>
    ): GaeRuleSpec {
      typeResolutionContext.currentRuleTypeName = jsonReader.peekRuleType()
      return jsonReader.nextCustomValue(parsableRuleSpecAdapter).also {
        // Reset the rule type & input name.
        typeResolutionContext.currentRuleInputName = null
        typeResolutionContext.currentRuleTypeName = null
      }.convertToGaeObject()
    }

    @ToJson
    fun convertToJson(jsonWriter: JsonWriter, gaeRuleSpec: GaeRuleSpec): Unit =
      error("Conversion to JSON is not supported.")

    @GaeInteractionObject.RuleInput
    @FromJson
    fun parseRuleInputMapFromJson(
      jsonReader: JsonReader,
      @GaeInteractionObject.RuleInput inputAdapter: JsonAdapter<GaeInteractionObject>
    ): Map<String, @JvmSuppressWildcards GaeInteractionObject> {
      return jsonReader.nextObject { key ->
        typeResolutionContext.currentRuleInputName = key
        jsonReader.nextCustomValue(inputAdapter)
      }
    }

    @ToJson
    fun convertRuleInputToJson(
      jsonWriter: JsonWriter,
      @GaeInteractionObject.RuleInput
      inputs: Map<String, @JvmSuppressWildcards GaeInteractionObject>
    ): Unit = error("Conversion to JSON is not supported.")

    @JsonClass(generateAdapter = true)
    data class ParsableRuleSpec(
      @Json(name = "rule_type") val ruleType: String,
      @Json(name = "inputs")
      @GaeInteractionObject.RuleInput
      val inputs: Map<String, @JvmSuppressWildcards GaeInteractionObject>
    ) {
      fun convertToGaeObject(): GaeRuleSpec = GaeRuleSpec(ruleType, inputs)
    }
  }

  private companion object {
    private fun JsonReader.peekRuleType(): String {
      return peekJson().use { jsonReader ->
        jsonReader.nextObject {
          if (it == "rule_type") jsonReader.nextString() else null
        }["rule_type"] ?: error("Missing rule type in rule spec JSON object.")
      }
    }
  }
}
