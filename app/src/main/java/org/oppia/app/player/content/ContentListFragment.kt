package org.oppia.app.player.content

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.app.fragment.InjectableFragment
import org.oppia.data.backends.gae.NetworkModule
import org.oppia.data.backends.gae.model.GaeExplorationContainer
import org.oppia.data.backends.gae.model.GaeState
import org.oppia.data.backends.gae.model.GaeSubtitledHtml
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

import java.util.ArrayList
import javax.inject.Inject

/** This fragment displays contents. It handles rich-text response
 */
class ContentListFragment : InjectableFragment() {

  @Inject
  lateinit var contentListFragmentPresenter: ContentListFragmentPresenter

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return contentListFragmentPresenter.handleCreateView(inflater, container)
  }
}
