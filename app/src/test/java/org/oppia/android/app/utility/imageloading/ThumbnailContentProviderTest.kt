package org.oppia.android.app.utility.imageloading

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/** Tests for [ThumbnailContentProvider]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class ThumbnailContentProviderTest {

  private lateinit var context: Context
  private lateinit var provider: ThumbnailContentProvider

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    provider = ThumbnailContentProvider()
    provider.attachInfo(context, null)
  }

  @Test
  fun testGetType_returnsSvgMimeType() {
    val uri = Uri.parse("content://org.oppia.android.provider.gcs/entity/id/assets/image/baker.img")
    val type = provider.getType(uri)
    assertThat(type).isEqualTo("image/svg+xml")
  }

  @Test
  fun testOpenFile_validThumbnail_returnsFileDescriptor() {
    val uri = Uri.parse("content://org.oppia.android.provider.gcs/entity/id/assets/image/baker.img")
    val pfd = provider.openFile(uri, "r")
    assertThat(pfd).isNotNull()
  }

  @Test
  fun testOpenFile_invalidThumbnail_returnsDefaultFileDescriptor() {
    val uri = Uri.parse("content://org.oppia.android.provider.gcs/entity/id/assets/image/invalid.img")
    val pfd = provider.openFile(uri, "r")
    assertThat(pfd).isNotNull()
  }
}
