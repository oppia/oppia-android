package org.oppia.android.data.backends.test

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.data.backends.api.MockConceptCardService
import org.oppia.android.data.backends.gae.api.ConceptCardService
import org.oppia.android.testing.network.MockRetrofitHelper
import org.robolectric.annotation.LooperMode
import retrofit2.mock.MockRetrofit

/**
 * Test for [ConceptCardService] retrofit instance using [MockConceptCardService]
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class MockConceptCardTest {
  private lateinit var mockRetrofit: MockRetrofit

  @Before
  fun setUp() {
    mockRetrofit = MockRetrofitHelper().createMockRetrofit()
  }

  @Test
  fun testConceptCardService_usingFakeJson_deserializationSuccessful() {
    val delegate = mockRetrofit.create(ConceptCardService::class.java)
    val mockConceptCardService = MockConceptCardService(delegate)

    val skillIdList = ArrayList<String>()
    skillIdList.add("1")
    skillIdList.add("2")
    skillIdList.add("3")

    val skillIds = skillIdList.joinToString(separator = ", ")
    val conceptCard = mockConceptCardService.getSkillContents(skillIds)
    val conceptCardResponse = conceptCard.execute()

    assertThat(conceptCardResponse.isSuccessful).isTrue()
    assertThat(conceptCardResponse.body()!!.conceptCardDicts!!.size).isEqualTo(1)
  }
}
