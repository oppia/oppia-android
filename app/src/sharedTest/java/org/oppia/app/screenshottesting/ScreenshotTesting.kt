package org.oppia.app.screenshottesting

import android.view.LayoutInflater
import androidx.test.platform.app.InstrumentationRegistry
import com.facebook.testing.screenshot.Screenshot
import com.facebook.testing.screenshot.ViewHelpers
import org.junit.Test
import org.oppia.app.R

class ScreenshotTesting {

  @Test
  fun runScreenshotTesting() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val view = LayoutInflater.from(context).inflate(R.layout.admin_auth_activity, null, false)
    ViewHelpers.setupView(view)
      .setExactWidthPx(300)
      .layout()
    Screenshot.snap(view)
      .record()
  }
}