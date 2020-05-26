package org.oppia.domain.repository

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.app.model.ConceptCardView
import org.oppia.app.model.LessonThumbnail
import org.oppia.app.model.LessonThumbnailGraphic
import org.oppia.app.model.SkillSummaryDomain
import org.oppia.domain.topic.FRACTIONS_SKILL_ID_0
import org.oppia.domain.topic.FRACTIONS_SKILL_ID_1
import org.oppia.domain.topic.FRACTIONS_SKILL_ID_2
import org.oppia.domain.topic.RATIOS_SKILL_ID_0
import org.oppia.domain.topic.TEST_SKILL_ID_0
import org.oppia.domain.topic.TEST_SKILL_ID_1
import org.oppia.util.caching.CacheAssetsLocally
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProviders
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import org.robolectric.annotation.Config

private const val DUMMY_DESCRIPTION_TEXT = "dummy_description_text"
private const val DUMMY_THUMBNAIL_URL = "dummy_thumbnail_url"
private const val INVALID_SKILL_ID = "invalid_skill_id"

/** Tests for [SkillRepository]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class SkillRepositoryTest {

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Rule
  @JvmField
  val executorRule = InstantTaskExecutorRule()

  @Inject lateinit var skillRepository: SkillRepository

  @Mock lateinit var mockConceptCardViewObserver: Observer<AsyncResult<ConceptCardView>>
  @Captor lateinit var conceptCardViewResultCaptor: ArgumentCaptor<AsyncResult<ConceptCardView>>

  @Inject lateinit var dataProviders: DataProviders

  @Inject
  @field:TestDispatcher
  lateinit var testDispatcher: CoroutineDispatcher

  private val coroutineContext by lazy {
    EmptyCoroutineContext + testDispatcher
  }

  // https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/
  @ObsoleteCoroutinesApi
  private val testThread = newSingleThreadContext("TestMain")

  @Before
  @ExperimentalCoroutinesApi
  @ObsoleteCoroutinesApi
  fun setUp() {
    Dispatchers.setMain(testThread)
    setUpTestApplicationComponent()
  }

  @After
  @ExperimentalCoroutinesApi
  @ObsoleteCoroutinesApi
  fun tearDown() {
    Dispatchers.resetMain()
    testThread.close()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun tesSkillSummary_storeAllSkillSummariesToProtoDatabase_getFractionSkillID0_correctSkillSummaryData() =
    runBlockingTest(coroutineContext) {
      skillRepository.storeAllSkillSummariesToProtoDatabase()
      advanceUntilIdle()

      val skillSummaryDataProvider =
        skillRepository.getSkillSummaryDataProvider(FRACTIONS_SKILL_ID_0)
      dataProviders.convertToLiveData(skillSummaryDataProvider)
        .observeForever(mockConceptCardViewObserver)
      advanceUntilIdle()

      verifyGetConceptCardViewSucceeded()
      val conceptCardView = conceptCardViewResultCaptor.value.getOrThrow()
      assertThat(conceptCardView.skillId).isEqualTo(FRACTIONS_SKILL_ID_0)
      assertThat(conceptCardView.skillThumbnail.thumbnailGraphic).isEqualTo(LessonThumbnailGraphic.IDENTIFYING_THE_PARTS_OF_A_FRACTION)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun tesSkillSummary_storeAllSkillSummariesToProtoDatabase_getFractionSkillID1_correctSkillSummaryData() =
    runBlockingTest(coroutineContext) {
      skillRepository.storeAllSkillSummariesToProtoDatabase()
      advanceUntilIdle()

      val skillSummaryDataProvider =
        skillRepository.getSkillSummaryDataProvider(FRACTIONS_SKILL_ID_1)
      dataProviders.convertToLiveData(skillSummaryDataProvider)
        .observeForever(mockConceptCardViewObserver)
      advanceUntilIdle()

      verifyGetConceptCardViewSucceeded()
      val conceptCardView = conceptCardViewResultCaptor.value.getOrThrow()
      assertThat(conceptCardView.skillId).isEqualTo(FRACTIONS_SKILL_ID_1)
      assertThat(conceptCardView.skillThumbnail.thumbnailGraphic).isEqualTo(LessonThumbnailGraphic.WRITING_FRACTIONS)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun tesSkillSummary_storeAllSkillSummariesToProtoDatabase_getFractionSkillID2_correctSkillSummaryData() =
    runBlockingTest(coroutineContext) {
      skillRepository.storeAllSkillSummariesToProtoDatabase()
      advanceUntilIdle()

      val skillSummaryDataProvider =
        skillRepository.getSkillSummaryDataProvider(FRACTIONS_SKILL_ID_2)
      dataProviders.convertToLiveData(skillSummaryDataProvider)
        .observeForever(mockConceptCardViewObserver)
      advanceUntilIdle()

      verifyGetConceptCardViewSucceeded()
      val conceptCardView = conceptCardViewResultCaptor.value.getOrThrow()
      assertThat(conceptCardView.skillId).isEqualTo(FRACTIONS_SKILL_ID_2)
      assertThat(conceptCardView.skillThumbnail.thumbnailGraphic).isEqualTo(LessonThumbnailGraphic.MIXED_NUMBERS_AND_IMPROPER_FRACTIONS)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun tesSkillSummary_storeAllSkillSummariesToProtoDatabase_getRatiosSkillID0_correctSkillSummaryData() =
    runBlockingTest(coroutineContext) {
      skillRepository.storeAllSkillSummariesToProtoDatabase()
      advanceUntilIdle()

      val skillSummaryDataProvider = skillRepository.getSkillSummaryDataProvider(RATIOS_SKILL_ID_0)
      dataProviders.convertToLiveData(skillSummaryDataProvider)
        .observeForever(mockConceptCardViewObserver)
      advanceUntilIdle()

      verifyGetConceptCardViewSucceeded()
      val conceptCardView = conceptCardViewResultCaptor.value.getOrThrow()
      assertThat(conceptCardView.skillId).isEqualTo(RATIOS_SKILL_ID_0)
      assertThat(conceptCardView.skillThumbnail.thumbnailGraphic).isEqualTo(LessonThumbnailGraphic.DERIVE_A_RATIO)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun tesSkillSummary_addSkillSummaryList_getSkillSummary0_correctSkillSummaryData() =
    runBlockingTest(coroutineContext) {
      skillRepository.addSkillSummaryList(createSkillSummaryList())
      advanceUntilIdle()

      val skillSummaryDataProvider = skillRepository.getSkillSummaryDataProvider(TEST_SKILL_ID_0)
      dataProviders.convertToLiveData(skillSummaryDataProvider)
        .observeForever(mockConceptCardViewObserver)
      advanceUntilIdle()

      verifyGetConceptCardViewSucceeded()
      val conceptCardView = conceptCardViewResultCaptor.value.getOrThrow()
      assertThat(conceptCardView.skillId).isEqualTo(TEST_SKILL_ID_0)
      assertThat(conceptCardView.description).isEqualTo(DUMMY_DESCRIPTION_TEXT)
      assertThat(conceptCardView.thumbnailUrl).isEqualTo(DUMMY_THUMBNAIL_URL)
      assertThat(conceptCardView.skillThumbnail.thumbnailGraphic).isEqualTo(LessonThumbnailGraphic.ADDING_AND_SUBTRACTING_FRACTIONS)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun tesSkillSummary_storeAllSkillSummariesToProtoDatabase_getInvalidSkillID_noSkillSummaryDataFound() =
    runBlockingTest(coroutineContext) {
      skillRepository.addSkillSummaryList(createSkillSummaryList())
      advanceUntilIdle()

      val skillSummaryDataProvider = skillRepository.getSkillSummaryDataProvider(INVALID_SKILL_ID)
      dataProviders.convertToLiveData(skillSummaryDataProvider)
        .observeForever(mockConceptCardViewObserver)
      advanceUntilIdle()

      verifyGetConceptCardViewSucceeded()
      val conceptCardView = conceptCardViewResultCaptor.value.getOrThrow()
      assertThat(conceptCardView).isEqualTo(ConceptCardView.getDefaultInstance())
    }

  @Test
  @ExperimentalCoroutinesApi
  fun tesSkillSummary_addSkillSummaryList_getSkillSummary1_correctSkillSummaryData() =
    runBlockingTest(coroutineContext) {
      skillRepository.addSkillSummaryList(createSkillSummaryList())
      advanceUntilIdle()

      val skillSummaryDataProvider = skillRepository.getSkillSummaryDataProvider(TEST_SKILL_ID_1)
      dataProviders.convertToLiveData(skillSummaryDataProvider)
        .observeForever(mockConceptCardViewObserver)
      advanceUntilIdle()

      verifyGetConceptCardViewSucceeded()
      val conceptCardView = conceptCardViewResultCaptor.value.getOrThrow()
      assertThat(conceptCardView.skillId).isEqualTo(TEST_SKILL_ID_1)
      assertThat(conceptCardView.description).isEqualTo(DUMMY_DESCRIPTION_TEXT)
      assertThat(conceptCardView.thumbnailUrl).isEqualTo(DUMMY_THUMBNAIL_URL)
      assertThat(conceptCardView.skillThumbnail.thumbnailGraphic).isEqualTo(LessonThumbnailGraphic.IDENTIFYING_THE_PARTS_OF_A_FRACTION)
    }

  private fun setUpTestApplicationComponent() {
    DaggerSkillRepositoryTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  private fun verifyGetConceptCardViewSucceeded() {
    verify(
      mockConceptCardViewObserver,
      atLeastOnce()
    ).onChanged(conceptCardViewResultCaptor.capture())
    assertThat(conceptCardViewResultCaptor.value.isSuccess()).isTrue()
  }

  private fun createSkillSummaryList(): List<SkillSummaryDomain> {
    val skillSummaryDomainList = mutableListOf<SkillSummaryDomain>()
    skillSummaryDomainList.add(createSkillSummary0())
    skillSummaryDomainList.add(createSkillSummary1())
    return skillSummaryDomainList
  }

  private fun createSkillSummary0(): SkillSummaryDomain {
    return SkillSummaryDomain.newBuilder()
      .setSkillId(TEST_SKILL_ID_0)
      .setDescription(DUMMY_DESCRIPTION_TEXT)
      .setSkillThumbnail(createThumbnail0())
      .setThumbnailUrl(DUMMY_THUMBNAIL_URL)
      .build()
  }

  private fun createSkillSummary1(): SkillSummaryDomain {
    return SkillSummaryDomain.newBuilder()
      .setSkillId(TEST_SKILL_ID_1)
      .setDescription(DUMMY_DESCRIPTION_TEXT)
      .setSkillThumbnail(createThumbnail1())
      .setThumbnailUrl(DUMMY_THUMBNAIL_URL)
      .build()
  }

  private fun createThumbnail0(): LessonThumbnail {
    return LessonThumbnail.newBuilder()
      .setThumbnailGraphic(LessonThumbnailGraphic.ADDING_AND_SUBTRACTING_FRACTIONS)
      .build()
  }

  private fun createThumbnail1(): LessonThumbnail {
    return LessonThumbnail.newBuilder()
      .setThumbnailGraphic(LessonThumbnailGraphic.IDENTIFYING_THE_PARTS_OF_A_FRACTION)
      .build()
  }

  @Qualifier annotation class TestDispatcher

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    @ExperimentalCoroutinesApi
    @Singleton
    @Provides
    @TestDispatcher
    fun provideTestDispatcher(): CoroutineDispatcher {
      return TestCoroutineDispatcher()
    }

    @Singleton
    @Provides
    @BackgroundDispatcher
    fun provideBackgroundDispatcher(@TestDispatcher testDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
    }

    @Singleton
    @Provides
    @BlockingDispatcher
    fun provideBlockingDispatcher(@TestDispatcher testDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
    }

    // TODO(#59): Either isolate these to their own shared test module, or use the real logging
    // module in tests to avoid needing to specify these settings for tests.
    @EnableConsoleLog
    @Provides
    fun provideEnableConsoleLog(): Boolean = true

    @EnableFileLog
    @Provides
    fun provideEnableFileLog(): Boolean = false

    @GlobalLogLevel
    @Provides
    fun provideGlobalLogLevel(): LogLevel = LogLevel.VERBOSE

    @CacheAssetsLocally
    @Provides
    fun provideCacheAssetsLocally(): Boolean = false
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [TestModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(skillRepositoryTest: SkillRepositoryTest)
  }
}