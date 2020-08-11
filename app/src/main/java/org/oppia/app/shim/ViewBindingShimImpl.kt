package org.oppia.app.shim

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
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
   * Handles binding inflation for [DragDropSortInteractionView] and returns the binding's view
   */
  override fun provideDragDropSortInteractionInflatedView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): View {
    return DragDropInteractionItemsBinding.inflate(
      LayoutInflater.from(parent.context), parent, /* attachToParent= */ false
    ).root
  }

  /**
   * Handles the binding for [DragDropSortInteractionView].
   */
  private lateinit var dragDropInteractionItemsBinding: DragDropInteractionItemsBinding

  override fun setDragDropInteractionItemsBinding(
    view: View
  ) {
    dragDropInteractionItemsBinding =
      DataBindingUtil.findBinding<DragDropInteractionItemsBinding>(view)!!
  }

  override fun setDragDropInteractionItemsBindingAdapter(
    adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
  ) {
    dragDropInteractionItemsBinding.adapter = adapter
  }

  override fun getDragDropInteractionItemsBindingRecyclerView(): RecyclerView {
    return dragDropInteractionItemsBinding.dragDropItemRecyclerview
  }

  override fun getDragDropInteractionItemsBindingGroupItem(): ImageButton {
    return dragDropInteractionItemsBinding.dragDropContentGroupItem
  }

  override fun getDragDropInteractionItemsBindingUnlinkItems(): ImageButton {
    return dragDropInteractionItemsBinding.dragDropContentUnlinkItems
  }

  override fun getDragDropInteractionItemsBindingAccessibleContainer(): LinearLayout {
    return dragDropInteractionItemsBinding.dragDropAccessibleContainer
  }

  override fun setDragDropInteractionItemsBindingViewModel(
    viewModel: DragDropInteractionContentViewModel
  ) {
    dragDropInteractionItemsBinding.viewModel = viewModel
  }

  override fun provideDragDropSingleItemInflatedView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): View {
    return DragDropSingleItemBinding.inflate(
      LayoutInflater.from(parent.context),
      parent,
      false
    ).root
  }

  private lateinit var dragDropSingleItemBinding: DragDropSingleItemBinding

  override fun setDragDropSingleItemBinding(
    view: View
  ) {
    dragDropSingleItemBinding =
      DataBindingUtil.findBinding<DragDropSingleItemBinding>(view)!!
  }

  override fun setDragDropSingleItemBindingHtmlContent(
    htmlParserFactory: HtmlParser.Factory,
    resourceBucketName: String,
    entityType: String,
    entityId: String,
    viewModel: String
  ) {
    dragDropSingleItemBinding.htmlContent = htmlParserFactory.create(
      resourceBucketName,
      entityType,
      entityId,
      false
    ).parseOppiaHtml(
      viewModel,
      dragDropSingleItemBinding.dragDropContentTextView
    )
  }

  override fun getDefaultRegion(parentView: FrameLayout): View {
    return parentView.findViewById<View>(R.id.default_selected_region)
  }
}
