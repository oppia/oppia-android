package org.oppia.app.player.content

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.app.application.ApplicationContext
import org.oppia.app.databinding.ContentListFragmentBinding
import org.oppia.data.backends.gae.NetworkModule
import org.oppia.data.backends.gae.model.GaeExplorationContainer
import org.oppia.data.backends.gae.model.GaeState
import org.oppia.data.backends.gae.model.GaeSubtitledHtml
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

/** Presenter for [ContentListFragment]. */
class ContentListFragmentPresenter @Inject constructor(
  @ApplicationContext private val context: Context,
  private val fragment: Fragment
) {

  private lateinit var binding: ContentListFragmentBinding
  var contentCardAdapter: ContentCardAdapter? = null
  var contentList: MutableList<GaeSubtitledHtml> = ArrayList()

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {

    binding = ContentListFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)

    binding.recyclerView.apply {

      binding.recyclerView.layoutManager = LinearLayoutManager(context)
      contentCardAdapter = ContentCardAdapter(context, contentList);
      binding.contentCardAdapter = ContentCardAdapter(context, contentList);

    }
    fetchDummyExplorations()

    return binding.root
  }

  private fun fetchDummyExplorations() {

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
              contentList.add(gaeSubtitledHtml!!)
              binding!!.contentCardAdapter?.notifyDataSetChanged()

              Log.d("Tag", "explorationContainer: ******************" + explorationContainer!!.exploration!!.states);
            }
          }
        } catch (e: Exception) {
          Log.d("Tag", "Failure ****************" + e.printStackTrace())
        }
      }
      override fun onFailure(call: Call<GaeExplorationContainer>, t: Throwable) {
        Log.d("Tag", "Failure ****************" + t.message)

      }
    })
  }
}
