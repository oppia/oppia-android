package org.oppia.android.app.fragment

import javax.inject.Provider

interface FragmentComponentBuilderInjector {
  fun getFragmentComponentBuilderProvider(): Provider<FragmentComponent.Builder>
}
