package org.oppia.android.testing.media

import android.content.Context
import android.media.MediaPlayer
import javax.inject.Inject

// TODO(#59): Replace the reflection code below with direct calls to Robolectric once this test can be made to run
//  only on Robolectric (or properly on Espresso without relying on Robolectric shadows, e.g. by using compile-time
//  replaceable fakes).

/** NOTE TO DEVELOPERS: DO NOT REPLICATE THE REFLECTION CODE BELOW ANYWHERE.
 * THIS IS A STOP-GAP MEASURE UNTIL WE CAN USE BAZEL TO PROPERLY BUILD THIS TEST SPECIFICALLY
 * FOR ROBOLECTRIC AND NOT FOR ESPRESSO.
 *
 *
 * Espresso-specific implementation of [TestMediaPlayer].
 *
 */
class TestMediaPlayerEspressoImpl @Inject constructor() : TestMediaPlayer {

  override fun addMediaInfo(
    context: Context,
    explorationId1: String,
    explorationId2: String,
    audioFileName1: String,
    audioFileName2: String,
    mediaPlayer: MediaPlayer
  ) {
    /* We can't use reflection like we are doing in [TestMediaPlayerRobolectricImpl],
     * need to find another way for espresso.
     */
  }

  override fun setupAudio(explorationId: String, audioFileName: String) {
    /* We can't use reflection like we are doing in [TestMediaPlayerRobolectricImpl],
     * need to find another way for espresso.
     */
  }

  override fun setupAudioDualMedia(
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
