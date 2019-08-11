package org.oppia.data.backends.gae.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;
import java.util.List;

@AutoValue
public abstract class Subtopic {

    /*
    * Parent Hierarchy: Subtopic
    */

    //TODO: AutoJSON null data error

    @SerializedName("topic_id") public abstract String getTopicId();
    @SerializedName("subtopic_id") public abstract String getSubtopicId();
    @SerializedName("page_contents") public abstract SubtopicPageContents getPageContents();
    
    public static TypeAdapter<Subtopic> createTypeAdapter(Gson gson) {
        return new AutoValue_Subtopic.GsonTypeAdapter(gson);
    }

}
