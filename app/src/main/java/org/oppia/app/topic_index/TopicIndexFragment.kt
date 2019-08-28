package org.oppia.app.topic_index

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.backend.model.TopicIndexModel
import org.oppia.app.backend.model.TopicIndexHandler
import org.oppia.app.R
import org.oppia.app.topic_index.adapter.TopicIndexAdapter
import org.oppia.data.backends.gae.OppiaGaeClient
import org.oppia.data.backends.gae.api.TopicIndexHandlerService
import retrofit2.Call

import retrofit2.Callback
import retrofit2.Response

import java.util.ArrayList

/** This Fragment contains a list of Topics summary  */
class TopicIndexFragment : Fragment() {

   var rvTopicindex: RecyclerView? = null

  internal var topicSummaryList: MutableList<TopicIndexModel> = ArrayList()
   var gridLayoutManager: GridLayoutManager? = null

  var topicIndexAdapter: TopicIndexAdapter? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val view = inflater.inflate(R.layout.topic_index_fragment, container, false)

     rvTopicindex = view.findViewById(R.id.rvTopicSummary) as RecyclerView

    initViews()

    fetchTopicSummary()

    return view

  }

  private fun initViews() {
    gridLayoutManager = GridLayoutManager(activity, 2)
    rvTopicindex!!.layoutManager = gridLayoutManager

    topicIndexAdapter = TopicIndexAdapter(topicSummaryList, this)
    rvTopicindex!!.adapter = topicIndexAdapter
  }

  private fun fetchTopicSummary() {

    val topicIndexService: TopicIndexHandlerService =
      OppiaGaeClient.retrofitInstance!!.create(TopicIndexHandlerService::class.java)

    val call: Call<TopicIndexHandler> = topicIndexService.getTopicIndex()

    call.enqueue(object : Callback<TopicIndexHandler> {

      override fun onResponse(call: Call<TopicIndexHandler>?, response: Response<TopicIndexHandler>?) {

//        val code = response!!.code()
//        when (code) {
//          200, 201 -> {
            val topicSummaryHandler = response!!.body()

            topicSummaryList.addAll(topicSummaryHandler!!.topic_summary_dicts!!)
            Log.d(TAG, "Topic Name: ******************" + response.body())

            topicIndexAdapter!!.notifyDataSetChanged()
//          }
//        }

      }

      override fun onFailure(call: Call<TopicIndexHandler>?, t: Throwable?) {

        Log.e(TAG, t.toString())

      }

    })

//    val appStoreApiService = OppiaGaeClient.retrofitInstance!!.create(TopicIndexHandlerService::class.java)
//
//    val getStoresResponseCall = appStoreApiService.getTopicIndex()
//
//    getStoresResponseCall.enqueue(object : SortedList.Callback<TopicIndexHandler>() {
//      fun onResponse(call: Call<TopicIndexHandler>, response: Response<TopicIndexHandler>) {
//
//        val code = response.code()
//        when (code) {
//          200, 201 -> {
//            val topicSummaryHandler = response.body()
//
//            topicSummaryList.addAll(topicSummaryHandler.topic_summary_dicts!!)
//            Log.d(TAG, "Topic Name: ******************" + response.body())
//
//            topicIndexAdapter.notifyDataSetChanged()
//          }
//        }
//
//      }
//
//      fun onFailure(call: Call<TopicIndexHandler>, t: Throwable) {
//        Log.d(TAG, "Failure ****************" + t.message)
//
//      }
//    })

  }

  companion object {
    private val TAG = TopicIndexFragment::class.java.name
  }

}// Required empty public constructor
