package org.oppia.app.help

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import org.oppia.app.viewmodel.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.app.databinding.HelpFragmentBinding
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** The presenter for [HelpFragment]. */
@FragmentScope
class HelpFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<HelpViewModel>
) {
  private var recyclerView:RecyclerView ?= null
  private var helpCategoryAdapter:HelpCategoryAdapter?=null
  var arrayListMutableLiveData = MutableLiveData<ArrayList<HelpViewModel>>()
  var arrayList = ArrayList<HelpViewModel>()
  private lateinit var binding: HelpFragmentBinding

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup? ): View? {
    binding = HelpFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    // NB: Both the view model and lifecycle owner must be set in order to correctly bind LiveData elements to
    // data-bound view models.
    binding.let {
      it.lifecycleOwner = fragment
    }
    recyclerView = binding.root.findViewById(R.id.help_fragment_recycler_view) as? RecyclerView
    val viewModel = getHelpModel()
    getRecyclerViewItemList().observe(fragment, Observer { helpViewModels ->
      helpCategoryAdapter = HelpCategoryAdapter(activity,helpViewModels!!)
      recyclerView!!.setLayoutManager(LinearLayoutManager(activity))
      recyclerView!!.setAdapter(helpCategoryAdapter)
    })
    return binding.root
  }

  private fun getHelpModel(): HelpViewModel {
    return viewModelProvider.getForFragment(fragment, HelpViewModel::class.java)
  }

  private fun getRecyclerViewItemList(): MutableLiveData<ArrayList<HelpViewModel>> {
    for (dir in HelpItems.values()) {
      if(dir.equals(HelpItems.FAQ)){
        val category1 = fragment.getString(R.string.frequently_asked_questions_FAQ)
        val helpViewModel1: HelpViewModel = HelpViewModel(category1)
        arrayList!!.add(helpViewModel1)
      }
    }
    arrayListMutableLiveData.value = arrayList
    return arrayListMutableLiveData
  }
}
