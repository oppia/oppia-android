package org.oppia.app.topic.review

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment to show content for skill review. */
class ReviewFragment: InjectableFragment(){
  @Inject
  lateinit var reviewFragmentPresenter: ReviewFragmentPresenter

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return reviewFragmentPresenter.handleCreateView(inflater, container)
  }
}
