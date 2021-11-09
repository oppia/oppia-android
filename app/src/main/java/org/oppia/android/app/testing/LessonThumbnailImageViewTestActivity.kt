package org.oppia.android.app.testing

import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.customview.LessonThumbnailImageView
import org.oppia.android.app.utility.activity.InjectableAppCompatActivity

/** Test-only activity for verifying behaviors of [LessonThumbnailImageView]. */
class LessonThumbnailImageViewTestActivity : InjectableAppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.lesson_thumbnail_image_view_test_activity)

    supportFragmentManager.beginTransaction().add(
      R.id.lesson_thumbnail_image_view_test_fragment_placeholder,
      LessonThumbnailImageViewTestFragment()
    ).commitNow()
  }
}
