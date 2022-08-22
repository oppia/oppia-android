package org.oppia.android.app.application.alpha

import org.oppia.android.app.application.AbstractOppiaApplication

/** The root [AbstractOppiaApplication] for alpha builds of the Oppia app. */
class AlphaOppiaApplication : AbstractOppiaApplication(DaggerAlphaApplicationComponent::builder)
