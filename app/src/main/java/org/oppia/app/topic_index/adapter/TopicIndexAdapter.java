package org.oppia.app.topic_index.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.backend.model.TopicSummary;
import org.oppia.app.R;

import java.util.ArrayList;
import java.util.List;

/** TopicIndexAdapter binds the summary data to the list of Topics*/
public class TopicIndexAdapter extends RecyclerView.Adapter<TopicIndexAdapter.MyViewHolder> {
    
    private List<TopicSummary> topicSummaryList = new ArrayList<>();
    Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvCategory;
        TextView tvDescription;
        TextView tvNumOfLessons;
        LinearLayout llSubContainer, llContainer;
        CardView cardView;

        public MyViewHolder(View view) {
            super(view);
            
            tvTitle = view.findViewById(R.id.tvTitle);
            tvCategory = view.findViewById(R.id.tvCategory);
            tvDescription = view.findViewById(R.id.tvDescription);
            tvNumOfLessons = view.findViewById(R.id.tvNumOfLessons);

            cardView = view.findViewById(R.id.cardView);
            llContainer = view.findViewById(R.id.llContainer);
            llSubContainer = view.findViewById(R.id.llSubContainer);

        }
    }
    public TopicIndexAdapter(List<TopicSummary> topicSummaryList, Context mcontext) {
        this.topicSummaryList = topicSummaryList;
        this.context = mcontext;
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // System.out.println("hiiiiii"+shopList.get(0).getShop_name());
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.topic_index_items, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        TopicSummary topicSummary = topicSummaryList.get(position);

        holder.tvTitle.setText(topicSummary.getTitle());
        holder.tvDescription.setText(topicSummary.getObjective());
        holder.tvNumOfLessons.setText(topicSummary.getNum_of_lessons()+context.getString(R.string.lessons));
        holder.tvCategory.setText(topicSummary.getCategory());


    }

    @Override
    public int getItemCount() {
        return topicSummaryList.size();
    }


}
