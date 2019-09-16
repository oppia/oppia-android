package org.oppia.app.player.content

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.app.application.ApplicationContext
import org.oppia.app.databinding.ContentListFragmentBinding
import org.oppia.app.databinding.ContentCardItemsBinding
import org.oppia.app.recyclerview.BindableAdapter
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

  var gaeSubtitledHtml: GaeSubtitledHtml? = null;

  init {
    this.gaeSubtitledHtml = gaeSubtitledHtml
  }

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = ContentListFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.recyclerView.apply {
      adapter = createRecyclerViewAdapter()
      // https://stackoverflow.com/a/50075019/3689782
      layoutManager = LinearLayoutManager(context)
    }
//    binding.let {
//      it.viewModel = getTopicListViewModel()
//      it.lifecycleOwner = fragment
//    }
    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<GaeSubtitledHtml> {
    return BindableAdapter.Builder
      .newBuilder<GaeSubtitledHtml>()
      .registerViewDataBinder(
        inflateDataBinding = ContentCardItemsBinding::inflate,
        setViewModel =ContentCardItemsBinding::setGaeSubtitleHtml
      )
      .build()
  }

//  var adapter: ContentCardAdapter? = null

  var contentList: MutableList<GaeSubtitledHtml> = ArrayList()

  private fun initviews() {

//    adapter = ContentCardAdapter(requireContext(),contentList)

    // TODO(Veena): remove dummy exploration and fetch exploration form server;
    fetchDummyExplorations();
//    fetchExplorations();

  }

  private fun fetchExplorations() {
    try {

      contentList.add(gaeSubtitledHtml!!)
//      adapter!!.notifyDataSetChanged()
    } catch (e: Exception) {
      Log.d("Tag", "Exception ****************" + e.printStackTrace())
    }
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
//              adapter!!.notifyDataSetChanged()
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
