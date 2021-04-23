package org.oppia.android.app.testing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.databinding.LessonThumbnailImageViewTestFragmentBinding

/** Test-only fragment for verifying behaviors of LessonThumbnailImageView. */
class LessonThumbnailImageViewTestFragment : InjectableFragment() {

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val binding = LessonThumbnailImageViewTestFragmentBinding
      .inflate(inflater, container, /* attachToRoot= */ false)
    return binding.root
  }
}
