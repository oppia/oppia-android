package org.oppia.android.app.testing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.customview.LessonThumbnailImageView
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.ui.R

/** Test-only fragment for verifying behaviors of [LessonThumbnailImageView]. */
class LessonThumbnailImageViewTestFragment : InjectableFragment() {

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return inflater.inflate(
      R.layout.lesson_thumbnail_image_view_test_fragment,
      container,
      /* attachToRoot= */ false
    )
  }
}
