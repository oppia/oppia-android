package org.oppia.app.player.content

import android.util.Log
import org.oppia.data.backends.gae.NetworkModule
import org.oppia.data.backends.gae.model.GaeExplorationContainer
import org.oppia.data.backends.gae.model.GaeState
import org.oppia.data.backends.gae.model.GaeSubtitledHtml
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GetContentCardIntractorImpl : MainContract.GetContentCardIntractor {
  override fun getContentCardArrayList(onFinishedListener: MainContract.GetContentCardIntractor.OnFinishedListener) {

    val retrofitInstance = NetworkModule().provideRetrofitInstance()
    val appStoreApiService = NetworkModule().provideExplorationService(retrofitInstance);
    val getStoresResponseCall = appStoreApiService.getExplorationById()

    getStoresResponseCall.enqueue(object : Callback<GaeExplorationContainer> {
      override fun onResponse(call: Call<GaeExplorationContainer>, response: Response<GaeExplorationContainer>) {

        try {

          val code = response.code()
          when (code) {
            200, 201 -> {
              val explorationContainer: GaeExplorationContainer? = response.body()
              var gaeStateMap: Map<String, GaeState>? = null
              gaeStateMap = explorationContainer!!.exploration!!.states
              val gaeStateData: GaeState? = gaeStateMap!!.get("Introduction")

              val gaeSubtitledHtml: GaeSubtitledHtml? = gaeStateData?.content

              var contentList: MutableList<GaeSubtitledHtml> = ArrayList()

              contentList.add(gaeSubtitledHtml!!)
//              adapter!!.notifyDataSetChanged()
              Log.d("Tag", "explorationContainer: ******************" + explorationContainer!!.exploration!!.states);

              onFinishedListener.onFinished(contentList);
            }

          }
        } catch (e: Exception) {
          Log.d("Tag", "Failure ****************" + e.printStackTrace())

        }

      }

      override fun onFailure(call: Call<GaeExplorationContainer>, t: Throwable) {
        Log.d("Tag", "Failure ****************" + t.message)
        onFinishedListener.onFailure(t);

      }
    })

  }

}
