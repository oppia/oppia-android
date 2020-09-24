package org.oppia.app.application

import org.oppia.app.profile.ProfileInputView
import org.oppia.util.data.DataProvidersInjector

/** Injector for application-level dependencies that can't be directly injected where needed. */
interface ApplicationInjector : DataProvidersInjector {

  // TODO(#1619): Remove post-modularization.
  fun inject(profileInputView: ProfileInputView)
}
