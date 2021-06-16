package org.oppia.android.testing.espresso

import android.view.View
import android.widget.ImageView
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

/** A custom class that provides matching for ImageView. */
class ImageViewMatcher {
  companion object {
    fun hasScaleType(scaleType: ImageView.ScaleType): TypeSafeMatcher<View> =
      checkScaleType(scaleType)
  }

  /**
   * Checks that the image view has a required scale type.
   *
   * @returns a [TypeSafeMatcher] that checks if the given view is a image view and
   *   has a [scaleType].
   */
  private class checkScaleType(
    private val scaleType: ImageView.ScaleType
  ) : TypeSafeMatcher<View>() {
    override fun describeTo(description: Description) {
      description.appendText("has scale type $scaleType")
    }

    override fun matchesSafely(view: View): Boolean {
      return view is ImageView && view.scaleType == scaleType
    }
  }
}
