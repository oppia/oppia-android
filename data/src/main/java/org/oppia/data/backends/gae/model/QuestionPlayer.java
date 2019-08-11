package org.oppia.data.backends.gae.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;
import java.util.List;

@AutoValue
public abstract class QuestionPlayer {

    /*
    * Parent Hierarchy: QuestionPlayer
    */

    //TODO: ImmutableList<> and List<> error
    //ImmutableList<> does not work but List<> works
    //TODO: AutoJSON null data error
    
    @SerializedName("question_dicts") public abstract List<Question> getQuestions();

    public static TypeAdapter<QuestionPlayer> createTypeAdapter(Gson gson) {
        return new AutoValue_QuestionPlayer.GsonTypeAdapter(gson);
    }

}
