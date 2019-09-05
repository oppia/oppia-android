// Code generated by moshi-kotlin-codegen. Do not edit.
package org.oppia.data.backends.gae.model

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.NullPointerException
import kotlin.Float
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map

class GaeTopicJsonAdapter(moshi: Moshi) : JsonAdapter<GaeTopic>() {
    private val options: JsonReader.Options =
            JsonReader.Options.of("topic_id", "topic_name", "canonical_story_dicts", "additional_story_dicts", "skill_descriptions", "degrees_of_mastery", "uncategorized_skill_ids", "subtopics")

    private val nullableStringAdapter: JsonAdapter<String?> =
            moshi.adapter<String?>(String::class.java, kotlin.collections.emptySet(), "topicId")

    private val nullableListOfNullableGaeStorySummaryAdapter: JsonAdapter<List<GaeStorySummary?>?> =
            moshi.adapter<List<GaeStorySummary?>?>(Types.newParameterizedType(List::class.java, GaeStorySummary::class.java), kotlin.collections.emptySet(), "canonicalStoryDicts")

    private val nullableMapOfStringNullableStringAdapter: JsonAdapter<Map<String, String?>?> =
            moshi.adapter<Map<String, String?>?>(Types.newParameterizedType(Map::class.java, String::class.java, String::class.java), kotlin.collections.emptySet(), "skillDescriptions")

    private val nullableMapOfStringNullableFloatAdapter: JsonAdapter<Map<String, Float?>?> =
            moshi.adapter<Map<String, Float?>?>(Types.newParameterizedType(Map::class.java, String::class.java, Float::class.javaObjectType), kotlin.collections.emptySet(), "degreesOfMastery")

    private val nullableListOfNullableStringAdapter: JsonAdapter<List<String?>?> =
            moshi.adapter<List<String?>?>(Types.newParameterizedType(List::class.java, String::class.java), kotlin.collections.emptySet(), "uncategorizedSkillIds")

    private val nullableListOfNullableGaeSubtopicSummaryAdapter: JsonAdapter<List<GaeSubtopicSummary?>?> =
            moshi.adapter<List<GaeSubtopicSummary?>?>(Types.newParameterizedType(List::class.java, GaeSubtopicSummary::class.java), kotlin.collections.emptySet(), "subtopics")

    override fun toString(): String = "GeneratedJsonAdapter(GaeTopic)"

    override fun fromJson(reader: JsonReader): GaeTopic {
        var topicId: String? = null
        var topicName: String? = null
        var canonicalStoryDicts: List<GaeStorySummary?>? = null
        var additionalStoryDicts: List<GaeStorySummary?>? = null
        var skillDescriptions: Map<String, String?>? = null
        var degreesOfMastery: Map<String, Float?>? = null
        var uncategorizedSkillIds: List<String?>? = null
        var subtopics: List<GaeSubtopicSummary?>? = null
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.selectName(options)) {
                0 -> topicId = nullableStringAdapter.fromJson(reader)
                1 -> topicName = nullableStringAdapter.fromJson(reader)
                2 -> canonicalStoryDicts = nullableListOfNullableGaeStorySummaryAdapter.fromJson(reader)
                3 -> additionalStoryDicts = nullableListOfNullableGaeStorySummaryAdapter.fromJson(reader)
                4 -> skillDescriptions = nullableMapOfStringNullableStringAdapter.fromJson(reader)
                5 -> degreesOfMastery = nullableMapOfStringNullableFloatAdapter.fromJson(reader)
                6 -> uncategorizedSkillIds = nullableListOfNullableStringAdapter.fromJson(reader)
                7 -> subtopics = nullableListOfNullableGaeSubtopicSummaryAdapter.fromJson(reader)
                -1 -> {
                    // Unknown name, skip it.
                    reader.skipName()
                    reader.skipValue()
                }
            }
        }
        reader.endObject()
        var result = GaeTopic(
                topicId = topicId,
                topicName = topicName,
                canonicalStoryDicts = canonicalStoryDicts,
                additionalStoryDicts = additionalStoryDicts,
                skillDescriptions = skillDescriptions,
                degreesOfMastery = degreesOfMastery,
                uncategorizedSkillIds = uncategorizedSkillIds,
                subtopics = subtopics)
        return result
    }

    override fun toJson(writer: JsonWriter, value: GaeTopic?) {
        if (value == null) {
            throw NullPointerException("value was null! Wrap in .nullSafe() to write nullable values.")
        }
        writer.beginObject()
        writer.name("topic_id")
        nullableStringAdapter.toJson(writer, value.topicId)
        writer.name("topic_name")
        nullableStringAdapter.toJson(writer, value.topicName)
        writer.name("canonical_story_dicts")
        nullableListOfNullableGaeStorySummaryAdapter.toJson(writer, value.canonicalStoryDicts)
        writer.name("additional_story_dicts")
        nullableListOfNullableGaeStorySummaryAdapter.toJson(writer, value.additionalStoryDicts)
        writer.name("skill_descriptions")
        nullableMapOfStringNullableStringAdapter.toJson(writer, value.skillDescriptions)
        writer.name("degrees_of_mastery")
        nullableMapOfStringNullableFloatAdapter.toJson(writer, value.degreesOfMastery)
        writer.name("uncategorized_skill_ids")
        nullableListOfNullableStringAdapter.toJson(writer, value.uncategorizedSkillIds)
        writer.name("subtopics")
        nullableListOfNullableGaeSubtopicSummaryAdapter.toJson(writer, value.subtopics)
        writer.endObject()
    }
}
