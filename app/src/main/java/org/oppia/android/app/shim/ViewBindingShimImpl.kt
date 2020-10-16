package org.oppia.android.app.shim

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
import org.oppia.android.R
import org.oppia.android.app.player.state.itemviewmodel.DragDropInteractionContentViewModel
import org.oppia.android.app.player.state.itemviewmodel.SelectionInteractionContentViewModel
import org.oppia.android.databinding.DragDropInteractionItemsBinding
import org.oppia.android.databinding.DragDropSingleItemBinding
import org.oppia.android.databinding.ItemSelectionInteractionItemsBinding
import org.oppia.android.databinding.MultipleChoiceInteractionItemsBinding
import org.oppia.android.databinding.ProfileInputViewBinding
import org.oppia.android.util.parser.HtmlParser
import javax.inject.Inject

/**
 * Creates bindings for Views in order to avoid View files directly depending on Binding files.
 * When working on a View file, developers should refrain from directly referencing Binding files
 * by adding all related functionality here.
 *
 * Please note that this file is temporary and all functionality will be returned to it's respective
 * View once Gradle has been removed.
 */
// TODO(#1619): Remove file post-Gradle
class ViewBindingShimImpl @Inject constructor() : ViewBindingShim {

  override fun inflateProfileInputView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): View {
    return DataBindingUtil.inflate<ProfileInputViewBinding>(
      inflater,
      R.layout.profile_input_view,
      parent,
      attachToParent
    ).root
  }

  override fun provideProfileInputViewBindingLabelText(
    profileInputView: View
  ): TextView {
    return DataBindingUtil.findBinding<ProfileInputViewBinding>(profileInputView)!!.labelText
  }

  override fun provideProfileInputViewBindingInput(
    profileInputView: View
  ): EditText {
    return DataBindingUtil.findBinding<ProfileInputViewBinding>(profileInputView)!!.input
  }

  override fun provideProfileInputViewBindingErrorText(
    profileInputView: View
  ): TextView {
    return DataBindingUtil.findBinding<ProfileInputViewBinding>(profileInputView)!!.errorText
  }

  override fun provideDragDropSortInteractionInflatedView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): View {
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
      LayoutInflater.from(parent.context), parent, /* attachToParent= */ false
    ).root
  }

  override fun getDefaultRegion(parentView: FrameLayout): View {
    return parentView.findViewById<View>(R.id.default_selected_region)
  }
}
