package org.oppia.app.player.content

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.app.R
import org.oppia.app.databinding.ContentListFragmentBinding
import org.oppia.app.model.EphemeralState
import org.oppia.app.player.state.StateViewModel
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.exploration.ExplorationProgressController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** Presenter for [ContentListFragment]. */
class ContentListFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<ContentViewModel>,
  private val explorationProgressController: ExplorationProgressController,
  private val logger: Logger
) {

  private var entityType: String = ""
  private var entityId: String = ""

  lateinit var contentCardAdapter: ContentCardAdapter

  var contentList: MutableList<ContentViewModel> = ArrayList()

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = ContentListFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)

    entityType = fragment.arguments!!.getString("entityType")
    entityId = fragment.arguments!!.getString("exploration_id")

    binding.recyclerview.apply {
      contentCardAdapter =
        ContentCardAdapter(context, entityType, entityId, contentList);
      binding.recyclerview.adapter = contentCardAdapter
    }
    subscribeToCurrentState()

    return binding.root
  }

  private fun subscribeToCurrentState() {
    ephemeralStateLiveData.observe(fragment, Observer<EphemeralState> { result ->
      logger.d("StateFragment", "getCurrentState: ${result.state.content.html}")
      if (!result.state.content.contentId.equals("")) {
        getContentViewModel().contentId = result.state.content.contentId
      }else{
        getContentViewModel().contentId = "content"
      }
      getContentViewModel().htmlContent = result.state.content.html
      bindContentList()
    })
  }

  private fun bindContentList() {
    contentList.add(getContentViewModel())
    contentCardAdapter.notifyDataSetChanged()
  }

  private fun getContentViewModel(): ContentViewModel {
    return viewModelProvider.getForFragment(fragment, ContentViewModel::class.java)
  }

  private val ephemeralStateLiveData: LiveData<EphemeralState> by lazy {
    getEphemeralState()
  }

  private fun getEphemeralState(): LiveData<EphemeralState> {
    return Transformations.map(explorationProgressController.getCurrentState(), ::processCurrentState)
  }

  private fun processCurrentState(ephemeralStateResult: AsyncResult<EphemeralState>): EphemeralState {
    if (ephemeralStateResult.isFailure()) {
      logger.e("StateFragment", "Failed to retrieve ephemeral state", ephemeralStateResult.getErrorOrNull()!!)
    }
    return ephemeralStateResult.getOrDefault(EphemeralState.getDefaultInstance())
  }
}
