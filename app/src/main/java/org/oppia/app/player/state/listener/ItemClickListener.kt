package org.oppia.app.player.state.listener

import org.oppia.app.model.InteractionObject

interface ItemClickListener{
  fun onItemClick(interactionObject: InteractionObject)
}
