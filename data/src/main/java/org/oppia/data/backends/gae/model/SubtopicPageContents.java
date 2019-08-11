package org.oppia.data.backends.gae.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;
import java.util.List;


@AutoValue
public abstract class SubtopicPageContents {

    /*
    * Parent Hierarchy: Subtopic -> SubtopicPageContents
    */

    //TODO: AutoJSON null data error

    @SerializedName("subtitled_html") public abstract SubtitledHtml getContent();
    @SerializedName("recorded_voiceovers") public abstract RecordedVoiceovers getRecordedVoiceovers();
    @SerializedName("written_translations") public abstract WrittenTranslations getWrittenTranslations();
        
    public static TypeAdapter<SubtopicPageContents> createTypeAdapter(Gson gson) {
        return new AutoValue_SubtopicPageContents.GsonTypeAdapter(gson);
    }

}
