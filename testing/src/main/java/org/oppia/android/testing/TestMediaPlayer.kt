package org.oppia.android.testing

import android.content.Context
import android.media.MediaPlayer

interface TestMediaPlayer {

  fun addMediaInfo(context: Context, testUrl: String, testUrl2: String, mediaPlayer: MediaPlayer)
}
