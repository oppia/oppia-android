package org.oppia.app.splash


import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction

import android.os.Bundle
import com.example.myapplication.SplashFragment
import org.oppia.app.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val fragment = SplashFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()

    }
}
