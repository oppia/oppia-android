package org.oppia.app.profile

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory

class CustomFragmentFactory() : FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        if (className == ProfileChooserFragment::class.java.name) {
            return ProfileChooserFragment()
        }
        return super.instantiate(classLoader, className)
    }
}