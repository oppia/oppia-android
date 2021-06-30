package org.oppia.android.domain.exploration.lightweightcheckpointing

/** Different states in which checkpoint saving exploration progress can exist. */
enum class ExplorationCheckpointState {
  /**
   *  Progress made in the exploration is saved and the size of the checkpoint database has
   * not exceeded limit.
   */
  CHECKPOINT_SAVED_DATABASE_NOT_EXCEEDED_LIMIT,

  /**
   * Progress made in the exploration is saved and the size of the checkpoint database has
   * exceeded limit.
   */
  CHECKPOINT_SAVED_DATABASE_EXCEEDED_LIMIT,

  /** Progress made in the exploration is not saved. */
  UNSAVED
}
