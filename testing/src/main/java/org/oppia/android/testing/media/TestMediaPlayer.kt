package org.oppia.android.testing.media

import android.media.MediaPlayer

/**
 * Helper class to test Media Player related test cases in both Robolectric & Espresso.
 */
interface TestMediaPlayer {

  /**
   * Add Media details and Audio Data Source
   *
   * Adding the required details of media which we want our test cases to run on.
   * @param context the test environment context
   * @param explorationId1, explorationId2 Id of the exploration
   * @param audioFileName1 name of the  audio file
   * @param audioFileName2 name of the  audio file
   * @param mediaPlayer Media Player object we use in our test case
   */
  fun addMediaInfo(
    explorationId1: String,
    explorationId2: String,
    audioFileName1: String,
    audioFileName2: String,
    mediaPlayer: MediaPlayer,
    duration: Int = 1000,
    preparationDelay: Int = 0
  )

  /**
   * Add Audio Data Source
   *
   * Adding the required details of media which we want our test cases to run on.
   * @param explorationId1 Id of the exploration
   * @param audioFileName1 name of the  audio file
   */
  fun setUpAudio(explorationId: String, audioFileName: String)

  /**
   * Add Audio Data Source
   *
   * Adding the required details of media which we want our test cases to run on.
   * @param explorationId1 Id of the exploration
   * @param explorationId2 Id of the exploration
   * @param audioFileName1 name of the  audio file
   * @param audioFileName2 name of the  audio file
   */
  fun setUpAudioDualMedia(
    explorationId1: String,
    audioFileName1: String,
    explorationId2: String,
    audioFileName2: String
  )
}
