package org.oppia.android.app.application.ga

import org.oppia.android.app.application.AbstractOppiaApplication

/** The root [AbstractOppiaApplication] for general availability builds of the Oppia app. */
class GaOppiaApplication : AbstractOppiaApplication(DaggerGaApplicationComponent::builder)
