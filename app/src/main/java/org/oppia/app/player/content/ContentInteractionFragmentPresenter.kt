package org.oppia.app.player.content

import android.content.Context
import android.text.Editable
import android.text.Spanned
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import kotlinx.android.synthetic.main.content_interaction_fragment.view.*
import org.xml.sax.Attributes
import java.util.*

/** Presenter for [ContentInteractionFragment]. */
class ContentInteractionFragmentPresenter @Inject constructor(
  @ApplicationContext private val context: Context,
  private val fragment: Fragment
) {

//  private val interactionId:String,val customizationArgs: Map<String, GaeCustomizationArgs>
  private lateinit var binding:ContentInteractionFragmentBinding


  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {

    binding = ContentInteractionFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)


    fetchDummyExplorations()

    return binding.root
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
              val gaeStateData: GaeState? = gaeStateMap!!.get("Q2 on N and D")

              var gaeCustomizationArgsMap: Map<String, GaeCustomizationArgs>? = null

              gaeCustomizationArgsMap= gaeStateData?.interactionInstance?.customizationArgs

              val gaeCustomizationArgs: Any? = gaeCustomizationArgsMap!!.get("choices")?.value

              Log.d("Tag", "explorationContainer: ******************" + explorationContainer!!.exploration!!.states);
              Log.d("Tag", "explorationContainer: ******************" + gaeCustomizationArgsMap);
              Log.d("Tag", "explorationContainer: ******************" + gaeCustomizationArgs);

              if (gaeStateData?.interactionInstance?.id.equals("MultipleChoiceInput"))
              {

                val gaeCustomArgsInString : String  = gaeCustomizationArgs.toString().replace("[","").replace("[","")

                Log.d("TAG","htmlToString: " + gaeCustomArgsInString)

                var items = gaeCustomArgsInString.split(",").toTypedArray()

                Log.d("TAG","Size: " + items!!.size + items[0])

                addRadioButtons(items)

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
  fun addRadioButtons(optionsArray: Array<String>) {
    for (row in 0..0) {
      val ll = RadioGroup(context)
      ll.orientation = LinearLayout.HORIZONTAL

      for (i in 0..optionsArray.size) {
        val rdbtn = RadioButton(context)
        rdbtn.id = View.generateViewId()
        rdbtn.text = optionsArray[i] + rdbtn.id
        ll.addView(rdbtn)
      }
      binding.root.radioGroup.addView(ll)
    }
  }

  fun convertHtmlToString(htmlContent: Any? ):Spanned{
    var result: Spanned
    result = HtmlParser.buildSpannedText(htmlContent.toString(),
      HtmlParser.TagHandler()
      { b: Boolean, s: String, editable: Editable?, attributes: Attributes? ->
        if (b && s.equals("oppia-noninteractive-image")) {
          var value: String? = HtmlParser.getValue(attributes, "filepath-with-value");

          // unescapeEntities method to remove html quotes
          var strictMode: Boolean = true;
          var unescapedString: String = org.jsoup.parser.Parser.unescapeEntities(value, strictMode);
          Log.d("value", "*****" + value)
          Log.d("unescapedString", "*****" + unescapedString)
        }
        false;

      })
    return result;
  }
}
