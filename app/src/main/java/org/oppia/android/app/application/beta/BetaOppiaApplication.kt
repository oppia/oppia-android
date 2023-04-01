package org.oppia.android.app.application.beta

import org.oppia.android.app.application.AbstractOppiaApplication

/** The root [AbstractOppiaApplication] for beta builds of the Oppia app. */
class BetaOppiaApplication : AbstractOppiaApplication(DaggerBetaApplicationComponent::builder)
