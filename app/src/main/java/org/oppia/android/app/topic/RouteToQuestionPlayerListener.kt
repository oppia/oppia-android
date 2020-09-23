package org.oppia.app.topic

/** Listener for when an activity should route to a Question Player. */
interface RouteToQuestionPlayerListener {
  fun routeToQuestionPlayer(skillIdList: ArrayList<String>)
}
