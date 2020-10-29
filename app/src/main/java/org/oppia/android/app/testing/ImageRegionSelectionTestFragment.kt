package org.oppia.android.app.testing

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.utility.ClickableAreasImage
import javax.inject.Inject

const val IMAGE_REGION_SELECTION_TEST_FRAGMENT_ARGUMENT_KEY = "ImageRegionSelectionTestFragment.image_region_selection_test_fragment"

// TODO(#59): Make this fragment only included in relevant tests instead of all prod builds.
/** Test Fragment used for testing [ClickableAreasImage] functionality */
class ImageRegionSelectionTestFragment : InjectableFragment() {
  @Inject
  lateinit var imageRegionSelectionTestFragmentPresenter:
    ImageRegionSelectionTestFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return imageRegionSelectionTestFragmentPresenter.handleCreateView(inflater, container)
  }
}
