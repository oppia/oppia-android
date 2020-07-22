package org.oppia.app.testing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import javax.inject.Inject

/** The presenter for [ImageRegionSelectionTestActivity] */
class ImageRegionSelectionTestFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    return inflater.inflate(R.layout.image_region_selection_test_fragment, container, false)
  }
}
