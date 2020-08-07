package org.oppia.app.screenshottesting

import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import androidx.test.platform.app.InstrumentationRegistry
import com.facebook.testing.screenshot.Screenshot
import com.facebook.testing.screenshot.ViewHelpers
import org.junit.Test
import org.oppia.app.R

class HomeFragmentScreenshotTesting {

  @Test
  fun runScreenshotTesting() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val themedContext = ContextThemeWrapper(context, R.style.OppiaTheme)
    val view = LayoutInflater.from(themedContext).inflate(R.layout.home_fragment, null, false)
    ViewHelpers.setupView(view)
      .setExactWidthPx(300)
      .layout()
    Screenshot.snap(view)
      .record()
  }
}
