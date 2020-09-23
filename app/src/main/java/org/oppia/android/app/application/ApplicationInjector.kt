package org.oppia.android.app.application

import org.oppia.android.app.profile.ProfileInputView

/** Injector for application-level dependencies that can't be directly injected where needed. */
interface ApplicationInjector {

  // TODO(#1619): Remove post-modularization.
  fun inject(profileInputView: ProfileInputView)
}
