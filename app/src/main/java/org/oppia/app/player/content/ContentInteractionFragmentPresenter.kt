package org.oppia.app.player.content

import android.content.Context
import android.text.Html
import android.text.Spannable
import android.text.Spanned
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import org.oppia.app.application.ApplicationContext
import org.oppia.data.backends.gae.model.GaeCustomizationArgs
import org.oppia.app.databinding.ContentInteractionFragmentBinding
import org.oppia.data.backends.gae.NetworkModule
import org.oppia.data.backends.gae.model.GaeExplorationContainer
import org.oppia.data.backends.gae.model.GaeState
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import android.widget.RadioButton
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.content_interaction_fragment.view.*
import org.oppia.util.data.UrlImageParser

/** Presenter for [ContentInteractionFragment]. */
class ContentInteractionFragmentPresenter @Inject constructor(
  @ApplicationContext private val context: Context,
  private val fragment: Fragment,
  private val interactionInstanceId: String, val gaeCustomizationArgs: Map<String, GaeCustomizationArgs>
) {

  private lateinit var binding: ContentInteractionFragmentBinding

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {

    binding = ContentInteractionFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)

    // TODO replace dummy exploration with below function once Exploration interface is available
    fetchDummyExplorations()

//    showInputInteractions()
    
    return binding.root
  }

  private fun showInputInteractions() {
    if (interactionInstanceId.equals("MultipleChoiceInput")) {
      val gaeCustomArgsInString: String = gaeCustomizationArgs.toString().replace("[", "").replace("]", "")
      var items = gaeCustomArgsInString.split(",").toTypedArray()
      addRadioButtons(items)

    } else if (interactionInstanceId.equals("ItemSelectionInput") || interactionInstanceId.equals("SingleChoiceInput")) {
      val gaeCustomArgsInString: String = gaeCustomizationArgs.toString().replace("[", "").replace("]", "")
      var items = gaeCustomArgsInString.split(",").toTypedArray()
      addCheckbox(items)
    } else {
      //Do no show any view
    }
  }

  private fun fetchDummyExplorations() {

    val retrofitInstance = NetworkModule().provideRetrofitInstance()
    val appStoreApiService = NetworkModule().provideExplorationService(retrofitInstance);
    val getStoresResponseCall = appStoreApiService.getExplorationById()

    getStoresResponseCall.enqueue(object : Callback<GaeExplorationContainer> {
      override fun onResponse(call: Call<GaeExplorationContainer>, response: Response<GaeExplorationContainer>) {

        try {

          val code = response.code()
          when (code) {
            200, 201 -> {
              val explorationContainer: GaeExplorationContainer? = response.body()
              var gaeStateMap: Map<String, GaeState>? = null
              gaeStateMap = explorationContainer!!.exploration!!.states
//              val gaeStateData: GaeState? = gaeStateMap!!.get("Q2 on N and D")
              val gaeStateData: GaeState? = gaeStateMap!!.get("Practice 2")

              var gaeCustomizationArgsMap: Map<String, GaeCustomizationArgs>? = null

              gaeCustomizationArgsMap = gaeStateData?.interactionInstance?.customizationArgs

              val gaeCustomizationArgs: Any? = gaeCustomizationArgsMap!!.get("choices")?.value

              Log.d("Tag", "explorationContainer: ******************" + explorationContainer!!.exploration!!.states);
              Log.d("Tag", "explorationContainer: ******************" + gaeCustomizationArgsMap);
              Log.d("Tag", "explorationContainer: ******************" + gaeCustomizationArgs);

              if (gaeStateData?.interactionInstance?.id.equals("MultipleChoiceInput")) {
                val gaeCustomArgsInString: String = gaeCustomizationArgs.toString().replace("[", "").replace("]", "")
                var items = gaeCustomArgsInString.split(",").toTypedArray()
                addRadioButtons(items)

              } else if (gaeStateData?.interactionInstance?.id.equals("ItemSelectionInput") || gaeStateData?.interactionInstance?.id.equals(
                  "SingleChoiceInput"
                )
              ) {
                val gaeCustomArgsInString: String = gaeCustomizationArgs.toString().replace("[", "").replace("]", "")
                var items = gaeCustomArgsInString.split(",").toTypedArray()
                addCheckbox(items)
              } else {
                //Do no show any view
              }
            }
          }
        } catch (e: Exception) {
          Log.d("Tag", "Failure ****************" + e.printStackTrace())
        }
      }

      override fun onFailure(call: Call<GaeExplorationContainer>, t: Throwable) {
        Log.d("Tag", "Failure ****************" + t.message)

      }
    })

  }

  fun addCheckbox(optionsArray: Array<String>) {
    for (row in 0..0) {
      val ll = LinearLayout(context)
      ll.orientation = LinearLayout.VERTICAL

      for (i in 0..optionsArray.size - 1) {
        val cb = CheckBox(context)
        cb.id = View.generateViewId()
        cb.text = convertHtmlToString(optionsArray[i], cb).toString()
        ll.addView(cb)
      }
      binding.root.radioGroup.addView(ll)
    }
  }

  fun addRadioButtons(optionsArray: Array<String>) {
    for (row in 0..0) {
      val ll = RadioGroup(context)
      ll.orientation = LinearLayout.VERTICAL

      for (i in 0..optionsArray.size - 1) {
        val rdbtn = RadioButton(context)
        rdbtn.id = View.generateViewId()
        rdbtn.text = convertHtmlToString(optionsArray[i], rdbtn).toString()// + rdbtn.id
        rdbtn.gravity = (Gravity.RIGHT); (Gravity.CENTER)
        ll.addView(rdbtn)
      }
      binding.root.radioGroup.addView(ll)
    }
  }

  fun convertHtmlToString(rawResponse: Any?, rdbtn: View): Spanned {

    var htmlContent = rawResponse as String;
    var result: Spanned
    var CUSTOM_TAG = "oppia-noninteractive-image"
    var HTML_TAG = "img"
    var CUSTOM_ATTRIBUTE = "filepath-with-value"
    var HTML_ATTRIBUTE = "src"

    if (htmlContent!!.contains(CUSTOM_TAG)) {

      htmlContent = htmlContent.replace(CUSTOM_TAG, HTML_TAG, false);
      htmlContent = htmlContent.replace(CUSTOM_ATTRIBUTE, HTML_ATTRIBUTE, false);
      htmlContent = htmlContent.replace("&amp;quot;", "")
    }

    var imageGetter = UrlImageParser(rdbtn as TextView, context)
    val html: Spannable
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
      html = Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_LEGACY, imageGetter, null) as Spannable
    } else {
      html = Html.fromHtml(htmlContent, imageGetter, null) as Spannable
    }

    result = html
    return result;
  }
}
