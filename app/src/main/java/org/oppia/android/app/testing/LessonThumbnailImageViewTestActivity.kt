package org.oppia.android.app.testing

import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.InjectableAppCompatActivity

class LessonThumbnailImageViewTestActivity : InjectableAppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.lesson_thumbnail_image_view_test_activity)

    supportFragmentManager.beginTransaction().add(
      R.id.mock_fragment_presenter,
      LessonThumbnailImageViewTestFragment()
    ).commitNow()
  }
}
