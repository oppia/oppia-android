package org.oppia.android.scripts.gae.json

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Types
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.assertFailsWith

/** Tests for question pagination, exact-version caching and pinned downloads. */
@Suppress("FunctionName")
class QuestionRepositoryTest {
  @get:Rule val temporaryFolder = TemporaryFolder()
  private val moshi = MoshiFactory.createMoshi()
  private val question by lazy {
    val adapter = moshi.adapter<List<GaeQuestion>>(
      Types.newParameterizedType(List::class.java, GaeQuestion::class.java)
    )
    checkNotNull(adapter.fromJson(File("scripts/assets/practice_question_test.json").readText()))[0]
  }

  @Test
  fun testFetchQuestions_multiplePages_readsUntilEmptyAndUsesPayloadVersions() = runBlocking {
    val offsets = mutableListOf<Int>()
    val repository = QuestionRepository(null, false, moshi) { offset ->
      offsets += offset
      when (offset) {
        0 -> listOf(createQuestion("first", 2), createQuestion("second", 3))
        2 -> listOf(createQuestion("third", 4))
        else -> emptyList()
      }
    }

    val questions = repository.fetchQuestions()

    assertThat(offsets).containsExactly(0, 2, 3).inOrder()
    assertThat(questions.map { it.expectedVersion }).containsExactly(2, 3, 4).inOrder()
  }

  @Test
  fun testFetchQuestions_repeatedPage_throwsInsteadOfLooping() = runBlocking {
    val repository = QuestionRepository(null, false, moshi) { listOf(createQuestion("first", 1)) }

    val error = assertFailsWith<IllegalStateException> { repository.fetchQuestions() }

    assertThat(error).hasMessageThat().contains("pagination repeated ID")
  }

  @Test
  fun testFetchQuestions_conflictingWrapperVersion_throws() = runBlocking {
    val repository = QuestionRepository(null, false, moshi) {
      listOf(createQuestion("first", 1).copy(version = 2))
    }

    val error = assertFailsWith<IllegalStateException> { repository.fetchQuestions() }

    assertThat(error).hasMessageThat().contains("conflicting content versions")
  }

  @Test
  fun testFetchQuestions_mismatchedId_throws() = runBlocking {
    val repository = QuestionRepository(null, false, moshi) {
      listOf(createQuestion("first", 1).copy(id = "different"))
    }

    val error = assertFailsWith<IllegalStateException> { repository.fetchQuestions() }

    assertThat(error).hasMessageThat().contains("mismatched ID")
  }

  @Test
  fun testFetchQuestions_forcedCache_reusesCompleteSnapshotWithoutNetwork() = runBlocking {
    val directory = temporaryFolder.newFolder()
    val expected = listOf(createQuestion("first", 1))
    QuestionRepository(directory, false, moshi) {
      if (it == 0) expected else emptyList()
    }.fetchQuestions()
    var networkUsed = false

    val actual = QuestionRepository(directory, true, moshi) {
      networkUsed = true
      emptyList()
    }.fetchQuestions()

    assertThat(actual.map { it.payload }).containsExactlyElementsIn(expected.map { it.payload })
    assertThat(networkUsed).isFalse()
  }

  @Test
  fun testFetchQuestions_pinnedCache_reusesOlderExactVersionWithoutNetwork() = runBlocking {
    val directory = temporaryFolder.newFolder()
    QuestionRepository(directory, false, moshi) {
      if (it == 0) listOf(createQuestion("first", 1)) else emptyList()
    }.fetchQuestions()
    QuestionRepository(directory, false, moshi) {
      if (it == 0) listOf(createQuestion("first", 2)) else emptyList()
    }.fetchQuestions()

    val actual = QuestionRepository(directory, false, moshi) {
      error("An exact cached question must not need the network.")
    }.fetchQuestions(mapOf("first" to 1))

    assertThat(actual.single().expectedVersion).isEqualTo(1)
  }

