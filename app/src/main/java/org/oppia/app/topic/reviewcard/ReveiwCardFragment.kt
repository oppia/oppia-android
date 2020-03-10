package org.oppia.app.topic.reviewcard

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableDialogFragment
import javax.inject.Inject

/* Fragment that displays review card */
class ReviewCardFragment : InjectableDialogFragment() {

  @Inject lateinit var reviewCardFragmentPresenter: ReviewCardFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    super.onCreateView(inflater, container, savedInstanceState)
    return reviewCardFragmentPresenter.handleCreateView(inflater, container)
  }
}
