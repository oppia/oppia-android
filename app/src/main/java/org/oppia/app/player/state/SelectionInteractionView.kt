package org.oppia.app.player.state

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.databinding.ItemSelectionInteractionItemsBinding
import org.oppia.app.databinding.MultipleChoiceInteractionItemsBinding
import org.oppia.app.fragment.InjectableFragment
import org.oppia.app.player.state.itemviewmodel.SelectionInteractionContentViewModel
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.util.gcsresource.DefaultResourceBucketName
import org.oppia.util.parser.ExplorationHtmlParserEntityType
import org.oppia.util.parser.HtmlParser
import javax.inject.Inject

/** Corresponds to the type of input that should be used for an item selection interaction view. */
enum class SelectionItemInputType {
  CHECKBOXES,
  RADIO_BUTTONS
}

/**
 * A custom [RecyclerView] for displaying a variable list of items that may be selected by a user as part of the item
 * selection or multiple choice interactions.
 */
class SelectionInteractionView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {
  // Default to checkboxes to ensure that something can render even if it may not be correct.
  private var selectionItemInputType: SelectionItemInputType = SelectionItemInputType.CHECKBOXES

  @Inject
  lateinit var htmlParserFactory: HtmlParser.Factory

  @Inject
  @field:ExplorationHtmlParserEntityType
  lateinit var entityType: String

  @Inject
  @field:DefaultResourceBucketName
  lateinit var resourceBucketName: String

  private lateinit var entityId: String

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    FragmentManager.findFragment<InjectableFragment>(this).createViewComponent(this).inject(this)
  }

  fun setAllOptionsItemInputType(selectionItemInputType: SelectionItemInputType) {
    // TODO(#299): Find a cleaner way to initialize the item input type. Using data-binding results in a race condition
    //  with setting the adapter data, so this needs to be done in an order-agnostic way. There should be a way to do
    //  this more efficiently and cleanly than always relying on notifying of potential changes in the adapter when the
    //  type is set (plus the type ought to be permanent).
    this.selectionItemInputType = selectionItemInputType
    adapter = createAdapter()
  }

  // TODO(#264): Clean up HTML parser such that it can be handled completely through a binding adapter, allowing
  //  TextViews that require custom Oppia HTML parsing to be fully automatically bound through data-binding.
  fun setEntityId(entityId: String) {
    this.entityId = entityId
  }

  private fun createAdapter(): BindableAdapter<SelectionInteractionContentViewModel> {
    return when (selectionItemInputType) {
      SelectionItemInputType.CHECKBOXES ->
        BindableAdapter.SingleTypeBuilder
          .newBuilder<SelectionInteractionContentViewModel>()
          .registerViewBinder(
            inflateView = { parent ->
              ItemSelectionInteractionItemsBinding.inflate(
                LayoutInflater.from(parent.context), parent, /* attachToParent= */ false
              ).root
            },
            bindView = { view, viewModel ->
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
          )
          .build()
      SelectionItemInputType.RADIO_BUTTONS ->
        BindableAdapter.SingleTypeBuilder
          .newBuilder<SelectionInteractionContentViewModel>()
          .registerViewBinder(
            inflateView = { parent ->
              MultipleChoiceInteractionItemsBinding.inflate(
                LayoutInflater.from(parent.context), parent, /* attachToParent= */ false
              ).root
            },
            bindView = { view, viewModel ->
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
          )
          .build()
    }
  }
}

/** Sets the [SelectionItemInputType] for a specific [SelectionInteractionView] via data-binding. */
@BindingAdapter("allOptionsItemInputType")
fun setAllOptionsItemInputType(
  selectionInteractionView: SelectionInteractionView,
  selectionItemInputType: SelectionItemInputType
) = selectionInteractionView.setAllOptionsItemInputType(selectionItemInputType)

/** Sets the exploration ID for a specific [SelectionInteractionView] via data-binding. */
@BindingAdapter("entityId")
fun setEntityId(
  selectionInteractionView: SelectionInteractionView,
  entityId: String
) = selectionInteractionView.setEntityId(entityId)
