package org.oppia.android.domain.question

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.protobuf.InvalidProtocolBufferException
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.oppia.android.app.model.Question
import org.oppia.android.app.model.QuestionList
import org.oppia.android.app.model.SyncStatus
import org.oppia.android.domain.util.JsonAssetRetriever
import org.oppia.android.domain.util.StateRetriever
import org.oppia.android.testing.assertThrows
import org.oppia.android.util.caching.AssetRepository
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.robolectric.annotation.Config

/** Tests for [QuestionRetriever]. */
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class QuestionRetrieverTest {
  private val assetRepository = mock(AssetRepository::class.java)
  private lateinit var questionRetriever: QuestionRetriever

  @Before
  fun setUp() {
    questionRetriever = createRetriever(loadProtos = true)
  }

  private fun createRetriever(loadProtos: Boolean): QuestionRetriever {
    return QuestionRetriever(
      JsonAssetRetriever(assetRepository), StateRetriever(), assetRepository,
      object : PlatformParameterValue<Boolean> {
        override val value = loadProtos
        override val syncStatus = SyncStatus.NOT_SYNCED_FROM_SERVER
      }
    )
  }

  @Test
  fun testLoadQuestions_developerAssets_protoAndJsonHaveSamePlayableStates() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    `when`(assetRepository.loadTextFileFromLocalAssets("questions.json")).thenReturn(
      context.assets.open("questions.json").bufferedReader().use { it.readText() }
    )
    val questionList = context.assets.open("questions.pb").use { QuestionList.parseFrom(it) }
    `when`(
      assetRepository.loadProtoFromLocalAssets("questions", QuestionList.getDefaultInstance())
    ).thenReturn(questionList)
    val skills = questionList.questionsList.flatMap { it.linkedSkillIdsList }.distinct()

    val jsonQuestions = createRetriever(loadProtos = false).loadQuestions(skills)
    val protoQuestions = questionRetriever.loadQuestions(skills)

    assertThat(protoQuestions.map { it.questionId })
      .containsExactlyElementsIn(jsonQuestions.map { it.questionId }).inOrder()
    assertThat(protoQuestions.map { it.linkedSkillIdsList })
      .containsExactlyElementsIn(jsonQuestions.map { it.linkedSkillIdsList }).inOrder()
    assertThat(protoQuestions.map { it.questionState })
      .containsExactlyElementsIn(jsonQuestions.map { it.questionState }).inOrder()
  }

  @Test
  fun testLoadQuestions_protoAssets_filtersByLinkedSkills() {
    val firstQuestion = createQuestion("question_0", listOf("skill_0", "skill_1"))
    val secondQuestion = createQuestion("question_1", listOf("skill_1"))
    setQuestions(listOf(firstQuestion, secondQuestion))

    val questions = questionRetriever.loadQuestions(listOf("skill_0"))

    assertThat(questions).containsExactly(firstQuestion)
  }

  @Test
  fun testLoadQuestions_protoAssets_multipleSkills_preservesPerSkillOrder() {
    val firstQuestion = createQuestion("question_0", listOf("skill_0", "skill_1"))
    val secondQuestion = createQuestion("question_1", listOf("skill_1"))
    setQuestions(listOf(firstQuestion, secondQuestion))

    val questions = questionRetriever.loadQuestions(listOf("skill_0", "skill_1"))

    assertThat(questions).containsExactly(firstQuestion, firstQuestion, secondQuestion).inOrder()
  }

  @Test
  fun testLoadQuestions_protoAssets_unknownSkills_returnsEmptyList() {
    setQuestions(listOf(createQuestion("question_0", listOf("skill_0"))))

    assertThat(questionRetriever.loadQuestions(listOf("missing_skill"))).isEmpty()
  }

  @Test
  fun testLoadQuestions_protoAssets_noSkills_returnsEmptyList() {
    setQuestions(listOf(createQuestion("question_0", listOf("skill_0"))))

    assertThat(questionRetriever.loadQuestions(emptyList())).isEmpty()
  }

  @Test
  fun testLoadQuestions_protoAssets_emptyQuestionList_returnsEmptyList() {
    setQuestions(emptyList())

    assertThat(questionRetriever.loadQuestions(listOf("skill_0"))).isEmpty()
  }

  @Test
  fun testLoadQuestions_protoAssets_missingAsset_propagatesFailure() {
    `when`(
      assetRepository.loadProtoFromLocalAssets("questions", QuestionList.getDefaultInstance())
    ).thenThrow(IllegalStateException("Asset doesn't exist: questions"))

    val failure = assertThrows<IllegalStateException> {
      questionRetriever.loadQuestions(listOf("skill_0"))
    }

    assertThat(failure).hasMessageThat().contains("Asset doesn't exist: questions")
  }

  @Test
  fun testLoadQuestions_protoAssets_invalidAsset_propagatesParsingFailure() {
    `when`(
      assetRepository.loadProtoFromLocalAssets("questions", QuestionList.getDefaultInstance())
    ).thenAnswer { QuestionList.parseFrom(byteArrayOf(10, -1)) }

    assertThrows<InvalidProtocolBufferException> {
      questionRetriever.loadQuestions(listOf("skill_0"))
    }
  }

  private fun setQuestions(questions: List<Question>) {
    `when`(
      assetRepository.loadProtoFromLocalAssets("questions", QuestionList.getDefaultInstance())
    ).thenReturn(QuestionList.newBuilder().addAllQuestions(questions).build())
  }

  private fun createQuestion(id: String, skills: List<String>): Question {
    return Question.newBuilder().setQuestionId(id).addAllLinkedSkillIds(skills).build()
  }
}
