package org.oppia.android.app.shim

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.app.home.promotedlist.ComingSoonTopicsViewModel
import org.oppia.android.app.home.promotedlist.PromotedStoryViewModel
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.player.state.itemviewmodel.DragDropInteractionContentViewModel
import org.oppia.android.app.player.state.itemviewmodel.SelectionInteractionContentViewModel
import org.oppia.android.app.survey.surveyitemviewmodel.MultipleChoiceOptionContentViewModel
import org.oppia.android.util.parser.html.HtmlParser

/**
 * Creates bindings for Views in order to avoid View files directly depending on Binding files.
 * When working on a View file, developers should refrain from directly referencing Binding files
 * by adding all related functionality here.
 *
 * Please note that this file is temporary and all functionality will be returned to it's respective
 * ViewModel once Bazel modularization work has completed.
 */
// TODO(#1619): Remove this file.
interface ViewBindingShim {

  /**
   * Handles binding inflation for [DragDropSortInteractionView]'s SortInteraction and returns the
   * binding.
   */
  fun provideDragDropSortInteractionInflatedView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): ViewDataBinding

  /** Handles setting [DragDropInteractionItemsBinding]. */
  fun setDragDropInteractionItemsBinding(
    binding: ViewDataBinding
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

  /** Handles setting [DragDropInteractionItemsBinding]'s view model. */
  fun setDragDropInteractionItemsBindingViewModel(
    viewModel: DragDropInteractionContentViewModel
  )

  /**
   * Handles binding inflation for [DragDropSortInteractionView]'s SingleItemInteraction and returns
   * the binding.
   */
  fun provideDragDropSingleItemInflatedView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): ViewDataBinding

  /** Handles setting [DragDropSingleItemBinding]. */
  fun setDragDropSingleItemBinding(
    binding: ViewDataBinding
  )

  /** Handles setting [DragDropSingleItemBinding]'s html content. */
  fun setDragDropSingleItemBindingHtmlContent(
    htmlParserFactory: HtmlParser.Factory,
    resourceBucketName: String,
    entityType: String,
    entityId: String,
    viewModel: String
  )

  /** Returns [ClickableAreasImage]'s default region. */
  fun getDefaultRegion(parentView: FrameLayout): View

  /**
   * Handles binding inflation for [org.oppia.android.app.home.promotedlist.PromotedStoryListView].
   */
  fun providePromotedStoryCardInflatedView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): ViewDataBinding

  /**
   * Handles binding inflation for [org.oppia.android.app.home.promotedlist.PromotedStoryListView]
   * and sets the view model.
   */
  fun providePromotedStoryViewModel(
    binding: ViewDataBinding,
    viewModel: PromotedStoryViewModel
  )

  /** Handles binding inflation for [ComingSoonTopicsListView]. */
  fun provideComingSoonTopicViewInflatedView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): ViewDataBinding

  /** Handles binding inflation for [ComingSoonTopicsListView] and sets the view model. */
  fun provideComingSoonTopicsViewViewModel(
    binding: ViewDataBinding,
    viewModel: ComingSoonTopicsViewModel
  )

  /**
   * Handles binding inflation for [SelectionInteractionView]'s ItemSelectionInteraction and
   * returns the binding.
   */
  fun provideSelectionInteractionViewInflatedView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): ViewDataBinding

  /**
   * Handles binding for [SelectionInteractionView]'s ItemSelectionInteraction and
   * sets the view model.
   */
  fun provideSelectionInteractionViewModel(
    binding: ViewDataBinding,
    viewModel: SelectionInteractionContentViewModel,
    htmlParserFactory: HtmlParser.Factory,
    resourceBucketName: String,
    entityType: String,
    entityId: String,
    writtenTranslationContext: WrittenTranslationContext
  )

  /**
   * Handles binding inflation for [SelectionInteractionView]'s MultipleChoiceInteraction and
   * returns the binding.
   */
  fun provideMultipleChoiceInteractionItemsInflatedView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): ViewDataBinding

  /**
   * Handles binding for [SelectionInteractionView]'s MultipleChoiceInteraction and
   * sets the view model.
   */
  fun provideMultipleChoiceInteractionItemsViewModel(
    binding: ViewDataBinding,
    viewModel: SelectionInteractionContentViewModel,
    htmlParserFactory: HtmlParser.Factory,
    resourceBucketName: String,
    entityType: String,
    entityId: String,
    writtenTranslationContext: WrittenTranslationContext
  )

  /**
   * Handles binding inflation for [SurveyMultipleChoiceOptionView]'s MultipleChoiceOption and
   * returns the binding.
   */
  fun provideMultipleChoiceItemsInflatedView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): ViewDataBinding

  /**
   * Handles binding for [SurveyMultipleChoiceOptionView]'s MultipleChoiceOption and
   * sets the view model.
   */
  fun provideMultipleChoiceOptionViewModel(
    binding: ViewDataBinding,
    viewModel: MultipleChoiceOptionContentViewModel
  )

  /**
   * Handles binding inflation for [SurveyNpsItemOptionView]'s MultipleChoiceOption and
   * returns the binding.
   */
  fun provideNpsItemsInflatedView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): ViewDataBinding

  /**
   * Handles binding for [SurveyNpsItemOptionView]'s MultipleChoiceOption and
   * sets the view model.
   */
  fun provideNpsItemsViewModel(
    binding: ViewDataBinding,
    viewModel: MultipleChoiceOptionContentViewModel
  )
}
