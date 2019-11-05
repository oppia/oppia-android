package org.oppia.app.player.state.customview

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.app.model.InteractionObject
import org.oppia.app.player.state.InteractionAdapter
import org.oppia.app.player.state.listener.InteractionAnswerRetriever
import org.oppia.app.player.state.listener.ItemClickListener

internal class SelectionInputInteractionView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defaultStyle: Int = 0
) : FrameLayout(context, attrs, defaultStyle), ItemClickListener, InteractionAnswerRetriever {
  private var interactionObjectBuilder: InteractionObject = InteractionObject.newBuilder().build()

  override fun onItemClick(interactionObject: InteractionObject) {
    interactionObjectBuilder = interactionObject
  }

  private val recyclerView: RecyclerView = RecyclerView(context, attrs, defaultStyle)

  init {
    recyclerView.id = R.id.selection_interaction_recyclerview
    val params = LayoutParams(
      LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT
    )
    this.addView(recyclerView, params)
    isClickable = true
    isFocusable = true
  }

  internal fun setAdapter(adapter: InteractionAdapter) {
    recyclerView.adapter = adapter
  }

  override fun getPendingAnswer(): InteractionObject {
    return interactionObjectBuilder
  }
}
