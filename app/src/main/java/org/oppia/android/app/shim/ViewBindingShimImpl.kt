package org.oppia.android.app.shim

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.R
import org.oppia.android.app.home.promotedlist.ComingSoonTopicsViewModel
import org.oppia.android.app.home.promotedlist.PromotedStoryViewModel
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.player.state.itemviewmodel.DragDropInteractionContentViewModel
import org.oppia.android.app.player.state.itemviewmodel.SelectionInteractionContentViewModel
import org.oppia.android.app.profile.ProfileItemViewModel
import org.oppia.android.app.survey.surveyitemviewmodel.MultipleChoiceOptionContentViewModel
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.ComingSoonTopicViewBinding
import org.oppia.android.databinding.DragDropInteractionItemsBinding
import org.oppia.android.databinding.DragDropSingleItemBinding
import org.oppia.android.databinding.ItemSelectionInteractionItemsBinding
import org.oppia.android.databinding.MultipleChoiceInteractionItemsBinding
import org.oppia.android.databinding.ProfileItemBinding
import org.oppia.android.databinding.PromotedStoryCardBinding
import org.oppia.android.databinding.SurveyMultipleChoiceItemBinding
import org.oppia.android.databinding.SurveyNpsItemBinding
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.parser.html.HtmlParser
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
class ViewBindingShimImpl @Inject constructor(
  private val translationController: TranslationController,
  private val appLanguageResourceHandler: AppLanguageResourceHandler
) : ViewBindingShim {

  override fun providePromotedStoryCardInflatedView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): View {
    return PromotedStoryCardBinding.inflate(
      LayoutInflater.from(parent.context), parent, attachToParent
    ).root
  }

  override fun providePromotedStoryViewModel(
    view: View,
    viewModel: PromotedStoryViewModel
  ) {
    val binding =
      DataBindingUtil.findBinding<PromotedStoryCardBinding>(view)!!
    binding.viewModel = viewModel
  }

  override fun provideComingSoonTopicViewInflatedView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): View {
    return ComingSoonTopicViewBinding.inflate(
      LayoutInflater.from(parent.context), parent, attachToParent
    ).root
  }

  override fun provideComingSoonTopicsViewViewModel(
    view: View,
    viewModel: ComingSoonTopicsViewModel
  ) {
    val binding =
      DataBindingUtil.findBinding<ComingSoonTopicViewBinding>(view)!!
    binding.viewModel = viewModel
  }

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

  override fun provideSelectionInteractionViewModel(
    view: View,
    viewModel: SelectionInteractionContentViewModel,
    htmlParserFactory: HtmlParser.Factory,
    resourceBucketName: String,
    entityType: String,
    entityId: String,
    writtenTranslationContext: WrittenTranslationContext
  ) {
    val binding =
      DataBindingUtil.findBinding<ItemSelectionInteractionItemsBinding>(view)!!
    binding.htmlContent =
      htmlParserFactory.create(
        resourceBucketName,
        entityType,
        entityId,
        false,
        displayLocale = appLanguageResourceHandler.getDisplayLocale()
      ).parseOppiaHtml(
        translationController.extractString(viewModel.htmlContent, writtenTranslationContext),
        binding.itemSelectionContentsTextView
      )
    binding.viewModel = viewModel
  }

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

  override fun provideMultipleChoiceInteractionItemsViewModel(
    view: View,
    viewModel: SelectionInteractionContentViewModel,
    htmlParserFactory: HtmlParser.Factory,
    resourceBucketName: String,
    entityType: String,
    entityId: String,
    writtenTranslationContext: WrittenTranslationContext
  ) {
    val binding =
      DataBindingUtil.findBinding<MultipleChoiceInteractionItemsBinding>(view)!!
    binding.htmlContent =
      htmlParserFactory.create(
        resourceBucketName, entityType, entityId, /* imageCenterAlign= */ false,
        displayLocale = appLanguageResourceHandler.getDisplayLocale()
      ).parseOppiaHtml(
        translationController.extractString(viewModel.htmlContent, writtenTranslationContext),
        binding.multipleChoiceContentTextView
      )
    binding.viewModel = viewModel
  }

  override fun provideMultipleChoiceItemsInflatedView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): View {
    return SurveyMultipleChoiceItemBinding.inflate(
      LayoutInflater.from(parent.context),
      parent,
      false
    ).root
  }

  override fun provideMultipleChoiceOptionViewModel(
    view: View,
    viewModel: MultipleChoiceOptionContentViewModel
  ) {
    val binding =
      DataBindingUtil.findBinding<SurveyMultipleChoiceItemBinding>(view)!!
    binding.optionContent = viewModel.optionContent
    binding.viewModel = viewModel
  }

  override fun provideNpsItemsInflatedView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): View {
    return SurveyNpsItemBinding.inflate(
      LayoutInflater.from(parent.context),
      parent,
      false
    ).root
  }

  override fun provideNpsItemsViewModel(
    view: View,
    viewModel: MultipleChoiceOptionContentViewModel
  ) {
    val binding =
      DataBindingUtil.findBinding<SurveyNpsItemBinding>(view)!!
    binding.scoreContent = viewModel.optionContent
    binding.viewModel = viewModel
  }

  override fun provideProfileItemInflatedView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean
  ): View {
    return ProfileItemBinding.inflate(
      LayoutInflater.from(parent.context),
      parent,
      false
    ).root
  }

  override fun provideProfileItemViewModel(view: View, viewModel: ProfileItemViewModel) {
    val binding =
      DataBindingUtil.findBinding<ProfileItemBinding>(view)!!
    binding.viewModel = viewModel
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
      LayoutInflater.from(parent.context), parent, /* attachToParent= */ false
    ).root
  }

  // TODO(#1692): Fix implementation to not use cache binding.
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
