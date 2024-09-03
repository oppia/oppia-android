package org.oppia.android.app.shim

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.app.home.promotedlist.ComingSoonTopicsViewModel
import org.oppia.android.app.home.promotedlist.PromotedStoryViewModel
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.player.state.itemviewmodel.DragDropInteractionContentViewModel
import org.oppia.android.app.player.state.itemviewmodel.SelectionInteractionContentViewModel
import org.oppia.android.app.profile.ProfileItemViewModel
import org.oppia.android.app.survey.surveyitemviewmodel.MultipleChoiceOptionContentViewModel
import org.oppia.android.util.parser.html.HtmlParser

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

  /** Handles setting [DragDropInteractionItemsBinding]'s view model. */
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
  ): View

  /**
   * Handles binding inflation for [org.oppia.android.app.home.promotedlist.PromotedStoryListView]
   * and returns the view model.
   */
  fun providePromotedStoryViewModel(
    view: View,
    viewModel: PromotedStoryViewModel
  )

  /** Handles binding inflation for [ComingSoonTopicsListView]. */
  fun provideComingSoonTopicViewInflatedView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): View

  /** Handles binding inflation for [ComingSoonTopicsListView] and returns the view model. */
  fun provideComingSoonTopicsViewViewModel(
    view: View,
    viewModel: ComingSoonTopicsViewModel
  )

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
   * returns the binding's view model.
   */
  fun provideSelectionInteractionViewModel(
    view: View,
    viewModel: SelectionInteractionContentViewModel,
    htmlParserFactory: HtmlParser.Factory,
    resourceBucketName: String,
    entityType: String,
    entityId: String,
    writtenTranslationContext: WrittenTranslationContext
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
   * returns the binding's view model.
   */
  fun provideMultipleChoiceInteractionItemsViewModel(
    view: View,
    viewModel: SelectionInteractionContentViewModel,
    htmlParserFactory: HtmlParser.Factory,
    resourceBucketName: String,
    entityType: String,
    entityId: String,
    writtenTranslationContext: WrittenTranslationContext
  )

  /**
   * Handles binding inflation for [SurveyMultipleChoiceOptionView]'s MultipleChoiceOption and
   * returns the binding's view.
   */
  fun provideMultipleChoiceItemsInflatedView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): View

  /**
   * Handles binding inflation for [SurveyMultipleChoiceOptionView]'s MultipleChoiceOption and
   * returns the binding's view model.
   */
  fun provideMultipleChoiceOptionViewModel(
    view: View,
    viewModel: MultipleChoiceOptionContentViewModel
  )

  /**
   * Handles binding inflation for [SurveyNpsItemOptionView]'s MultipleChoiceOption and
   * returns the binding's view.
   */
  fun provideNpsItemsInflatedView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): View

  /**
   * Handles binding inflation for [SurveyNpsItemOptionView]'s MultipleChoiceOption and
   * returns the binding's view model.
   */
  fun provideNpsItemsViewModel(
    view: View,
    viewModel: MultipleChoiceOptionContentViewModel
  )

  /** Handles binding inflation for [org.oppia.android.app.profile.ProfileListView]. */
  fun provideProfileItemInflatedView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): View

  /**
   * Handles binding inflation for [org.oppia.android.app.profile.ProfileListView]
   * and returns the view model.
   */
  fun provideProfileItemViewModel(
    view: View,
    viewModel: ProfileItemViewModel
  )
}
