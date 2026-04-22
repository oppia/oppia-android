package org.oppia.android.app.help.thirdparty

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.databinding.databinding.ThirdPartyDependencyListActivityBinding
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.ui.R
import org.oppia.android.app.utility.EdgeToEdgeHelper
import org.oppia.android.util.platformparameter.EnableEdgeToEdge
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/** The presenter for [ThirdPartyDependencyListActivity]. */
@ActivityScope
class ThirdPartyDependencyListActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val resourceHandler: AppLanguageResourceHandler,
  @EnableEdgeToEdge
  private val enableEdgeToEdge: PlatformParameterValue<Boolean>
) {

  /** Handles onCreate() method of the [ThirdPartyDependencyListActivity]. */
  fun handleOnCreate(isMultipane: Boolean) {
    if (enableEdgeToEdge.value) {
      EdgeToEdgeHelper.enableEdgeToEdgeDispatch(activity)
    }
    val binding =
      DataBindingUtil.setContentView<ThirdPartyDependencyListActivityBinding>(
        activity,
        R.layout.third_party_dependency_list_activity
      )
    binding.apply {
      lifecycleOwner = activity
    }

    val thirdPartyDependencyListActivityToolbar = binding.thirdPartyDependencyListActivityToolbar
    activity.setSupportActionBar(thirdPartyDependencyListActivityToolbar)
    activity.supportActionBar!!.title = resourceHandler.getStringInLocale(
      R.string.third_party_dependency_list_activity_title
    )
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    binding.thirdPartyDependencyListActivityToolbar.setNavigationOnClickListener {
      (activity as ThirdPartyDependencyListActivity).finish()
    }
    if (enableEdgeToEdge.value) {
      EdgeToEdgeHelper.applyToAppBarLayout(
        activity,
        thirdPartyDependencyListActivityToolbar,
        R.color.component_color_shared_activity_status_bar_color
      )
    }

    if (getThirdPartyDependencyListFragment() == null) {
      val thirdPartyDependencyListFragment =
        ThirdPartyDependencyListFragment.newInstance(isMultipane)
      activity.supportFragmentManager.beginTransaction().add(
        R.id.third_party_dependency_list_fragment_placeholder,
        thirdPartyDependencyListFragment
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
