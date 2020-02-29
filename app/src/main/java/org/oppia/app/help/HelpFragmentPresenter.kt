package org.oppia.app.help

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.app.databinding.HelpFragmentBinding
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** The presenter for [HelpFragment]. */
@FragmentScope
class HelpFragmentPresenter @Inject constructor(
  activity: AppCompatActivity,
  private val fragment: Fragment
) {
  private var recyclerView:RecyclerView ?= null
  private var customAdapter:HelpCategoryAdapter?=null
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup? , context : Context): View? {
    val binding = HelpFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    // NB: Both the view model and lifecycle owner must be set in order to correctly bind LiveData elements to
    // data-bound view models.
    binding.let {
      it.lifecycleOwner = fragment
    }
    recyclerView = binding.root.findViewById(R.id.help_fragment_recycler_view) as RecyclerView
    var helpViewModel:HelpViewModel = ViewModelProviders.of(fragment).get(HelpViewModel::class.java)
    helpViewModel.getArrayList().observe(fragment, Observer { categoryViewModels ->
      customAdapter = HelpCategoryAdapter(context,categoryViewModels!!)
      recyclerView!!.setLayoutManager(LinearLayoutManager(context))
      recyclerView!!.setAdapter(customAdapter)
    })
    return binding.root
  }
}
