package org.oppia.app.profile

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.profile_input_view.view.*
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.databinding.AdminPinActivityBinding
import org.oppia.app.model.ProfileId
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.profile.ProfileManagementController
import java.util.prefs.PreferenceChangeEvent
import javax.inject.Inject

/** The presenter for [AdminPinActivity]. */
@ActivityScope
class AdminPinActivityPresenter @Inject constructor(
  private val context: Context,
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
  private val viewModelProvider: ViewModelProvider<AdminPinViewModel>
) {
  private val adminViewModel by lazy {
    getAdminPinViewModel()
  }

  private var input_Pin: String = ""
  private var input_Confirm_Pin: String = ""
  private lateinit var sharedPreferences: SharedPreferences
  //private var mCount:Int=0


  /** Binds ViewModel and sets up text and button listeners. */
  fun handleOnCreate(savedInstanceState: Bundle?,sharedPreferences: SharedPreferences) {
    activity.title = activity.getString(R.string.add_profile_title)
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp)

    val binding =
      DataBindingUtil.setContentView<AdminPinActivityBinding>(activity, R.layout.admin_pin_activity)

    /*binding.apply {
      lifecycleOwner = activity
      viewModel = adminViewModel
    }*/
    this.sharedPreferences=sharedPreferences


    /*binding.inputPin.input.setText(input_Pin)
    Log.i("binding input pin",input_Pin)
    binding.inputConfirmPin.input.setText(input_Confirm_Pin)*/

    //binding.inputPin.input.setText("LOL")
    /*mCount = savedInstanceState?.getInt("mCount", 0) ?: 0
    if (mCount % 2 === 1) {
      // 1st, 3rd, 5th, etc. rotations. Explicitly execute the bindings and let the framework
      // restore from the saved instance state.
      binding.executePendingBindings()
    } else {
      // First creation and 2nd, 4th, etc. rotations. Set up our model and let the
      // framework restore from the saved instance state then overwrite with the bindings.
      // (Or maybe it just ignores the saved instance state and restores the bindnings.)
      binding.apply {
        lifecycleOwner = activity
        viewModel = adminViewModel
      }
    }
    mCount++*/

    /*if (savedInstanceState != null) {

      if (savedInstanceState.getString("InputPin") != null) {
        Log.i("Blah",savedInstanceState.getString("InputPin"))
        binding.inputPin.input.setText(savedInstanceState.getString("InputPin"))

      }
      if (savedInstanceState.getString("InputConfirmPin") != null) {
        binding.inputConfirmPin.input.setText(input_Confirm_Pin)
      }
      binding.executePendingBindings()
    }*/

    addTextChangedListener(binding.inputPin) { pin ->
      pin?.let {
        adminViewModel.pinErrorMsg.set("")
      }
    }

    addTextChangedListener(binding.inputConfirmPin) { confirmPin ->
      confirmPin?.let {
        adminViewModel.confirmPinErrorMsg.set("")
      }
    }

    binding.inputPin.addTextChangedListener(object :TextWatcher{
      override fun afterTextChanged(p0: Editable?) {
        input_Pin=p0.toString().trim()
        sharedPreferences.edit().putString("InputPin", p0.toString().trim()).apply();
        Log.i("input pin I" ,input_Pin)
        Log.i("input confirm pin I",input_Confirm_Pin)
      }

      override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
      }

      override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

      }
    })

    binding.inputConfirmPin.addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(p0: Editable?) {
        input_Confirm_Pin=p0.toString().trim()
        sharedPreferences.edit().putString("InputConfirmPin", p0.toString()).apply();
        Log.i("input pin C",input_Pin)
        Log.i("input confirm pin C",input_Confirm_Pin)
        binding.submitButton.isEnabled =
          (p0.toString().trim().length == 5 && binding.inputPin.getInput().length == 5)
      }

      override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
      }

      override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

      }
    })

    binding.submitButton.setOnClickListener {
      val inputPin = binding.inputPin.getInput()
      val confirmPin = binding.inputConfirmPin.getInput()
      var failed = false
      if (inputPin.length < 5) {
        adminViewModel.pinErrorMsg.set(activity.getString(R.string.admin_pin_error_pin_length))
        failed = true
      }
      if (inputPin != confirmPin) {
        adminViewModel.confirmPinErrorMsg.set(activity.getString(R.string.admin_pin_error_pin_confirm_wrong))
        failed = true
      }
      if (failed) {
        return@setOnClickListener
      }
      val profileId =
        ProfileId.newBuilder()
          .setInternalId(activity.intent.getIntExtra(KEY_ADMIN_PIN_PROFILE_ID, -1))
          .build()

      profileManagementController.updatePin(profileId, inputPin).observe(activity, Observer {
        if (it.isSuccess()) {
          activity.startActivity(
            AddProfileActivity.createAddProfileActivityIntent(
              context, activity.intent.getIntExtra(KEY_ADMIN_PIN_COLOR_RGB, -10710042)
            )
          )
        }
      })
    }


  }

  fun handleOnSavedInstanceState(bundle: Bundle) {
    Log.i("LOL","LOL")
    /*val binding =
      DataBindingUtil.setContentView<AdminPinActivityBinding>(activity, R.layout.admin_pin_activity)
    binding.apply {
      lifecycleOwner = activity
      viewModel = adminViewModel
    }*/
    bundle.putString("InputPin", input_Pin)
    Log.i("binding",input_Pin)
    bundle.putString("InputConfirmPin", input_Confirm_Pin)
    /*input_Pin = binding.inputPin.getInput()
    input_Confirm_Pin = binding.inputConfirmPin.getInput()*/

  }

  fun handleOnDestroy(bundle: Bundle) {
    /*val binding =
      DataBindingUtil.setContentView<AdminPinActivityBinding>(activity, R.layout.admin_pin_activity)
    binding.viewModel=adminViewModel
    input_Pin = binding.inputPin.getInput()
    input_Confirm_Pin = binding.inputConfirmPin.getInput()*/
    bundle.putString("InputPin", input_Pin)
    Log.i("binding",input_Pin)
    bundle.putString("InputConfirmPin", input_Confirm_Pin)
  }

  fun handleOnRestoreInstanceState(bundle: Bundle?) {
    val binding =
      DataBindingUtil.setContentView<AdminPinActivityBinding>(activity, R.layout.admin_pin_activity)
    binding.executePendingBindings()

    binding.apply {
      lifecycleOwner = activity
      viewModel = adminViewModel
    }
    /*input_Pin=bundle?.getString("InputPin")?:""
    input_Confirm_Pin=bundle?.getString("InputConfirmPin")?:""*/
    input_Pin=sharedPreferences.getString("InputPin","LOL")
    input_Confirm_Pin=sharedPreferences.getString("InputConfirmPin","LOL")

    binding.inputPin.input.setText(sharedPreferences.getString("InputPin","LOL"))
    binding.inputConfirmPin.input.setText(input_Confirm_Pin)

    //binding.inputPin.input.setText(bundle?.getString("InputPin"))
    //binding.inputConfirmPin.input.setText(bundle?.getString("InputConfirmPin"))
    /*if (bundle != null) {

      if (bundle.getString("InputPin") != null) {
        //input_Pin=bundle.getString("InputPin")
        binding.inputPin.input.setText(bundle.getString("InputPin"))
      }
      if (bundle.getString("InputConfirmPin") != null) {
        binding.inputConfirmPin.input.setText(bundle.getString("InputConfirmPin"))
        //input_Confirm_Pin=bundle.getString("InputConfirmPin")
      }
    }*/
  }

  private fun addTextChangedListener(
    profileInputView: ProfileInputView,
    onTextChanged: (CharSequence?) -> Unit
  ) {
    profileInputView.addTextChangedListener(object : TextWatcher {
      override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        onTextChanged(p0)
      }

      override fun afterTextChanged(p0: Editable?) {}
      override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    })
  }

  private fun getAdminPinViewModel(): AdminPinViewModel {
    return viewModelProvider.getForActivity(activity, AdminPinViewModel::class.java)
  }
}
