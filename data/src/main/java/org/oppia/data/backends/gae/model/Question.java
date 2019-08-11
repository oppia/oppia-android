package org.oppia.data.backends.gae.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;
import java.util.List;

@AutoValue
public abstract class Question {

    /*
    * Parent Hierarchy: QuestionPlayer -> Question
    */


    //TODO: AutoJSON null data error

    @SerializedName("id") public abstract String getId();
    @SerializedName("question_state_data") public abstract State getState();
    @SerializedName("version") public abstract int getVersion();

    public static TypeAdapter<Question> createTypeAdapter(Gson gson) {
        return new AutoValue_Question.GsonTypeAdapter(gson);
    }

}