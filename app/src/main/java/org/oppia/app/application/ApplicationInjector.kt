package org.oppia.app.application

import org.oppia.app.profile.ProfileInputView

/**
 * Alternative to [ViewComponent] for [ProfileInputView]
 */
interface ApplicationInjector {

  // TODO(#1619): Remove post-modularization.
  fun inject(profileInputView: ProfileInputView)
}
