package org.oppia.app.story

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import javax.inject.Inject

interface StoryFragmentInterface {

  fun onAttach(context: Context)

  fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View?

  fun selectExploration(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    backflowScreen: Int?
  )

  fun smoothScrollToPosition(position: Int)
}