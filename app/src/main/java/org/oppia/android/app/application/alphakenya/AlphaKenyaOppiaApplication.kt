package org.oppia.android.app.application.alphakenya

import org.oppia.android.app.application.AbstractOppiaApplication

// TODO(#4419): Remove this application class & broader Kenya-specific alpha package.
/** The root [AbstractOppiaApplication] for the Kenya-specific alpha build of the Oppia app. */
class AlphaKenyaOppiaApplication : AbstractOppiaApplication(
  DaggerAlphaKenyaApplicationComponent::builder
)
