package org.oppia.android.app.player.state

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.databinding.ObservableList
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.player.state.itemviewmodel.SelectionInteractionContentViewModel
import org.oppia.android.app.player.state.itemviewmodel.SelectionItemInputType
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.shim.ViewBindingShim
import org.oppia.android.app.view.ViewComponentFactory
import org.oppia.android.app.view.ViewComponentImpl
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.parser.html.ExplorationHtmlParserEntityType
import org.oppia.android.util.parser.html.HtmlParser
import javax.inject.Inject

/**
 * A custom [RecyclerView] for displaying a variable list of items that may be selected by a user as
 * part of the item selection or multiple choice interactions.
 */
class SelectionInteractionView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {
  @field:[Inject ExplorationHtmlParserEntityType] lateinit var entityType: String
  @field:[Inject DefaultResourceBucketName] lateinit var resourceBucketName: String

  @Inject lateinit var htmlParserFactory: HtmlParser.Factory
  @Inject lateinit var bindingInterface: ViewBindingShim
  @Inject lateinit var singleTypeBuilderFactory: BindableAdapter.SingleTypeBuilder.Factory

  private lateinit var selectionItemInputType: SelectionItemInputType
  private lateinit var entityId: String
  private lateinit var writtenTranslationContext: WrittenTranslationContext
  private lateinit var dataList: ObservableList<SelectionInteractionContentViewModel>

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    val viewComponentFactory = FragmentManager.findFragment<Fragment>(this) as ViewComponentFactory
    val viewComponent = viewComponentFactory.createViewComponent(this) as ViewComponentImpl
    viewComponent.inject(this)
    maybeInitializeAdapter()
  }

  fun setAllOptionsItemInputType(selectionItemInputType: SelectionItemInputType) {
    this.selectionItemInputType = selectionItemInputType
    maybeInitializeAdapter()
  }

  // TODO(#264): Clean up HTML parser such that it can be handled completely through a binding
  //  adapter, allowing TextViews that require custom Oppia HTML parsing to be fully automatically
  //  bound through data-binding.
  fun setEntityId(entityId: String) {
    this.entityId = entityId
    maybeInitializeAdapter()
  }

  /**
   * Sets the [WrittenTranslationContext] used to translate strings in this view.
   *
   * This must be called during view initialization.
   */
  fun setWrittenTranslationContext(writtenTranslationContext: WrittenTranslationContext) {
    this.writtenTranslationContext = writtenTranslationContext
    maybeInitializeAdapter()
  }

  /**
   * Sets the view's RecyclerView [SelectionInteractionContentViewModel] data list.
   *
   * Note that this needs to be used instead of the generic RecyclerView 'data' binding adapter
   * since this one takes into account initialization order with other binding properties.
   */
  fun setSelectionData(dataList: ObservableList<SelectionInteractionContentViewModel>) {
    this.dataList = dataList
    maybeInitializeAdapter()
  }

  private fun maybeInitializeAdapter() {
    if (::singleTypeBuilderFactory.isInitialized &&
      ::selectionItemInputType.isInitialized &&
      ::entityId.isInitialized &&
      ::writtenTranslationContext.isInitialized &&
      ::dataList.isInitialized
    ) {
      adapter = createAdapter().also { it.setData(dataList) }
    }
  }

  private fun createAdapter(): BindableAdapter<SelectionInteractionContentViewModel> {
    return when (selectionItemInputType) {
      SelectionItemInputType.CHECKBOXES ->
        singleTypeBuilderFactory.create<SelectionInteractionContentViewModel>()
          .registerViewBinder(
            inflateView = { parent ->
              bindingInterface.provideSelectionInteractionViewInflatedView(
                LayoutInflater.from(parent.context),
                parent,
                /* attachToParent= */ false
              )
            },
            bindView = { view, viewModel ->
              bindingInterface.provideSelectionInteractionViewModel(
                view,
                viewModel,
                htmlParserFactory,
                resourceBucketName,
                entityType,
                entityId,
                writtenTranslationContext
              )
            }
          )
          .build()
      SelectionItemInputType.RADIO_BUTTONS ->
        singleTypeBuilderFactory.create<SelectionInteractionContentViewModel>()
          .registerViewBinder(
            inflateView = { parent ->
              bindingInterface.provideMultipleChoiceInteractionItemsInflatedView(
                LayoutInflater.from(parent.context),
                parent,
                /* attachToParent= */ false
              )
            },
            bindView = { view, viewModel ->
              bindingInterface.provideMultipleChoiceInteractionItemsViewModel(
                view,
                viewModel,
                htmlParserFactory,
                resourceBucketName,
                entityType,
                entityId,
                writtenTranslationContext
              )
            }
          )
          .build()
    }
  }
}
