package org.oppia.util.logging

import android.os.Bundle
import org.oppia.app.model.EventLog

/**
 * Utility for creating bundles from [EventLog] objects.
 * Note that this utility may later upload them to remote services.
 */
interface EventBundleCreator {

  /** Creates a bundle from event having exploration context. */
  fun createExplorationContextBundle(eventLog: EventLog): Bundle

  /** Creates a bundle from event having question context. */
  fun createQuestionContextBundle(eventLog: EventLog): Bundle

  /** Creates a bundle from event having no context. */
  fun defaultBundle(eventLog: EventLog): Bundle
}