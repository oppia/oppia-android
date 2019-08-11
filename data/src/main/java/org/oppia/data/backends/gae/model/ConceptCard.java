package org.oppia.data.backends.gae.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@AutoValue
public abstract class ConceptCard {

    /*
    * Parent Hierarchy: ConceptCard
    */

    //TODO: ImmutableList<> and List<> error
    //ImmutableList<> does not work but List<> works
    //TODO: AutoJSON null data error
    
    @SerializedName("concept_card_dicts") public abstract List<SkillContents> getConceptCardDicts();

    public static TypeAdapter<ConceptCard> createTypeAdapter(Gson gson) {
        return new AutoValue_ConceptCard.GsonTypeAdapter(gson);
    }

}
