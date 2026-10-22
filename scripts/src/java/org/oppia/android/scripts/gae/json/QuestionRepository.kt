package org.oppia.android.scripts.gae.json

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/** Downloads question pages and retains exact versions for reproducible asset builds. */
class QuestionRepository(
  private val cacheDir: File?,
  private val forceCacheLoad: Boolean,
  moshi: Moshi,
  private val fetchPage: suspend (Int) -> List<VersionedStructure<GaeQuestion>>
) {
  private val questionAdapter = moshi.adapter<VersionedStructure<GaeQuestion>>(
    Types.newParameterizedType(VersionedStructure::class.java, GaeQuestion::class.java)
  )
  private val listAdapter = moshi.adapter<List<VersionedStructure<GaeQuestion>>>(
    Types.newParameterizedType(
      List::class.java,
      Types.newParameterizedType(VersionedStructure::class.java, GaeQuestion::class.java)
    )
  )

  /** Loads the requested pins, or the latest question inventory when [pinnedVersions] is null. */
  suspend fun fetchQuestions(
    pinnedVersions: Map<String, Int>? = null
  ): List<VersionedStructure<GaeQuestion>> {
    pinnedVersions?.forEach { (id, version) ->
      require(id.matches(Regex("[A-Za-z0-9_-]+")) && version >= 1) {
        "Invalid question pin: $id version $version."
      }
    }
    val cachedPins = pinnedVersions?.mapValues { (id, version) ->
      cacheDir?.let { File(it, "question_${id}_$version.json") }?.takeIf { it.exists() }?.let {
        validateQuestion(checkNotNull(questionAdapter.fromJson(it.readText()))).also { question ->
          check(question.id == id && question.expectedVersion == version) {
            "Cached question does not match pin $id version $version."
          }
        }
      }
    }
    if (cachedPins != null && cachedPins.values.all { it != null }) {
      return cachedPins.values.map { checkNotNull(it) }
    }
    val latestQuestions = if (forceCacheLoad) {
      val snapshot = cacheDir?.let { File(it, "questions_latest.json") }
      check(snapshot != null && snapshot.exists()) {
        "Question inventory is missing from the forced cache. Download questions before using it."
      }
      validateQuestions(checkNotNull(listAdapter.fromJson(snapshot.readText())))
    } else {
      downloadLatestQuestions().also(::saveQuestions)
    }
    if (pinnedVersions == null) return latestQuestions
    val latestById = latestQuestions.associateBy { it.id }
    return pinnedVersions.map { (id, version) ->
      val question = cachedPins?.get(id) ?: checkNotNull(latestById[id]) {
        "Pinned question $id version $version was not downloaded."
      }
      check(question.expectedVersion == version) {
        "Pinned question $id requires version $version, but the server returned" +
          " ${question.expectedVersion}. The question endpoint only serves latest versions;" +
          " use the exact cached version or refresh the question pins."
      }
      question
    }
  }

  private suspend fun downloadLatestQuestions(): List<VersionedStructure<GaeQuestion>> {
    val questions = mutableListOf<VersionedStructure<GaeQuestion>>()
    val ids = mutableSetOf<String>()
    while (true) {
      val page = fetchPage(questions.size)
      if (page.isEmpty()) break
      page.map(::validateQuestion).forEach { question ->
        check(ids.add(question.id)) { "Question pagination repeated ID: ${question.id}." }
        questions += question
      }
    }
    return questions
  }

  private fun validateQuestions(
    questions: List<VersionedStructure<GaeQuestion>>
  ): List<VersionedStructure<GaeQuestion>> {
    check(questions.map { it.id }.distinct().size == questions.size) {
      "Question inventory contains duplicate IDs."
    }
    return questions.map(::validateQuestion)
  }

  private fun validateQuestion(
    question: VersionedStructure<GaeQuestion>
  ): VersionedStructure<GaeQuestion> {
    check(question.id == question.payload.id && question.id.matches(Regex("[A-Za-z0-9_-]+"))) {
      "Question response has an invalid or mismatched ID: ${question.id}."
    }
    check(question.payload.version >= 1) { "Question ${question.id} has an invalid version." }
    check(question.version == null || question.version == question.payload.version) {
      "Question ${question.id} has conflicting content versions."
    }
    return question.copy(version = question.payload.version)
  }

  private fun saveQuestions(questions: List<VersionedStructure<GaeQuestion>>) {
    val directory = cacheDir ?: return
    check(directory.isDirectory || directory.mkdirs()) {
      "Cannot create question cache: $directory."
    }
    questions.forEach { question ->
      val file = File(directory, "question_${question.id}_${question.expectedVersion}.json")
      if (file.exists()) {
        check(questionAdapter.fromJson(file.readText()) == question) {
          "Question ${question.id} changed without a content-version update."
        }
      } else {
        writeAtomically(file, questionAdapter.indent("  ").toJson(question))
      }
    }
    writeAtomically(
      File(directory, "questions_latest.json"), listAdapter.indent("  ").toJson(questions)
    )
  }

  private fun writeAtomically(file: File, content: String) {
    val temporary = File.createTempFile(file.name, ".tmp", file.parentFile)
    try {
      temporary.writeText(content)
      Files.move(
        temporary.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE,
        StandardCopyOption.REPLACE_EXISTING
      )
    } finally {
      temporary.delete()
    }
  }
}
