package org.oppia.android.testing

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import javax.inject.Inject

// TODO(#59): Replace the reflection code below with direct calls to Robolectric once this test can be made to run
//  only on Robolectric (or properly on Espresso without relying on Robolectric shadows, e.g. by using compile-time
//  replaceable fakes).

/** NOTE TO DEVELOPERS: DO NOT REPLICATE THE REFLECTION CODE BELOW ANYWHERE.
 * THIS IS A STOP-GAP MEASURE UNTIL WE CAN USE BAZEL TO PROPERLY BUILD THIS TEST SPECIFICALLY
 * FOR ROBOLECTRIC AND NOT FOR ESPRESSO.
 */

class TestMediaPlayerRobolectricImpl @Inject constructor() : TestMediaPlayer {

  override fun addMediaInfo(
    context: Context,
    testUrl: String,
    testUrl2: String,
    mediaPlayer: MediaPlayer
  ) {
    val dataSource = toDataSource(context, Uri.parse(testUrl))
    val dataSource2 = toDataSource(context, Uri.parse(testUrl2))
    val mediaInfo = createMediaInfo(
      /* duration= */ 1000,
      /* preparationDelay= */ 0
    )
    addMediaInfo(dataSource, mediaInfo)
    addMediaInfo(dataSource2, mediaInfo)

    val shadowMediaPlayer = shadowOf(mediaPlayer)
    setDataSource(shadowMediaPlayer, toDataSource(context, Uri.parse(testUrl)))

    invokePreparedListener(shadowMediaPlayer)
  }

  /** Calls DataSource.toDataSource() using reflection. */
  private fun toDataSource(context: Context, uri: Uri): Any {
    val dataSourceClass = Class.forName("org.robolectric.shadows.util.DataSource")
    val toDataSourceMethod =
      dataSourceClass.getMethod("toDataSource", Context::class.java, Uri::class.java)
    return toDataSourceMethod.invoke(/* obj= */ null, context, uri)
  }

  /** Returns a new ShadowMediaPlayer.MediaInfo using reflection. */
  private fun createMediaInfo(duration: Int, preparationDelay: Int): Any {
    val mediaInfoClass = Class.forName(
      "org.robolectric.shadows.ShadowMediaPlayer\$MediaInfo"
    )
    return mediaInfoClass.getConstructor(Int::class.java, Int::class.java)
      .newInstance(duration, preparationDelay)
  }

  /** Calls ShadowMediaPlayer.addMediaInfo() using reflection. */
  private fun addMediaInfo(dataSource: Any, mediaInfo: Any) {
    val shadowMediaPlayerClass = Class.forName(
      "org.robolectric.shadows.ShadowMediaPlayer"
    )
    val dataSourceClass = Class.forName(
      "org.robolectric.shadows.util.DataSource"
    )
    val mediaInfoClass = Class.forName(
      "org.robolectric.shadows.ShadowMediaPlayer\$MediaInfo"
    )
    val addMediaInfoMethod =
      shadowMediaPlayerClass.getMethod("addMediaInfo", dataSourceClass, mediaInfoClass)
    addMediaInfoMethod.invoke(/* obj= */ null, dataSource, mediaInfo)
  }

  /** Calls Robolectric's Shadows.shadowOf() using reflection. */
  private fun shadowOf(mediaPlayer: MediaPlayer): Any {
    val shadowsClass = Class.forName("org.robolectric.Shadows")
    return shadowsClass.getMethod("shadowOf", MediaPlayer::class.java)
      .invoke(/* obj= */ null, mediaPlayer)
  }

  /** Calls ShadowMediaPlayer.setDataSource() using reflection. */
  private fun setDataSource(shadowMediaPlayer: Any, dataSource: Any) {
    val dataSourceClass = Class.forName("org.robolectric.shadows.util.DataSource")
    shadowMediaPlayer.javaClass.getMethod("setDataSource", dataSourceClass)
      .invoke(shadowMediaPlayer, dataSource)
  }

  /** Calls ShadowMediaPlayer.invokePreparedListener() using reflection. */
  private fun invokePreparedListener(shadowMediaPlayer: Any) {
    shadowMediaPlayer.javaClass.getMethod("invokePreparedListener").invoke(shadowMediaPlayer)
  }
}
