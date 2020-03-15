package org.oppia.app.help.faq

import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.app.databinding.FaqContentBinding
import org.oppia.app.databinding.FaqFragmentBinding
import org.oppia.app.databinding.FaqItemHeaderBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.help.faq.faqItemViewModel.FAQContentViewModel
import org.oppia.app.help.faq.faqItemViewModel.FAQHeaderViewModel
import org.oppia.app.help.faq.faqItemViewModel.FAQItemViewModel
import org.oppia.app.recyclerview.BindableAdapter
import javax.inject.Inject

/** The presenter for [FAQFragment]. */
@FragmentScope
class FAQFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment
) {
  private val arrayList = ArrayList<FAQContentViewModel>()
  private lateinit var binding: FaqFragmentBinding
  private lateinit var linearLayoutManager: LinearLayoutManager
  private lateinit var linearSmoothScroller: RecyclerView.SmoothScroller

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    linearLayoutManager = LinearLayoutManager(activity.applicationContext)
    linearSmoothScroller = createSmoothScroller()
    binding = FaqFragmentBinding.inflate(inflater, container, /* attachToRoot = */ false)
    binding.lifecycleOwner = fragment
    binding.faqFragmentRecyclerView.apply {
      layoutManager = linearLayoutManager
      adapter = createRecyclerViewAdapter()
    }
    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<FAQItemViewModel> {
    return BindableAdapter.MultiTypeBuilder
      .newBuilder<FAQItemViewModel, ViewType> { viewModel ->
        when (viewModel) {
          is FAQHeaderViewModel -> ViewType.VIEW_TYPE_HEADER
          is FAQContentViewModel -> ViewType.VIEW_TYPE_CONTENT
          else -> throw IllegalArgumentException("Encountered unexpected view model: $viewModel")
        }
      }
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_HEADER,
        inflateDataBinding = FaqItemHeaderBinding::inflate,
        setViewModel = FaqItemHeaderBinding::setViewModel,
        transformViewModel = { it as FAQHeaderViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_CONTENT,
        inflateDataBinding = FaqContentBinding::inflate,
        setViewModel = FaqContentBinding::setViewModel,
        transformViewModel = { it as FAQContentViewModel }
      )

      .build()
  }

  private enum class ViewType {
    VIEW_TYPE_HEADER,
    VIEW_TYPE_CONTENT
  }

  private fun getRecyclerViewItemList(): ArrayList<FAQContentViewModel> {
    val questions: Array<String> = activity.resources.getStringArray(R.array.faq_questions)
    for (question in questions) {
      val faqViewModel =
        FAQContentViewModel(question)
      arrayList.add(faqViewModel)
    }
    return arrayList
  }

  private fun createSmoothScroller(): RecyclerView.SmoothScroller {
    val milliSecondsPerInch = 100f

    return object : LinearSmoothScroller(activity) {
      override fun getVerticalSnapPreference(): Int {
        return SNAP_TO_START
      }

      override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
        return milliSecondsPerInch / displayMetrics!!.densityDpi
      }

      override fun calculateDyToMakeVisible(view: View, snapPreference: Int): Int {
        return super.calculateDyToMakeVisible(view, snapPreference) + dipToPixels(48)
      }
    }
  }

  private fun dipToPixels(dipValue: Int): Int {
    return TypedValue.applyDimension(
      TypedValue.COMPLEX_UNIT_DIP,
      dipValue.toFloat(),
      Resources.getSystem().displayMetrics
    ).toInt()
  }
}
