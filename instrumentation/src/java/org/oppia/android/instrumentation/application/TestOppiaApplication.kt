package org.oppia.android.instrumentation.application

import org.oppia.android.app.application.AbstractOppiaApplication

/** The root [Application] of the all end-to-end test apps. */
class TestOppiaApplication : AbstractOppiaApplication(DaggerTestApplicationComponent::builder)
