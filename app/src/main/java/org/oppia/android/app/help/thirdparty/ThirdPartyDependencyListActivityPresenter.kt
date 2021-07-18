package org.oppia.android.app.help.thirdparty

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.databinding.ThirdPartyDependencyListActivityBinding
import javax.inject.Inject

/** The presenter for [ThirdPartyDependencyListActivity]. */
@ActivityScope
class ThirdPartyDependencyListActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private lateinit var thirdPartyDependencyListActivityToolbar: Toolbar

  fun handleOnCreate() {
    val binding =
      DataBindingUtil.setContentView<ThirdPartyDependencyListActivityBinding>(
        activity,
        R.layout.third_party_dependency_list_activity
      )
    binding.apply {
      lifecycleOwner = activity
    }

//    thirdPartyDependencyListActivityToolbar = binding.thirdPartyDependencyListActivityToolbar
//    activity.setSupportActionBar(thirdPartyDependencyListActivityToolbar)

//    activity.supportActionBar!!.title = activity.getString(R.string.third_party_dependency_list_activity_title)
//    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
//    activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    binding.thirdPartyDependencyListActivityToolbar.setNavigationOnClickListener {
      (activity as ThirdPartyDependencyListActivity).finish()
    }

    if (getThirdPartyDependencyListFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.third_party_dependency_list_fragment_placeholder,
        ThirdPartyDependencyListFragment()
      ).commitNow()
    }
  }

  private fun getThirdPartyDependencyListFragment(): ThirdPartyDependencyListFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(R.id.third_party_dependency_list_fragment_placeholder) as
      ThirdPartyDependencyListFragment?
  }
}
