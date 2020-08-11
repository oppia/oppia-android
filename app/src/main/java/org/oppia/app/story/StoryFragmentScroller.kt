package org.oppia.app.story

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

interface StoryFragmentScroller {
  fun smoothScrollToPosition(position: Int)
}
