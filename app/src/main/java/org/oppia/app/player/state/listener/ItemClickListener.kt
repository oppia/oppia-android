package org.oppia.app.player.state.listener

import org.oppia.app.model.InteractionObject

/** This interface helps to get pending answer of MultipleChoice/ItemSelection input interaction. */
interface ItemClickListener{
  fun onItemClick(interactionObject: InteractionObject)
}
