package org.oppia.android.testing.network

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.oppia.android.data.gae.gae.NetworkInterceptor
import org.oppia.android.data.gae.gae.NetworkSettings
import org.oppia.android.data.gae.gae.api.ConceptCardService
import org.oppia.android.data.gae.gae.model.GaeConceptCard
import org.oppia.android.testing.ApiUtils
import retrofit2.Call
import retrofit2.mock.BehaviorDelegate

/**
 * Mock ConceptCardService with dummy data from [concept_card.json]
 */
class MockConceptCardService(private val delegate: BehaviorDelegate<ConceptCardService>) :
  ConceptCardService {
  override fun getSkillContents(skillIds: String): Call<GaeConceptCard> {
    val conceptCard = createMockGaeConceptCard()
    return delegate.returningResponse(conceptCard).getSkillContents(skillIds)
  }

  /**
   * This function creates a mock GaeConceptCard with data from dummy json.
   * @return GaeConceptCard: GaeConceptCard with mock data
   */
  private fun createMockGaeConceptCard(): GaeConceptCard {
    val networkInterceptor = NetworkInterceptor()
    var conceptCardResponseWithXssiPrefix =
      NetworkSettings.XSSI_PREFIX + ApiUtils.getFakeJson("concept_card.json")

    conceptCardResponseWithXssiPrefix =
      networkInterceptor.removeXSSIPrefix(conceptCardResponseWithXssiPrefix)

    val moshi = Moshi.Builder().build()
    val adapter: JsonAdapter<GaeConceptCard> = moshi.adapter(GaeConceptCard::class.java)
    val mockGaeConceptCard = adapter.fromJson(conceptCardResponseWithXssiPrefix)

    return mockGaeConceptCard!!
  }
}
