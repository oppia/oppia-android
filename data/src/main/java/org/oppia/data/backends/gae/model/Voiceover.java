package org.oppia.data.backends.gae.model;

import com.squareup.moshi.Json;

public class Voiceover {

  /*
   * Parent Hierarchy: ExplorationContainer -> Exploration -> State -> RecordedVoiceovers -> Voiceover
   */

  @Json(name = "file_size_bytes")
  public Long fileSize_Bytes;

  @Json(name = "needs_update")
  public boolean needsUpdate;

  @Json(name = "filename")
  public String filename;

}
