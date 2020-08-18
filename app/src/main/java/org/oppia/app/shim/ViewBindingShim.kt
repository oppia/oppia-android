package org.oppia.app.shim

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.player.state.itemviewmodel.DragDropInteractionContentViewModel
import org.oppia.app.player.state.itemviewmodel.SelectionInteractionContentViewModel
import org.oppia.util.parser.HtmlParser

/**
 * Creates bindings for Views in order to avoid View files directly depending on Binding files.
 * When working on a View file, developers should refrain from directly referencing Binding files
 * by adding all related functionality here.
 *
 * Please note that this file is temporary and all functionality will be returned to it's respective
 * View once Gradle has been removed.
 */
// TODO(#1619): Remove file post-Gradle
interface ViewBindingShim {

  /**
   * Handles binding inflation for [ProfileInputView] and returns the binding's labelText.
   */
  fun provideProfileInputViewBindingLabelText(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): TextView

  /**
   * Handles binding inflation for [ProfileInputView] and returns the binding's input.
   */
  fun provideProfileInputViewBindingInput(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): EditText

  /**
   * Handles binding inflation for [ProfileInputView] and returns the binding's errorText.
   */
  fun provideProfileInputViewBindingErrorText(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): TextView

  /**
   * Handles binding inflation for [SelectionInteractionView]'s ItemSelectionInteraction and
   * returns the binding's root.
   */
  fun provideSelectionInteractionViewInflatedView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): View

  /**
   * Handles binding inflation for [SelectionInteractionView]'s ItemSelectionInteraction and
   * returns the binding's viewModel.
   */
  fun provideSelectionInteractionViewModel(
    view: View,
    viewModel: SelectionInteractionContentViewModel,
    htmlParserFactory: HtmlParser.Factory,
    resourceBucketName: String,
    entityType: String,
    entityId: String
  )

  /**
   * Handles binding inflation for [SelectionInteractionView]'s MultipleChoiceInteraction and
   * returns the binding's view.
   */
  fun provideMultipleChoiceInteractionItemsInflatedView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): View

  /**
   * Handles binding inflation for [SelectionInteractionView]'s MultipleChoiceInteraction and
   * returns the binding's viewModel.
   */
  fun provideMultipleChoiceInteractionItemsViewModel(
    view: View,
    viewModel: SelectionInteractionContentViewModel,
    htmlParserFactory: HtmlParser.Factory,
    resourceBucketName: String,
    entityType: String,
    entityId: String
  )

  /**
   * Handles binding inflation for [DragDropSortInteractionView]'s SortInteraction and returns the
   * binding's view.
   */
  fun provideDragDropSortInteractionInflatedView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): View

  /** Handles setting [DragDropInteractionItemsBinding]. */
  fun setDragDropInteractionItemsBinding(
    view: View
  )

  /** Handles setting [DragDropInteractionItemsBinding]'s adapter. */
  fun setDragDropInteractionItemsBindingAdapter(
    adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
  )

  /** Returns [DragDropInteractionItemsBinding]'s RecyclerView. */
  fun getDragDropInteractionItemsBindingRecyclerView(): RecyclerView

  /** Returns [DragDropInteractionItemsBinding]'s dragDropContentGroupItem. */
  fun getDragDropInteractionItemsBindingGroupItem(): ImageButton

  /** Returns [DragDropInteractionItemsBinding]'s dragDropContentUnlinkItems. */
  fun getDragDropInteractionItemsBindingUnlinkItems(): ImageButton

  /** Returns [DragDropInteractionItemsBinding]'s dragDropAccessibleContainer. */
  fun getDragDropInteractionItemsBindingAccessibleContainer(): LinearLayout

  /** Handles setting [DragDropInteractionItemsBinding]'s viewModel. */
  fun setDragDropInteractionItemsBindingViewModel(
    viewModel: DragDropInteractionContentViewModel
  )

  /**
   * Handles binding inflation for [DragDropSortInteractionView]'s SingleItemInteraction and returns
   * the binding's view.
   */
  fun provideDragDropSingleItemInflatedView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): View

  /** Handles setting [DragDropSingleItemBinding]. */
  fun setDragDropSingleItemBinding(
    view: View
  )

  /** Handles setting [DragDropSingleItemBinding]'s htmlContent. */
  fun setDragDropSingleItemBindingHtmlContent(
    htmlParserFactory: HtmlParser.Factory,
    resourceBucketName: String,
    entityType: String,
    entityId: String,
    viewModel: String
  )

  /** Returns [ClickableAreasImage]'s defaultRegion. */
  fun getDefaultRegion(parentView: FrameLayout): View
}
