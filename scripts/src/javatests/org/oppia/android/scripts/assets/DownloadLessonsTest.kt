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
    structures = jsonAdapter.fromJson(
      File("scripts/assets/practice_download_test.json").readText()
    ) as Map<*, *>
    questions = (
      jsonAdapter.fromJson(
        File("scripts/assets/practice_question_test.json").readText()
      ) as List<*>
      ).take(10).map { it as Map<*, *> }
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
    assertThat(topic.hasPracticeQuestions).isTrue()
    assertThat(requests.filter { it["activity_type"] == "questions" }.map { it["offset"] })
      .containsExactly("0", "6", "10").inOrder()
    assertThat(File(output, "protov1/textproto/questions.textproto").isFile).isTrue()
    assertThat(File(output, "images/thumb.png").isFile).isTrue()
    System.getenv("TEST_UNDECLARED_OUTPUTS_DIR")?.let { path ->
      File(output, "protov1/binary/questions.pb")
        .copyTo(File(path, "pipeline-public-questions.pb"), overwrite = true)
    }
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
    assertThat(asset.questionsCount).isEqualTo(10)
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
      if (includeQuestions) questions.drop(offset).take(6).map {
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
}
