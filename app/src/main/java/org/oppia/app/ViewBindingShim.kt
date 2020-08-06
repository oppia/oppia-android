package org.oppia.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.databinding.DragDropInteractionItemsBinding
import org.oppia.app.databinding.DragDropSingleItemBinding
import org.oppia.app.databinding.ItemSelectionInteractionItemsBinding
import org.oppia.app.databinding.MultipleChoiceInteractionItemsBinding
import org.oppia.app.databinding.ProfileInputViewBinding
import org.oppia.app.fragment.InjectableFragment
import org.oppia.app.player.state.itemviewmodel.DragDropInteractionContentViewModel
import org.oppia.app.player.state.itemviewmodel.SelectionInteractionContentViewModel
import org.oppia.util.parser.HtmlParser

class ViewBindingShim :
  ViewBindingShimInterface {

  override fun provideProfileInputViewBindingLabelText(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean): TextView {
    val binding = DataBindingUtil.inflate<ProfileInputViewBinding>(inflater,
      R.layout.profile_input_view,parent,attachToParent)
    return binding.labelText
  }

  override fun provideProfileInputViewBindingInput(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean): EditText {
    val binding = DataBindingUtil.inflate<ProfileInputViewBinding>(inflater,
      R.layout.profile_input_view,parent,attachToParent)
    return binding.input
  }

  override fun provideProfileInputViewBindingErrorText(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean): TextView {
    val binding = DataBindingUtil.inflate<ProfileInputViewBinding>(inflater,
      R.layout.profile_input_view,parent,attachToParent)
    return binding.errorText
  }

  override fun provideSelectionInteractionViewInflatedView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean): View {
    return ItemSelectionInteractionItemsBinding.inflate(
      LayoutInflater.from(parent.context), parent, /* attachToParent= */ false
    ).root
  }

  override fun provideSelectionInteractionViewModel(
    view: View,
    viewModel: SelectionInteractionContentViewModel,
    htmlParserFactory: HtmlParser.Factory,
    resourceBucketName: String,
    entityType: String,
    entityId: String) {
    val binding =
      DataBindingUtil.findBinding<ItemSelectionInteractionItemsBinding>(view)!!
    binding.htmlContent =
      htmlParserFactory.create(
        resourceBucketName, entityType, entityId, /* imageCenterAlign= */ false
      ).parseOppiaHtml(
        viewModel.htmlContent, binding.itemSelectionContentsTextView
      )
    binding.viewModel = viewModel
  }

  override fun provideMultipleChoiceInteractionItemsInflatedView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean): View {
    return MultipleChoiceInteractionItemsBinding.inflate(
      LayoutInflater.from(parent.context), parent, /* attachToParent= */ false
    ).root
  }

  override fun provideMultipleChoiceInteractionItemsViewModel(
    view: View,
    viewModel: SelectionInteractionContentViewModel,
    htmlParserFactory: HtmlParser.Factory,
    resourceBucketName: String,
    entityType: String,
    entityId: String) {
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

  override fun provideFragmentManager(view: View) {
    return FragmentManager.findFragment<InjectableFragment>(view).createViewComponent(view).inject(this)
  }

  override fun provideDragDropSortInteractionInflatedView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean): View {
    return DragDropInteractionItemsBinding.inflate(
      LayoutInflater.from(parent.context), parent, /* attachToParent= */ false
    ).root
  }

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
    attachToParent: Boolean): View {
    return DragDropSingleItemBinding.inflate(
      LayoutInflater.from(parent.context), parent, /* attachToParent= */ false
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
      /* imageCenterAlign= */ false
    ).parseOppiaHtml(
      viewModel, dragDropSingleItemBinding.dragDropContentTextView
    )
  }

  override fun getDefaultRegion(parentView: FrameLayout): View {
    return parentView.findViewById<View>(R.id.default_selected_region)
  }

}
