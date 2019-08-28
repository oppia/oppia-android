package org.oppia.app.topic_index;

import android.os.Bundle;
import android.util.Log;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.backend.model.TopicSummary;
import com.example.myapplication.backend.model.TopicSummaryHandler;
import org.oppia.app.R;
import org.oppia.app.backend.OppiaGaeClient;
import org.oppia.app.backend.api.TopicIndexHandlerService;
import org.oppia.app.topic_index.adapter.TopicIndexAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;
/** This Fragment contains a list of Topics summary */
public class TopicIndexFragment extends Fragment {

  RecyclerView rvTopicSummary;

  List<TopicSummary> topicSummaryList = new ArrayList<>();
  GridLayoutManager gridLayoutManager;

  TopicIndexAdapter topicIndexAdapter;
  private static final String TAG = TopicIndexFragment.class.getName();
  public TopicIndexFragment() {
    // Required empty public constructor
  }


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view =  inflater.inflate(R.layout.topic_index_fragment, container, false);
    rvTopicSummary = view.findViewById(R.id.rvTopicSummary);
    fetchTopicSummary();
    initViews();

    return view;

  }

  private void initViews() {
    gridLayoutManager = new GridLayoutManager(getActivity(), 2);
    rvTopicSummary.setLayoutManager(gridLayoutManager);
    topicIndexAdapter = new TopicIndexAdapter(topicSummaryList, getActivity());
    rvTopicSummary.setAdapter(topicIndexAdapter);
  }

  private void fetchTopicSummary() {

    TopicIndexHandlerService appStoreApiService = OppiaGaeClient.INSTANCE.getRetrofitInstance().create(TopicIndexHandlerService.class);

    Call<TopicSummaryHandler> getStoresResponseCall = appStoreApiService.getTopicIndex();

    getStoresResponseCall.enqueue(new Callback<TopicSummaryHandler>() {
      @Override
      public void onResponse(Call<TopicSummaryHandler> call, Response<TopicSummaryHandler> response) {


        Integer code = response.code();
        switch (code) {
          case 200:
          case 201:
            TopicSummaryHandler topicSummaryHandler =  response.body();

            topicSummaryList.addAll(topicSummaryHandler.getTopic_summary_dicts());
            Log.d(TAG, "Topic Name: ******************" +response.body());

            topicIndexAdapter.notifyDataSetChanged();
            break;
        }

      }

      @Override
      public void onFailure(Call<TopicSummaryHandler> call, Throwable t) {
        Log.d(TAG,"Failure ****************"+ t.getMessage());



      }
    });


  }

}
