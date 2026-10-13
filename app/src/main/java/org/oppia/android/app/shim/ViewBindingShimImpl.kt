package org.oppia.android.app.shim

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.app.databinding.R
import org.oppia.android.app.databinding.databinding.ComingSoonTopicViewBinding
import org.oppia.android.app.databinding.databinding.DragDropInteractionItemsBinding
import org.oppia.android.app.databinding.databinding.DragDropSingleItemBinding
import org.oppia.android.app.databinding.databinding.ItemSelectionInteractionItemsBinding
import org.oppia.android.app.databinding.databinding.MultipleChoiceInteractionItemsBinding
import org.oppia.android.app.databinding.databinding.PromotedStoryCardBinding
import org.oppia.android.app.databinding.databinding.SurveyMultipleChoiceItemBinding
import org.oppia.android.app.databinding.databinding.SurveyNpsItemBinding
import org.oppia.android.app.home.promotedlist.ComingSoonTopicsViewModel
import org.oppia.android.app.home.promotedlist.PromotedStoryViewModel
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.player.state.itemviewmodel.DragDropInteractionContentViewModel
import org.oppia.android.app.player.state.itemviewmodel.SelectionInteractionContentViewModel
import org.oppia.android.app.survey.surveyitemviewmodel.MultipleChoiceOptionContentViewModel
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.parser.html.HtmlParser
import javax.inject.Inject

/**
 * Creates bindings for Views in order to avoid View files directly depending on Binding files.
 * When working on a View file, developers should refrain from directly referencing Binding files
 * by adding all related functionality here.
 *
 * Please note that this file is temporary and all functionality will be returned to it's respective
 * ViewModel once Bazel modularization work has completed.
 */
// TODO(#1619): Remove this file.
class ViewBindingShimImpl @Inject constructor(
  private val translationController: TranslationController,
  private val appLanguageResourceHandler: AppLanguageResourceHandler
) : ViewBindingShim {

  override fun providePromotedStoryCardInflatedView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): ViewDataBinding {
    return PromotedStoryCardBinding.inflate(
      LayoutInflater.from(parent.context), parent, attachToParent
    )
  }

  override fun providePromotedStoryViewModel(
    binding: ViewDataBinding,
    viewModel: PromotedStoryViewModel
  ) {
    val promotedBinding = binding as PromotedStoryCardBinding
    promotedBinding.viewModel = viewModel
  }

  override fun provideComingSoonTopicViewInflatedView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): ViewDataBinding {
    return ComingSoonTopicViewBinding.inflate(
      LayoutInflater.from(parent.context), parent, attachToParent
    )
  }

  override fun provideComingSoonTopicsViewViewModel(
    binding: ViewDataBinding,
    viewModel: ComingSoonTopicsViewModel
  ) {
    val comingSoonBinding = binding as ComingSoonTopicViewBinding
    comingSoonBinding.viewModel = viewModel
  }

  override fun provideSelectionInteractionViewInflatedView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): ViewDataBinding {
    return ItemSelectionInteractionItemsBinding.inflate(
      LayoutInflater.from(parent.context),
      parent,
      /* attachToParent= */ false
    )
  }

  override fun provideSelectionInteractionViewModel(
    binding: ViewDataBinding,
    viewModel: SelectionInteractionContentViewModel,
    htmlParserFactory: HtmlParser.Factory,
    resourceBucketName: String,
    entityType: String,
    entityId: String,
    writtenTranslationContext: WrittenTranslationContext
  ) {
    val selectionBinding = binding as ItemSelectionInteractionItemsBinding
    selectionBinding.htmlContent =
      htmlParserFactory.create(
        resourceBucketName,
        entityType,
        entityId,
        false,
        displayLocale = appLanguageResourceHandler.getDisplayLocale()
      ).parseOppiaHtml(
        translationController.extractString(viewModel.htmlContent, writtenTranslationContext),
        selectionBinding.itemSelectionContentsTextView
      )
    selectionBinding.viewModel = viewModel
  }

  override fun provideMultipleChoiceInteractionItemsInflatedView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): ViewDataBinding {
    return MultipleChoiceInteractionItemsBinding.inflate(
      LayoutInflater.from(parent.context),
      parent,
      false
    )
  }

  override fun provideMultipleChoiceInteractionItemsViewModel(
    binding: ViewDataBinding,
    viewModel: SelectionInteractionContentViewModel,
    htmlParserFactory: HtmlParser.Factory,
    resourceBucketName: String,
    entityType: String,
    entityId: String,
    writtenTranslationContext: WrittenTranslationContext
  ) {
    val multipleChoiceBinding = binding as MultipleChoiceInteractionItemsBinding
    multipleChoiceBinding.htmlContent =
      htmlParserFactory.create(
        resourceBucketName, entityType, entityId, /* imageCenterAlign= */ false,
        displayLocale = appLanguageResourceHandler.getDisplayLocale()
      ).parseOppiaHtml(
        translationController.extractString(viewModel.htmlContent, writtenTranslationContext),
        multipleChoiceBinding.multipleChoiceContentTextView
      )
    multipleChoiceBinding.viewModel = viewModel
  }

  override fun provideMultipleChoiceItemsInflatedView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): ViewDataBinding {
    return SurveyMultipleChoiceItemBinding.inflate(
      LayoutInflater.from(parent.context),
      parent,
      false
    )
  }

  override fun provideMultipleChoiceOptionViewModel(
    binding: ViewDataBinding,
    viewModel: MultipleChoiceOptionContentViewModel
  ) {
    val surveyBinding = binding as SurveyMultipleChoiceItemBinding
    surveyBinding.optionContent = viewModel.optionContent
    surveyBinding.viewModel = viewModel
  }

  override fun provideNpsItemsInflatedView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): ViewDataBinding {
    return SurveyNpsItemBinding.inflate(
      LayoutInflater.from(parent.context),
      parent,
      false
    )
  }

  override fun provideNpsItemsViewModel(
    binding: ViewDataBinding,
    viewModel: MultipleChoiceOptionContentViewModel
  ) {
    val npsBinding = binding as SurveyNpsItemBinding
    npsBinding.scoreContent = viewModel.optionContent
    npsBinding.viewModel = viewModel
  }

  override fun provideDragDropSortInteractionInflatedView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): ViewDataBinding {
    return DragDropInteractionItemsBinding.inflate(
      LayoutInflater.from(parent.context), parent, /* attachToParent= */ false
    )
  }

  private lateinit var dragDropInteractionItemsBinding: DragDropInteractionItemsBinding

  override fun setDragDropInteractionItemsBinding(
    binding: ViewDataBinding
  ) {
    dragDropInteractionItemsBinding = binding as DragDropInteractionItemsBinding
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
  ): ViewDataBinding {
    return DragDropSingleItemBinding.inflate(
      LayoutInflater.from(parent.context), parent, /* attachToParent= */ false
    )
  }

  // TODO(#1692): Fix implementation to not use cache binding.
  private lateinit var dragDropSingleItemBinding: DragDropSingleItemBinding

  override fun setDragDropSingleItemBinding(
    binding: ViewDataBinding
  ) {
    dragDropSingleItemBinding = binding as DragDropSingleItemBinding
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
      /* imageCenterAlign= */ false,
      displayLocale = appLanguageResourceHandler.getDisplayLocale()
    ).parseOppiaHtml(
      viewModel, dragDropSingleItemBinding.dragDropContentTextView
    )
  }

  override fun getDefaultRegion(parentView: FrameLayout): View {
    return parentView.findViewById<View>(R.id.default_selected_region)
  }
}
