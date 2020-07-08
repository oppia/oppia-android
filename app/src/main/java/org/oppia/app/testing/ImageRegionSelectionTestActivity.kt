package org.oppia.app.testing

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.utility.OnClickableAreaClickedListener
import org.oppia.app.utility.ClickableAreasImage
import javax.inject.Inject

/** Test Activity used for testing [ClickableAreasImage] functionality */
class ImageRegionSelectionTestActivity :
  InjectableAppCompatActivity(),
  OnClickableAreaClickedListener {

  @Inject lateinit var imageRegionSelectionTestActivityPresenter: ImageRegionSelectionTestActivityPresenter // ktlint-disable max-line-length

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    imageRegionSelectionTestActivityPresenter.handleOnCreate()
  }

  override fun onClickableAreaTouched(region: String) {

  }
}
