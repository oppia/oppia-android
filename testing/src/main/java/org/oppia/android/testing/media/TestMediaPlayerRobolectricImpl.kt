package org.oppia.android.testing.media

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import java.io.IOException
import javax.inject.Inject

// TODO(#59): Replace the reflection code below with direct calls to Robolectric once this test can be made to run
//  only on Robolectric (or properly on Espresso without relying on Robolectric shadows, e.g. by using compile-time
//  replaceable fakes).
/**
 * NOTE TO DEVELOPERS: DO NOT REPLICATE THE REFLECTION CODE BELOW ANYWHERE.
 * THIS IS A STOP-GAP MEASURE UNTIL WE CAN USE BAZEL TO PROPERLY BUILD THIS TEST SPECIFICALLY
 * FOR ROBOLECTRIC AND NOT FOR ESPRESSO.
 */

/**
 * Robolectric-specific implementation of [TestMediaPlayer].
 *
 * This implementation uses Reflection.
 */
class TestMediaPlayerRobolectricImpl @Inject constructor(
  val context: Context
) : TestMediaPlayer {

  override fun addMediaInfo(
    explorationId1: String,
    explorationId2: String,
    audioFileName1: String,
    audioFileName2: String,
    mediaPlayer: MediaPlayer,
    duration: Int,
    preparationDelay: Int
  ) {
    val mediaTestUrl1 = createAudioUrl(explorationId1, audioFileName1)
    val mediaTestUrl2 = createAudioUrl(explorationId2, audioFileName2)

    val dataSource = toDataSource(context, Uri.parse(mediaTestUrl1))!!
    val dataSource2 = toDataSource(context, Uri.parse(mediaTestUrl2))!!
    val mediaInfo = createMediaInfo(duration, preparationDelay)
    addMediaInfo(dataSource, mediaInfo)
    addMediaInfo(dataSource2, mediaInfo)

    val shadowMediaPlayer = shadowOf(mediaPlayer)!!
    setDataSource(shadowMediaPlayer, toDataSource(context, Uri.parse(mediaTestUrl1))!!)

    invokePreparedListener(shadowMediaPlayer)
  }

  /** Calls DataSource.toDataSource() using reflection. */
  private fun toDataSource(context: Context, uri: Uri): Any? {
    val dataSourceClass = getDataSourceClass()

    val toDataSourceMethod =
      dataSourceClass.getMethod("toDataSource", Context::class.java, Uri::class.java)
    return toDataSourceMethod.invoke(/* obj= */ null, context, uri)
  }

  /** Returns a new ShadowMediaPlayer.MediaInfo using reflection. */
  private fun createMediaInfo(duration: Int, preparationDelay: Int): Any {
    val mediaInfoClass = getMediaInfoClass()

    return mediaInfoClass
      .getConstructor(Int::class.java, Int::class.java)
      .newInstance(duration, preparationDelay)
  }

  /** Calls ShadowMediaPlayer.addMediaInfo() using reflection. */
  private fun addMediaInfo(dataSource: Any, mediaInfo: Any) {
    val shadowMediaPlayerClass = getShadowMediaPlayerClass()

    val dataSourceClass = getDataSourceClass()

    val mediaInfoClass = getMediaInfoClass()

    val addMediaInfoMethod =
      shadowMediaPlayerClass.getMethod("addMediaInfo", dataSourceClass, mediaInfoClass)

    addMediaInfoMethod.invoke(/* obj= */ null, dataSource, mediaInfo)
  }

  /** Calls Robolectric's Shadows.shadowOf() using reflection. */
  private fun shadowOf(mediaPlayer: MediaPlayer): Any? {
    val shadowsClass = getShadowsClass()

    return shadowsClass.getMethod("shadowOf", MediaPlayer::class.java)
      .invoke(/* obj= */ null, mediaPlayer)
  }

  /** Calls ShadowMediaPlayer.setDataSource() using reflection. */
  private fun setDataSource(shadowMediaPlayer: Any, dataSource: Any) {
    val dataSourceClass = getDataSourceClass()

    shadowMediaPlayer.javaClass.getMethod("setDataSource", dataSourceClass)
      .invoke(shadowMediaPlayer, dataSource)
  }

  /** Calls ShadowMediaPlayer.invokePreparedListener() using reflection. */
  private fun invokePreparedListener(shadowMediaPlayer: Any) {
    shadowMediaPlayer.javaClass.getMethod("invokePreparedListener").invoke(shadowMediaPlayer)
  }

  override fun setUpAudio(explorationId: String, audioFileName: String) {
    val dataSource = createAudioDataSource(
      explorationId = explorationId, audioFileName = audioFileName
    )
    addShadowMediaPlayerException(dataSource!!, IOException("Test does not have networking"))
  }

  override fun setUpAudioDualMedia(
    explorationId1: String,
    audioFileName1: String,
    explorationId2: String,
    audioFileName2: String
  ) {
    val dataSource = createAudioDataSource(
      explorationId = explorationId1, audioFileName = audioFileName1
    )
    val dataSource2 = createAudioDataSource(
      explorationId = explorationId2, audioFileName = audioFileName2
    )
    addShadowMediaPlayerException(dataSource!!, IOException("Test does not have networking"))
    addShadowMediaPlayerException(dataSource2!!, IOException("Test does not have networking"))
  }

  @Suppress("SameParameterValue")
  private fun createAudioDataSource(explorationId: String, audioFileName: String): Any? {
    val audioUrl = createAudioUrl(explorationId, audioFileName)

    val dataSourceClass = getDataSourceClass()

    val toDataSource =
      dataSourceClass.getDeclaredMethod(
        "toDataSource", String::class.java, Map::class.java
      )
    return toDataSource.invoke(/* obj= */ null, audioUrl, /* headers= */ null)
  }

  private fun addShadowMediaPlayerException(dataSource: Any, exception: Exception) {
    val shadowMediaPlayerClass = getShadowMediaPlayerClass()

    val addException =
      shadowMediaPlayerClass.getDeclaredMethod(
        "addException", dataSource.javaClass, IOException::class.java
      )
    addException.invoke(/* obj= */ null, dataSource, exception)
  }

  private fun createAudioUrl(explorationId: String, audioFileName: String): String {
    return "https://storage.googleapis.com/oppiaserver-resources/" +
      "exploration/$explorationId/assets/audio/$audioFileName"
  }

  private fun getDataSourceClass(): Class<*> {
    return context.classLoader.loadClass("org.robolectric.shadows.util.DataSource")
  }

  private fun getMediaInfoClass(): Class<*> {
    return context.classLoader.loadClass("org.robolectric.shadows.ShadowMediaPlayer\$MediaInfo")
  }

  private fun getShadowMediaPlayerClass(): Class<*> {
    return context.classLoader.loadClass("org.robolectric.shadows.ShadowMediaPlayer")
  }

  private fun getShadowsClass(): Class<*> {
    return context.classLoader.loadClass("org.robolectric.Shadows")
  }
}
