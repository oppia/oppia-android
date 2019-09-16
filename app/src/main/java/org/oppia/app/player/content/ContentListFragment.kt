package org.oppia.app.player.content

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.data.backends.gae.NetworkModule
import org.oppia.data.backends.gae.model.GaeExplorationContainer
import org.oppia.data.backends.gae.model.GaeState
import org.oppia.data.backends.gae.model.GaeSubtitledHtml
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

import java.util.ArrayList

/**
 */
class ContentListFragment : Fragment() {

  private var gaeSubtitledHtml: GaeSubtitledHtml? = null

  var mLayoutManager: LinearLayoutManager? = null

  var rvContentCard: RecyclerView? = null

  var btnBack: Button? = null
  var btnContinue: Button? = null

  var adapter: ContentCardAdapter? = null

  var contentList: MutableList<GaeSubtitledHtml> = ArrayList()

  var btnCount = 0;

  companion object {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private val GAE_SUBTITLE_HTML_CONTENT = "param1"
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param gaeSubtitledHtml return gaeSubtitle contents that holds rich-text.
     * @return A new instance of fragment ContentListFragment.
     */
    fun newInstance(gaeSubtitledHtml: GaeSubtitledHtml?, interactionKey: String): ContentListFragment {
      val fragment = ContentListFragment()
      val args = Bundle()
      args.putSerializable(GAE_SUBTITLE_HTML_CONTENT, gaeSubtitledHtml)
      fragment.arguments = args
      return fragment
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (arguments != null) {
      gaeSubtitledHtml = arguments!!.getSerializable(GAE_SUBTITLE_HTML_CONTENT) as GaeSubtitledHtml?
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    // Inflate the layout for this fragment
    val view = inflater.inflate(R.layout.content_list_fragment, container, false)

    rvContentCard = view.findViewById(R.id.rvContentCard)
    btnContinue = view.findViewById(R.id.btnContinue)
    btnBack = view.findViewById(R.id.btnBack)


    initviews()

    return view
  }

  private fun initviews() {

    mLayoutManager = LinearLayoutManager(activity)
    rvContentCard!!.layoutManager = mLayoutManager

    adapter = ContentCardAdapter(requireContext(),contentList)
    rvContentCard!!.adapter=adapter

    // TODO(Veena): remove dummy exploration and fetch exploration form server;
    fetchDummyExplorations();
//    fetchExplorations();


    btnContinue!!.setOnClickListener {

      btnCount++;
      btnBack!!.visibility = View.VISIBLE;

      adapter!!.notifyDataSetChanged();

    }
    btnBack!!.setOnClickListener {

      btnCount--;
      if (btnCount == 0)
        btnBack!!.visibility = View.GONE;
      fetchDummyExplorations()
      adapter!!.notifyDataSetChanged();
    }
  }

  private fun fetchExplorations() {
    try {
      val gaeSubtitledHtml: GaeSubtitledHtml? = gaeSubtitledHtml;
      contentList.add(gaeSubtitledHtml!!)
      adapter!!.notifyDataSetChanged()
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
              adapter!!.notifyDataSetChanged()
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
