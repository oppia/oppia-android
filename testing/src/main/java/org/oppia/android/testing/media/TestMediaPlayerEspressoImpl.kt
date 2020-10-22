package org.oppia.android.testing.media

import android.media.MediaPlayer
import javax.inject.Inject

// TODO(#59): Replace the reflection code below with direct calls to Robolectric once this test can be made to run
//  only on Robolectric (or properly on Espresso without relying on Robolectric shadows, e.g. by using compile-time
//  replaceable fakes).

/** Espresso-specific implementation of [TestMediaPlayer]. */
class TestMediaPlayerEspressoImpl @Inject constructor() : TestMediaPlayer {

  override fun addMediaInfo(
    explorationId1: String,
    explorationId2: String,
    audioFileName1: String,
    audioFileName2: String,
    mediaPlayer: MediaPlayer,
    duration: Int,
    preparationDelay: Int
  ) {
    /* We can't use reflection like we are doing in [TestMediaPlayerRobolectricImpl],
     * need to find another way for espresso.
     */
  }

  override fun setUpAudio(explorationId: String, audioFileName: String) {
    /* We can't use reflection like we are doing in [TestMediaPlayerRobolectricImpl],
     * need to find another way for espresso.
     */
  }

  override fun setUpAudioDualMedia(
    explorationId1: String,
    audioFileName1: String,
    explorationId2: String,
    audioFileName2: String
  ) {
    /* We can't use reflection like we are doing in [TestMediaPlayerRobolectricImpl],
     * need to find another way for espresso.
     */
  }
}
