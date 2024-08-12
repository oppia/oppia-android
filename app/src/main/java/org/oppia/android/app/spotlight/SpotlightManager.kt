package org.oppia.android.app.spotlight

/** Manager for showing spotlights. */
interface SpotlightManager {
  /**
   * Requests a spotlight to be shown on the [SpotlightTarget]. The spotlight is enqueued if it
   * hasn't been shown before in a FIFO buffer. This API can ensure proper spotlighting of a
   * [SpotlightTarget] which is laid out after an animation, or is a part of a recycler view which
   * gets laid out after a data provider call.
   *
   * @param spotlightTarget The [SpotlightTarget] for which the spotlight is requested
   */
  fun requestSpotlightViewWithDelayedLayout(spotlightTarget: SpotlightTarget)

  /**
   * Requests a spotlight to be shown on the [SpotlightTarget]. The spotlight is enqueued if it
   * hasn't been shown before in a FIFO buffer. This API can ensure proper spotlighting of a
   * [SpotlightTarget] when it is laid out late due to a data provider call. In these cases, use
   * [requestSpotlightViewWithDelayedLayout], instead.  It cannot ensure the  same if the view has
   * to be spotlit immediately after an animation. It also cannot spotlight targets which are a part
   * of a recycler view which are laid out after a data provider call. If TalkBack is turned on, no
   * spotlight shall be shown.
   *
   * @param spotlightTarget The [SpotlightTarget] for which the spotlight is requested
   */
  fun requestSpotlight(spotlightTarget: SpotlightTarget)

  companion object {
    /**
     * The tag that should be associated with fragment implementations of [SpotlightManager] so that
     * it can be used for later retrieval.
     */
    const val SPOTLIGHT_FRAGMENT_TAG = "SpotlightFragment"
  }
}
