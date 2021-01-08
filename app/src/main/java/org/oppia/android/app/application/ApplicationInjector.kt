package org.oppia.android.app.application

import org.oppia.android.app.home.promotedlist.ComingSoonTopicsListView
import org.oppia.android.app.home.promotedlist.PromotedStoryListView
import org.oppia.android.app.profile.ProfileInputView
import org.oppia.android.util.data.DataProvidersInjector

/** Injector for application-level dependencies that can't be directly injected where needed. */
interface ApplicationInjector : DataProvidersInjector {

  // TODO(#1619): Remove post-modularization.
  fun inject(profileInputView: ProfileInputView)

  fun injectPromotedStoryListView(promotedStoryListView: PromotedStoryListView)

  fun injectComingSoonTopicsListView(comingSoonTopicsListView: ComingSoonTopicsListView)
}
