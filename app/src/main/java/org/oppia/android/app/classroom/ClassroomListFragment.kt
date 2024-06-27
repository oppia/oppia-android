package org.oppia.android.app.classroom

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.home.classroomlist.ClassroomSummaryClickListener
import org.oppia.android.app.home.topiclist.TopicSummaryClickListener
import org.oppia.android.app.model.ClassroomSummary
import org.oppia.android.app.model.TopicSummary
import javax.inject.Inject

/** Fragment that displays the classroom list screen. */
class ClassroomListFragment :
  InjectableFragment(),
  TopicSummaryClickListener,
  ClassroomSummaryClickListener {
  @Inject
  lateinit var classroomListFragmentPresenter: ClassroomListFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return classroomListFragmentPresenter.handleCreateView(inflater, container)
  }

  override fun onTopicSummaryClicked(topicSummary: TopicSummary) {
    classroomListFragmentPresenter.onTopicSummaryClicked(topicSummary)
  }

  override fun onClassroomSummaryClicked(classroomSummary: ClassroomSummary) {
    classroomListFragmentPresenter.onClassroomSummaryClicked(classroomSummary)
  }
}
