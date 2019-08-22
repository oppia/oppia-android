package org.oppia.app.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.HomeActivity
import org.oppia.app.R


class SplashFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_splash, container, false)

//        loadSplashScreen()
        routeToHomePage();
        return view
    }

    private fun loadSplashScreen() {

        // TODO(Veena): Replace below code to load profile data and then redirect to HomePage

        Handler().postDelayed(// Using handler with postDelayed called runnable run method

        {
            routeToHomePage()
        }, (3 * 1000).toLong()) // wait for 5 seconds

    }


    private fun routeToHomePage() {
        val intent = Intent(requireContext(), HomeActivity::class.java)

        startActivity(intent)

        // close this activity

        activity!!.finish()
        activity!!.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

    }


}



