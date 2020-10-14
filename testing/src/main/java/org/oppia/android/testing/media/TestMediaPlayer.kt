package org.oppia.android.testing.media

import android.content.Context
import android.media.MediaPlayer

/**
 * Helper class to test Media Player related test cases in both Robolectric & Espresso.
 */
interface TestMediaPlayer {

  /**
   * Add Media details and Audio Data Source
   *
   * Adding the required details of media which we want our test cases to run on.
   * [context] the test environment context
   * [explorationId1], [explorationId2] Id of the exploration
   * [audioFileName1], [audioFileName2] name of the  audio file
   * [mediaPlayer] Media Player object we use in our test case
   */
  fun addMediaInfo(
    context: Context,
    explorationId1: String,
    explorationId2: String,
    audioFileName1: String,
    audioFileName2: String,
    mediaPlayer: MediaPlayer
  )

  /**
   * Add Audio Data Source
   *
   * Adding the required details of media which we want our test cases to run on.
   * [explorationId1] Id of the exploration
   * [audioFileName1] name of the  audio file
   */
  fun setupAudio(explorationId: String, audioFileName: String)

  /**
   * Add Audio Data Source
   *
   * Adding the required details of media which we want our test cases to run on.
   * [explorationId1], [explorationId2] Id of the exploration
   * [audioFileName1], [audioFileName2] name of the  audio file
   */
  fun setupAudioDualMedia(
    explorationId1: String,
    audioFileName1: String,
    explorationId2: String,
    audioFileName2: String
  )
}
