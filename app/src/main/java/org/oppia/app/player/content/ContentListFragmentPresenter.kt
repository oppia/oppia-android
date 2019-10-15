package org.oppia.app.player.content

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.app.databinding.ContentListFragmentBinding
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** Presenter for [ContentListFragment]. */
class ContentListFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<ContentViewModel>,
  private val logger: Logger
) {

  private val entityType: String = "exploration"

  lateinit var contentCardAdapter: ContentCardAdapter

  var contentList: MutableList<ContentViewModel> = ArrayList()

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = ContentListFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.let {
      it.viewModel = getContentViewModel()
      it.contentFragment = fragment as ContentListFragment
      it.lifecycleOwner = fragment
    }
    binding.recyclerview.apply {
      binding.recyclerview.layoutManager = LinearLayoutManager(context)
      contentCardAdapter =
        ContentCardAdapter(context, entityType, fragment.arguments!!.getString("exploration_id"), contentList);
      binding.recyclerview.adapter = contentCardAdapter
    }
    getContentList()

    return binding.root
  }

  private fun getContentList() {
    getContentViewModel().contentId = fragment.arguments!!.getString("content_id")
    getContentViewModel().htmlContent = fragment.arguments!!.getString("htmlContent")
    logger.d("ContentListFragment", "htmlcontent: ${fragment.arguments!!.getString("htmlContent")}")
    contentList.add(getContentViewModel())
    contentCardAdapter!!.notifyDataSetChanged()
  }

  private fun getContentViewModel(): ContentViewModel {
    return viewModelProvider.getForFragment(fragment, ContentViewModel::class.java)
  }
}