  @Test
  fun testFetchQuestions_pinnedVersionChanged_throwsRatherThanUsingLatest() = runBlocking {
    val repository = QuestionRepository(null, false, moshi) {
      if (it == 0) listOf(createQuestion("first", 2)) else emptyList()
    }

    val error = assertFailsWith<IllegalStateException> {
      repository.fetchQuestions(mapOf("first" to 1))
    }

    assertThat(error).hasMessageThat().contains("requires version 1")
  }

  @Test
  fun testFetchQuestions_missingPinnedQuestion_throws() = runBlocking {
    val repository = QuestionRepository(null, false, moshi) { emptyList() }

    val error = assertFailsWith<IllegalStateException> {
      repository.fetchQuestions(mapOf("missing" to 1))
    }

    assertThat(error).hasMessageThat().contains("was not downloaded")
  }

  @Test
  fun testFetchQuestions_emptyPins_returnsEmptyWithoutNetwork() = runBlocking {
    val questions = QuestionRepository(null, false, moshi) {
      error("Empty pins must not fetch an unpinned inventory.")
    }.fetchQuestions(emptyMap())

    assertThat(questions).isEmpty()
  }

  @Test
  fun testFetchQuestions_failedLaterPage_doesNotWritePartialSnapshot() = runBlocking {
    val directory = temporaryFolder.newFolder()
    val repository = QuestionRepository(directory, false, moshi) {
      if (it == 0) listOf(createQuestion("first", 1)) else error("Page failed")
    }

    assertFailsWith<IllegalStateException> { repository.fetchQuestions() }

    assertThat(File(directory, "questions_latest.json").exists()).isFalse()
  }

  @Test
  fun testFetchQuestions_forcedCacheMissing_throwsWithoutNetwork() = runBlocking {
    val repository = QuestionRepository(temporaryFolder.newFolder(), true, moshi) {
      error("Forced cache must not use the network.")
    }

    val error = assertFailsWith<IllegalStateException> { repository.fetchQuestions() }

    assertThat(error).hasMessageThat().contains("inventory is missing")
  }

  @Test
  fun testFetchQuestions_sameVersionChangedContent_throwsAndKeepsCache() = runBlocking {
    val directory = temporaryFolder.newFolder()
    val first = createQuestion("first", 1)
    QuestionRepository(directory, false, moshi) {
      if (it == 0) listOf(first) else emptyList()
    }.fetchQuestions()
    val snapshot = File(directory, "questions_latest.json").readText()
    val changed = first.copy(payload = first.payload.copy(linkedSkillIds = listOf("changed")))

    val error = assertFailsWith<IllegalStateException> {
      QuestionRepository(directory, false, moshi) {
        if (it == 0) listOf(changed) else emptyList()
      }.fetchQuestions()
    }

    assertThat(error).hasMessageThat().contains("changed without a content-version update")
    assertThat(File(directory, "questions_latest.json").readText()).isEqualTo(snapshot)
  }

  @Test
  fun testFetchQuestions_pinCacheContainsWrongVersion_throwsWithoutNetwork() = runBlocking {
    val directory = temporaryFolder.newFolder()
    QuestionRepository(directory, false, moshi) {
      if (it == 0) listOf(createQuestion("first", 2)) else emptyList()
    }.fetchQuestions()
    File(directory, "question_first_2.json").copyTo(File(directory, "question_first_1.json"))
    var networkUsed = false

    val error = assertFailsWith<IllegalStateException> {
      QuestionRepository(directory, false, moshi) {
        networkUsed = true
        emptyList()
      }.fetchQuestions(mapOf("first" to 1))
    }

    assertThat(error).hasMessageThat().contains("does not match pin")
    assertThat(networkUsed).isFalse()
  }

  private fun createQuestion(id: String, version: Int): VersionedStructure<GaeQuestion> =
    VersionedStructure(id, question.copy(id = id, version = version), null, null)
}
