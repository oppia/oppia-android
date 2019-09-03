package org.oppia.app.splash

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.HomeActivity
import org.oppia.app.R

/** The SplashFragments navigates to Home page once the app is finished loading completely*/

class SplashFragment : Fragment() {

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val view = inflater.inflate(R.layout.splash_fragment, container, false)
    routeToHomePage()
    return view
  }

  private fun routeToHomePage() {
    val intent = Intent(requireContext(), HomeActivity::class.java)
    startActivity(intent)
    activity!!.finish()
  }
}
