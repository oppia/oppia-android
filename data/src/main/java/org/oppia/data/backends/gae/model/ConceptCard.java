package org.oppia.data.backends.gae.model;

import com.squareup.moshi.Json;

import java.util.List;

public class ConceptCard {

  /*
   * Parent Hierarchy: ConceptCard
   */

  //TODO: ImmutableList<> and List<> error
  //ImmutableList<> does not work but List<> works

  @Json(name = "concept_card_dicts")
  public List<SkillContents> conceptCardDicts;

}
