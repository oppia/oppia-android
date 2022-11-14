package org.oppia.android.app.testing

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.Point2d
import org.oppia.android.app.utility.ClickableAreasImage
import org.oppia.android.app.utility.OnClickableAreaClickedListener
import org.oppia.android.app.utility.RegionClickedEvent
import javax.inject.Inject

const val IMAGE_REGION_SELECTION_TEST_FRAGMENT_TAG = "image_region_selection_test_fragment"

// TODO(#59): Make this fragment only included in relevant tests instead of all prod builds.
/** Test Fragment used for testing [ClickableAreasImage] functionality */
class ImageRegionSelectionTestFragment : InjectableFragment(), OnClickableAreaClickedListener {
  @Inject
  lateinit var imageRegionSelectionTestFragmentPresenter:
    ImageRegionSelectionTestFragmentPresenter

  lateinit var mockOnClickableAreaClickedListener: OnClickableAreaClickedListener

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return imageRegionSelectionTestFragmentPresenter.handleCreateView(inflater, container)
  }

  override fun onClickableAreaTouched(region: RegionClickedEvent, coordinates: Point2d) {
    mockOnClickableAreaClickedListener.onClickableAreaTouched(region, coordinates)
  }
}
