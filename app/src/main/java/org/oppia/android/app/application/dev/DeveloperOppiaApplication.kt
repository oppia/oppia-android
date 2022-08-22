package org.oppia.android.app.application.dev

import org.oppia.android.app.application.AbstractOppiaApplication

/** The root [AbstractOppiaApplication] for developer builds of the Oppia app. */
class DeveloperOppiaApplication : AbstractOppiaApplication(
  DaggerDeveloperApplicationComponent::builder
)
