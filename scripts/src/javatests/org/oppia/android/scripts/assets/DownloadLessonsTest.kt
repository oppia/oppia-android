package org.oppia.android.scripts.assets

import com.google.common.truth.Truth.assertThat
import com.google.protobuf.TextFormat
import com.squareup.moshi.Moshi
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.app.model.QuestionList
import org.oppia.android.app.model.TopicRecord
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.scripts.gae.proto.OppiaWebTranslationExtractor
import org.oppia.android.scripts.proto.DownloadConfig
import org.oppia.android.scripts.proto.DownloadListVersions
import java.io.File
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.util.Base64
import java.util.Collections
import kotlin.test.assertFailsWith

/** Tests the actual HTTP downloader, question conversion and Android asset writer together. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
class DownloadLessonsTest {
  @get:Rule val temporaryFolder = TemporaryFolder()
  private lateinit var server: HttpServer
  private lateinit var baseUrl: String
  private val requests = Collections.synchronizedList(mutableListOf<Map<String, String>>())
  private val imagePaths = Collections.synchronizedList(mutableListOf<String>())
  private val jsonAdapter = Moshi.Builder().build().adapter(Any::class.java)
  private lateinit var structures: Map<*, *>
  private lateinit var questions: List<Map<*, *>>
  private var includeQuestions = true
  private var sourcePracticeEnabled = true

  @Before
  fun setUp() {
    structures = createLessonStructures()
    questions = (0..2).map { createQuestion(it) }
    server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
    server.createContext("/android_data") { exchange ->
      try {
        serveActivity(exchange)
      } catch (error: Exception) {
        val response = error.stackTraceToString().toByteArray()
        exchange.sendResponseHeaders(500, response.size.toLong())
        exchange.responseBody.use { it.write(response) }
        exchange.close()
      }
    }
    server.createContext("/test_bucket/") { exchange ->
      imagePaths += exchange.requestURI.path
      val png = Base64.getDecoder().decode(
        "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMC" +
          "AO+jK1YAAAAASUVORK5CYII="
      )
      exchange.responseHeaders.add("Content-Length", png.size.toString())
      if (exchange.requestMethod == "HEAD") {
        exchange.sendResponseHeaders(200, -1)
      } else {
        exchange.sendResponseHeaders(200, png.size.toLong())
        exchange.responseBody.use { it.write(png) }
      }
      exchange.close()
    }
    server.start()
    baseUrl = "http://127.0.0.1:${server.address.port}"
  }

  @After
  fun tearDown() {
    server.stop(0)
  }

  @Test
  fun testDownloadLessons_questionsEnabled_packagesQuestionsAndEnablesCoveredTopic() {
    val output = downloadLessons(downloadQuestions = true)

    val asset = QuestionList.parseFrom(File(output, "protov1/binary/questions.pb").readBytes())
    val topic = TopicRecord.parseFrom(File(output, "protov1/binary/test_topic.pb").readBytes())
    assertThat(asset.questionsList.map { it.questionId })
      .containsExactlyElementsIn(questions.map { it["id"] })
    assertThat(asset.questionsList.associate { it.questionId to it.version })
      .containsExactlyEntriesIn(
        questions.associate { it["id"] to (it["version"] as Double).toInt() }
      )
    val expectedContent = questions.map {
      ((it["question_state_data"] as Map<*, *>)["content"] as Map<*, *>)["html"]
    }
    assertThat(asset.questionsList.map { it.questionState.content.html })
      .containsExactlyElementsIn(expectedContent)
    assertThat(topic.hasPracticeQuestions).isTrue()
    assertThat(requests.filter { it["activity_type"] == "questions" }.map { it["offset"] })
      .containsExactly("0", "2", "3").inOrder()
    assertThat(File(output, "protov1/textproto/questions.textproto").isFile).isTrue()
    assertThat(
      TextFormat.parse(
        File(output, "protov1/textproto/questions.textproto").readText(), QuestionList::class.java
      )
    ).isEqualTo(asset)
    assertThat(File(output, "images/thumb.png").isFile).isTrue()
  }

  @Test
  fun testDownloadLessons_questionsDisabled_doesNotRequestOrPackageQuestions() {
    val output = downloadLessons(downloadQuestions = false)

    val topic = TopicRecord.parseFrom(File(output, "protov1/binary/test_topic.pb").readBytes())
    assertThat(File(output, "protov1/binary/questions.pb").exists()).isFalse()
    assertThat(topic.hasPracticeQuestions).isFalse()
    assertThat(requests.none { it["activity_type"] == "questions" }).isTrue()
  }

  @Test
  fun testDownloadLessons_emptyQuestionInventory_keepsPracticeHidden() {
    includeQuestions = false

    val output = downloadLessons(downloadQuestions = true)

    val topic = TopicRecord.parseFrom(File(output, "protov1/binary/test_topic.pb").readBytes())
    val asset = QuestionList.parseFrom(File(output, "protov1/binary/questions.pb").readBytes())
    assertThat(topic.hasPracticeQuestions).isFalse()
    assertThat(asset.questionsCount).isEqualTo(0)
  }

  @Test
  fun testDownloadLessons_sourcePracticeDisabled_keepsPracticeHidden() {
    sourcePracticeEnabled = false

    val output = downloadLessons(downloadQuestions = true)

    val topic = TopicRecord.parseFrom(File(output, "protov1/binary/test_topic.pb").readBytes())
    val asset = QuestionList.parseFrom(File(output, "protov1/binary/questions.pb").readBytes())
    assertThat(topic.hasPracticeQuestions).isFalse()
    assertThat(asset.questionsCount).isEqualTo(0)
  }

  @Test
  fun testDownloadLessons_incompatibleQuestion_excludesItFromBundledAsset() {
    val incompatibleQuestion = questions[0] + ("question_state_data_schema_version" to 1000)
    questions = listOf(incompatibleQuestion) + questions.drop(1)

    val output = downloadLessons(downloadQuestions = true)

    val asset = QuestionList.parseFrom(File(output, "protov1/binary/questions.pb").readBytes())
    val topic = TopicRecord.parseFrom(File(output, "protov1/binary/test_topic.pb").readBytes())
    assertThat(asset.questionsList.map { it.questionId })
      .containsExactlyElementsIn(questions.drop(1).map { it["id"] })
    assertThat(topic.hasPracticeQuestions).isTrue()
  }

  @Test
  fun testDownloadLessons_onlyIncompatibleQuestions_keepsPracticeHidden() {
    questions = questions.map { it + ("question_state_data_schema_version" to 1000) }

    val output = downloadLessons(downloadQuestions = true)

    val asset = QuestionList.parseFrom(File(output, "protov1/binary/questions.pb").readBytes())
    val topic = TopicRecord.parseFrom(File(output, "protov1/binary/test_topic.pb").readBytes())
    assertThat(asset.questionsCount).isEqualTo(0)
    assertThat(topic.hasPracticeQuestions).isFalse()
  }

  @Test
  fun testDownloadLessons_partiallyCoveredTopic_keepsPracticeHidden() {
    val topic = structures["learntopic"] as Map<*, *>
    val subtopics = topic["subtopics"] as List<*>
    val firstSubtopic = subtopics[0] as Map<*, *>
    val uncoveredSubtopic = firstSubtopic + mapOf(
      "id" to 2, "skill_ids" to listOf("uncovered_skill")
    )
    structures = structures + (
      "learntopic" to (topic + ("subtopics" to (subtopics + listOf(uncoveredSubtopic))))
      )

    val output = downloadLessons(downloadQuestions = true)

    val asset = QuestionList.parseFrom(File(output, "protov1/binary/questions.pb").readBytes())
    val topicRecord =
      TopicRecord.parseFrom(File(output, "protov1/binary/test_topic.pb").readBytes())
    assertThat(asset.questionsCount).isEqualTo(questions.size)
    assertThat(topicRecord.subtopicIdsList).hasSize(2)
    assertThat(topicRecord.hasPracticeQuestions).isFalse()
  }

  @Test
  fun testDownloadLessons_questionImage_downloadsFromQuestionDirectory() {
    val first = questions[0]
    val state = first["question_state_data"] as Map<*, *>
    val content = state["content"] as Map<*, *>
    val image = "<oppia-noninteractive-image filepath-with-value=\"&amp;quot;question.png" +
      "&amp;quot;\"></oppia-noninteractive-image>"
    questions = listOf(
      first + (
        "question_state_data" to (
          state + ("content" to (content + ("html" to (content["html"].toString() + image))))
          )
        )
    ) + questions.drop(1)

    val output = downloadLessons(downloadQuestions = true)

    val asset = QuestionList.parseFrom(File(output, "protov1/binary/questions.pb").readBytes())
    assertThat(
      asset.questionsList.single { it.questionId == first["id"] }
        .questionState.content.html
    ).contains("question.png")
    assertThat(File(output, "images/question.png").isFile).isTrue()
    assertThat(imagePaths).contains(
      "/test_bucket/question/${first["id"]}/assets/image/question.png"
    )
  }

  @Test
  fun testDownloadLessons_practiceEnabledWithoutQuestionPins_failsWithInstructions() = runBlocking {
    val pins = capturePins(temporaryFolder.newFolder()).toBuilder()
      .clearTrackedQuestionInfo().build()
    val error = assertFailsWith<IllegalStateException> {
      downloadLessons(true, pins)
    }

    assertThat(error.cause).hasMessageThat().contains("no question versions are pinned")
  }

  @Test
  fun testDownloadLessonList_thenPinnedDownload_preservesExactQuestionVersions() = runBlocking {
    val cache = temporaryFolder.newFolder()
    val pins = capturePins(cache)
    assertThat(pins.trackedQuestionInfoList.associate { it.id to it.contentVersion })
      .containsExactlyEntriesIn(
        questions.associate { it["id"] to (it["version"] as Double).toInt() }
      )
    requests.clear()

    val output = downloadLessons(true, pins, cache)

    val asset = QuestionList.parseFrom(File(output, "protov1/binary/questions.pb").readBytes())
    assertThat(asset.questionsList.associate { it.questionId to it.version })
      .containsExactlyEntriesIn(
        pins.trackedQuestionInfoList.associate { it.id to it.contentVersion }
      )
    assertThat(requests.none { it["activity_type"] == "questions" }).isTrue()
  }

  @Test
  fun testDownloadLessons_pinnedQuestionChanged_failsInsteadOfPackagingLatest() = runBlocking {
    val pins = capturePins(temporaryFolder.newFolder())
    questions = questions.map { question ->
      question + ("version" to ((question["version"] as Double).toInt() + 1))
    }

    val error = assertFailsWith<IllegalStateException> {
      downloadLessons(true, pins)
    }

    assertThat(error.cause).hasMessageThat().contains("only serves latest versions")
  }

  @Test
  fun testDownloadLessons_pinnedQuestionMissing_failsInsteadOfDroppingQuestion() = runBlocking {
    val pins = capturePins(temporaryFolder.newFolder())
    questions = questions.drop(1)

    val error = assertFailsWith<IllegalStateException> {
      downloadLessons(true, pins)
    }

    assertThat(error.cause).hasMessageThat().contains("was not downloaded")
  }

  private suspend fun capturePins(cache: File): DownloadListVersions {
    val pinsFile = temporaryFolder.newFile("versions.textproto")
    ScriptBackgroundCoroutineDispatcher().use { dispatcher ->
      LessonListDownloader(
        baseUrl, baseUrl, "test_bucket", "test_key", cache, dispatcher, createConfig(), true,
        OppiaWebTranslationExtractor.createFromTranslations(emptyMap())
      ).downloadLessonListAsync(pinsFile).await()
    }
    return TextFormat.parse(pinsFile.readText(), DownloadListVersions::class.java)
  }

  private fun createConfig(): DownloadConfig = DownloadConfig.newBuilder()
    .addAllowedClassrooms("test_classroom")
    .addAllowedTopicIds("test_topic")
    .addAllAllowedInteractionIds(
      listOf("TextInput", "NumericInput", "MultipleChoiceInput", "EndExploration")
    )
    .build()

  private fun downloadLessons(
    downloadQuestions: Boolean,
    pins: DownloadListVersions? = null,
    cache: File = temporaryFolder.newFolder()
  ): File {
    val output = temporaryFolder.newFolder()
    LessonDownloader(
      baseUrl, baseUrl, "test_bucket", "test_key", cache, false,
      downloadQuestions, pins, createConfig(),
      OppiaWebTranslationExtractor.createFromTranslations(emptyMap())
    ).downloadLessons(output, failOnError = false)
    return output
  }

  private fun serveActivity(exchange: HttpExchange) {
    val parameters = exchange.requestURI.rawQuery.split("&").associate {
      val parts = it.split("=", limit = 2)
      parts[0] to URLDecoder.decode(parts.getOrElse(1) { "" }, "UTF-8")
    }
    requests += parameters
    check(exchange.requestHeaders.getFirst("X-ApiKey") == "test_key")
    val type = parameters.getValue("activity_type")
    val payload: List<Map<String, Any?>> = if (type == "questions") {
      val offset = parameters.getValue("offset").toInt()
      if (includeQuestions) questions.drop(offset).take(2).map {
        mapOf("id" to it["id"], "payload" to it)
      } else emptyList()
    } else {
      val request = (jsonAdapter.fromJson(parameters.getValue("activities_data")) as List<*>)[0]
        as Map<*, *>
      val data = if (type == "exp_translations") emptyMap<Any, Any>() else {
        val structure = checkNotNull(structures[type]) as Map<*, *>
        val original = if ("id" in structure) structure + ("id" to request["id"]) else structure
        if (type == "learntopic") {
          original + ("practice_tab_is_displayed" to sourcePracticeEnabled)
        } else original
      }
      listOf(
        mapOf(
          "id" to request["id"], "payload" to data, "version" to request["version"],
          "language_code" to request["language_code"]
        )
      )
    }
    val response = (")]}'\n" + jsonAdapter.toJson(payload)).toByteArray()
    exchange.sendResponseHeaders(200, response.size.toLong())
    exchange.responseBody.use { it.write(response) }
    exchange.close()
  }

  private fun createQuestion(index: Int): Map<*, *> = jsonAdapter.fromJson(
    """
    {
      "id": "question_$index",
      "question_state_data": {
        "content": {"content_id": "content", "html": "<p>Write one for question $index.</p>"},
        "param_changes": [],
        "interaction": {
          "id": "TextInput",
          "customization_args": {
            "placeholder": {"value": {"content_id": "placeholder", "unicode_str": "Answer"}},
            "rows": {"value": 1}, "catchMisspellings": {"value": false}
          },
          "answer_groups": [{
            "rule_specs": [{
              "rule_type": "FuzzyEquals",
              "inputs": {"x": {"normalizedStrSet": ["one"], "contentId": "rule_input"}}
            }],
            "outcome": {
              "dest": null, "dest_if_really_stuck": null,
              "feedback": {"content_id": "feedback", "html": "<p>Correct</p>"},
              "labelled_as_correct": true, "param_changes": [],
              "refresher_exploration_id": null, "missing_prerequisite_skill_id": null
            },
            "training_data": [], "tagged_skill_misconception_id": null
          }],
          "default_outcome": {
            "dest": null, "dest_if_really_stuck": null,
            "feedback": {"content_id": "default_feedback", "html": "<p>Try again</p>"},
            "labelled_as_correct": false, "param_changes": [],
            "refresher_exploration_id": null, "missing_prerequisite_skill_id": null
          },
          "confirmed_unclassified_answers": [],
          "hints": [{"hint_content": {"content_id": "hint", "html": "<p>Use letters</p>"}}],
          "solution": {
            "answer_is_exclusive": true, "correct_answer": "one",
            "explanation": {"content_id": "solution", "html": "<p>One is written as one.</p>"}
          }
        },
        "classifier_model_id": null, "linked_skill_id": null,
        "solicit_answer_details": false, "card_is_checkpoint": false,
        "inapplicable_skill_misconception_ids": []
      },
      "question_state_data_schema_version": 57, "language_code": "en", "version": ${index + 1},
      "linked_skill_ids": ["skill_0"], "inapplicable_skill_misconception_ids": [],
      "next_content_id_index": 7
    }
    """.trimIndent()
  ) as Map<*, *>

  private fun createLessonStructures(): Map<*, *> = jsonAdapter.fromJson(
    """
    {
      "classroom": {
        "classroom_id": "test_classroom", "name": "Test classroom",
        "url_fragment": "test_classroom",
        "topic_id_to_prerequisite_topic_ids": {"test_topic": []},
        "course_details": "", "topic_list_intro": "", "teaser_text": "", "is_published": true,
        "thumbnail_data": {}, "banner_data": {}, "diagnostic_test_is_enabled": false, "index": 0
      },
      "learntopic": {
        "id": "test_topic", "name": "Test topic", "abbreviated_name": "Test",
        "url_fragment": "test", "description": "Test",
        "canonical_story_references": [{
          "story_id": "test_story", "story_is_published": true, "story_unpublish_type": null
        }],
        "additional_story_references": [], "uncategorized_skill_ids": [],
        "subtopics": [{
          "id": 1, "title": "Practice", "skill_ids": ["skill_0"], "url_fragment": "practice",
          "thumbnail_filename": "thumb.png", "thumbnail_bg_color": "#ffffff",
          "thumbnail_size_in_bytes": 68
        }],
        "subtopic_schema_version": 1, "next_subtopic_id": 2, "language_code": "en", "version": 1,
        "story_reference_schema_version": 1, "meta_tag_content": "",
        "practice_tab_is_displayed": true, "page_title_fragment_for_web": "Test",
        "skill_ids_for_diagnostic_test": [], "thumbnail_filename": "thumb.png",
        "thumbnail_bg_color": "#ffffff", "thumbnail_size_in_bytes": 68
      },
      "story": {
        "id": "test_story", "title": "Test story", "description": "Test story", "notes": "",
        "language_code": "en", "story_contents_schema_version": 1,
        "corresponding_topic_id": "test_topic", "version": 1,
        "story_contents": {
          "nodes": [{
            "id": "node_1", "title": "Chapter", "description": "Chapter",
            "destination_node_ids": [], "acquired_skill_ids": ["skill_0"],
            "prerequisite_skill_ids": [], "outline": "", "outline_is_finalized": true,
            "exploration_id": "test_exp", "status": "Published",
            "planned_publication_date_msecs": null, "last_modified_msecs": null,
            "first_publication_date_msecs": null, "unpublishing_reason": null,
            "thumbnail_filename": "thumb.png", "thumbnail_bg_color": "#ffffff",
            "thumbnail_size_in_bytes": 68
          }],
          "initial_node_id": "node_1", "next_node_id": "node_2", "arcs": []
        },
        "url_fragment": "test_story", "meta_tag_content": "",
        "thumbnail_filename": "thumb.png", "thumbnail_bg_color": "#ffffff",
        "thumbnail_size_in_bytes": 68
      },
      "exploration": {
        "id": "test_exp", "title": "Test exploration", "category": "Math", "author_notes": "",
        "blurb": "", "states_schema_version": 57, "init_state_name": "End",
        "language_code": "en", "objective": "Test", "param_changes": [], "param_specs": {},
        "tags": [], "auto_tts_enabled": false, "next_content_id_index": 1, "edits_allowed": false,
        "states": {
          "End": {
            "content": {"content_id": "content", "html": "<p>Done</p>"}, "param_changes": [],
            "interaction": {
              "id": "EndExploration",
              "customization_args": {"recommendedExplorationIds": {"value": []}},
              "answer_groups": [], "default_outcome": null, "confirmed_unclassified_answers": [],
              "hints": [], "solution": null
            },
            "classifier_model_id": null, "linked_skill_id": null,
            "solicit_answer_details": false, "card_is_checkpoint": false,
            "inapplicable_skill_misconception_ids": [],
            "recorded_voiceovers": {"voiceovers_mapping": {}}
          }
        },
        "version": 1
      },
      "skill": {
        "id": "skill_0", "description": "Test skill", "misconceptions": [], "rubrics": [],
        "skill_contents": {
          "explanation": {"content_id": "explanation", "html": "<p>Test skill</p>"},
          "recorded_voiceovers": {"voiceovers_mapping": {}},
          "written_translations": {"translations_mapping": {}}
        },
        "language_code": "en", "misconceptions_schema_version": 1, "rubric_schema_version": 1,
        "skill_contents_schema_version": 1, "version": 1, "next_misconception_id": 0,
        "superseding_skill_id": null, "all_questions_merged": false, "prerequisite_skill_ids": []
      },
      "subtopic_with_study_guide": {
        "id": "test_topic-1", "topic_id": "test_topic",
        "sections": [{
          "heading": {"content_id": "heading", "unicode_str": "Test"},
          "content": {"content_id": "content", "html": "<p>Test</p>"}
        }],
        "sections_schema_version": 1, "next_content_id_index": 2, "language_code": "en", "version": 1
      }
    }
    """.trimIndent()
  ) as Map<*, *>
}
