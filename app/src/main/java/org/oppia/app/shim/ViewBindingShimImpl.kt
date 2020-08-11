package org.oppia.app.shim

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.app.databinding.DragDropInteractionItemsBinding
import org.oppia.app.databinding.DragDropSingleItemBinding
import org.oppia.app.databinding.ItemSelectionInteractionItemsBinding
import org.oppia.app.databinding.MultipleChoiceInteractionItemsBinding
import org.oppia.app.databinding.ProfileInputViewBinding
import org.oppia.app.player.state.itemviewmodel.DragDropInteractionContentViewModel
import org.oppia.app.player.state.itemviewmodel.SelectionInteractionContentViewModel
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.util.parser.HtmlParser
import javax.inject.Inject

/**
 * Extracts binding file dependencies from view files in order for Bazel to build.
 */
class ViewBindingShimImpl @Inject constructor() : ViewBindingShim {

  /**
   * Handles binding inflation for [ProfileInputView] and returns the binding's labelText
   */
  override fun provideProfileInputViewBindingLabelText(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): TextView {
    val binding = DataBindingUtil.inflate<ProfileInputViewBinding>(
      inflater,
      R.layout.profile_input_view,
      parent,
      attachToParent
    )
    return binding.labelText
  }

  /**
   * Handles binding inflation for [ProfileInputView] and returns the binding's input
   */
  override fun provideProfileInputViewBindingInput(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): EditText {
    val binding = DataBindingUtil.inflate<ProfileInputViewBinding>(
      inflater,
      R.layout.profile_input_view,
      parent,
      attachToParent
    )
    return binding.input
  }

  /**
   * Handles binding inflation for [ProfileInputView] and returns the binding's errorText
   */
  override fun provideProfileInputViewBindingErrorText(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): TextView {
    val binding = DataBindingUtil.inflate<ProfileInputViewBinding>(
      inflater,
      R.layout.profile_input_view,
      parent,
      attachToParent
    )
    return binding.errorText
  }

  /**
   * Handles binding inflation for [SelectionInteractionView]'s ItemSelectionInteraction and
   * returns the binding's root
   */
  override fun provideSelectionInteractionViewInflatedView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): View {
    return ItemSelectionInteractionItemsBinding.inflate(
      LayoutInflater.from(parent.context),
      parent,
      /* attachToParent= */ false
    ).root
  }

  /**
   * Handles binding inflation for [SelectionInteractionView]'s ItemSelectionInteraction and
   * returns the binding's viewModel
   */
  override fun provideSelectionInteractionViewModel(
    view: View,
    viewModel: SelectionInteractionContentViewModel,
    htmlParserFactory: HtmlParser.Factory,
    resourceBucketName: String,
    entityType: String,
    entityId: String
  ) {
    val binding =
      DataBindingUtil.findBinding<ItemSelectionInteractionItemsBinding>(view)!!
    binding.htmlContent =
      htmlParserFactory.create(
        resourceBucketName,
        entityType,
        entityId,
        false
      ).parseOppiaHtml(
        viewModel.htmlContent,
        binding.itemSelectionContentsTextView
      )
    binding.viewModel = viewModel
  }

  /**
   * Handles binding inflation for [SelectionInteractionView]'s MultipleChoiceInteraction and
   * returns the binding's view
   */
  override fun provideMultipleChoiceInteractionItemsInflatedView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): View {
    return MultipleChoiceInteractionItemsBinding.inflate(
      LayoutInflater.from(parent.context),
      parent,
      false
    ).root
  }

  /**
   * Handles binding inflation for [SelectionInteractionView]'s MultipleChoiceInteraction and
   * returns the binding's viewModel
   */
  override fun provideMultipleChoiceInteractionItemsViewModel(
    view: View,
    viewModel: SelectionInteractionContentViewModel,
    htmlParserFactory: HtmlParser.Factory,
    resourceBucketName: String,
    entityType: String,
    entityId: String
  ) {
    val binding =
      DataBindingUtil.findBinding<MultipleChoiceInteractionItemsBinding>(view)!!
    binding.htmlContent =
      htmlParserFactory.create(
        resourceBucketName, entityType, entityId, /* imageCenterAlign= */ false
      ).parseOppiaHtml(
        viewModel.htmlContent, binding.multipleChoiceContentTextView
      )
    binding.viewModel = viewModel
  }

  /**
   * Handles implementation of createAdapter() function in [DragDropSortInteractionView] in order
   * for the file not to depend on [DragDropInteractionItemsBinding].
   */
  override fun createDragDropInteractionViewAdapter(
    adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
    isMultipleItemsInSamePositionAllowed: Boolean,
    isAccessibilityEnabled: Boolean,
    htmlParserFactory: HtmlParser.Factory,
    resourceBucketName: String,
    entityType: String,
    entityId: String
  ): BindableAdapter<DragDropInteractionContentViewModel> {
    return BindableAdapter.SingleTypeBuilder
      .newBuilder<DragDropInteractionContentViewModel>()
      .registerViewBinder(
        inflateView = { parent ->
          DragDropInteractionItemsBinding.inflate(
            LayoutInflater.from(parent.context), parent, /* attachToParent= */ false
          ).root
        },
        bindView = { view, viewModel ->
          val binding = DataBindingUtil.findBinding<DragDropInteractionItemsBinding>(view)!!
          binding.dragDropItemRecyclerview.adapter =
            createDragDropInteractionViewNestedAdapter<String>(
              htmlParserFactory,
              resourceBucketName,
              entityType,
              entityId
            )
          binding.adapter = adapter
          binding.dragDropContentGroupItem.isVisible = isMultipleItemsInSamePositionAllowed
          binding.dragDropContentUnlinkItems.isVisible = viewModel.htmlContent.htmlList.size > 1
          binding.dragDropAccessibleContainer.isVisible = isAccessibilityEnabled
          binding.viewModel = viewModel
        }
      )
      .build()
  }

  /**
   * Handles implementation of createNestedAdapter() function in [DragDropSortInteractionView] in
   * order for the file not to depend on [DragDropInteractionItemsBinding].
   */
  override fun <T> createDragDropInteractionViewNestedAdapter(
    htmlParserFactory: HtmlParser.Factory,
    resourceBucketName: String,
    entityType: String,
    entityId: String
  ): BindableAdapter<String> {
    return BindableAdapter.SingleTypeBuilder
      .newBuilder<String>()
      .registerViewBinder(
        inflateView = { parent ->
          DragDropSingleItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, /* attachToParent= */ false
          ).root
        },
        bindView = { view, viewModel ->
          val binding = DataBindingUtil.findBinding<DragDropSingleItemBinding>(view)!!
          binding.htmlContent = htmlParserFactory.create(
            resourceBucketName,
            entityType,
            entityId,
            /* imageCenterAlign= */ false
          )
            .parseOppiaHtml(
              viewModel, binding.dragDropContentTextView
            )
        }
      )
      .build()
  }

  override fun getDefaultRegion(parentView: FrameLayout): View {
    return parentView.findViewById<View>(R.id.default_selected_region)
  }
}
