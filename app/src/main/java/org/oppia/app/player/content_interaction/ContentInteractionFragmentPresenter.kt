package org.oppia.app.player.content_interaction

import android.content.Context
import android.text.Html
import android.text.Spannable
import android.text.Spanned
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import org.oppia.app.application.ApplicationContext
import org.oppia.data.backends.gae.model.GaeCustomizationArgs
import org.oppia.app.databinding.ContentInteractionFragmentBinding
import javax.inject.Inject
import android.widget.RadioButton
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.content_interaction_fragment.view.*
import org.oppia.util.data.UrlImageParser
import android.widget.CompoundButton

/** Presenter for [ContentInteractionFragment]. */
class ContentInteractionFragmentPresenter @Inject constructor(
  @ApplicationContext private val context: Context,
  private val fragment: Fragment
//  private val interactionInstanceId: String, val gaeCustomizationArgs: Map<String, GaeCustomizationArgs>
) {

  private lateinit var binding: ContentInteractionFragmentBinding

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {

    binding = ContentInteractionFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)

    showInputInteractions()

    return binding.root
  }

  private fun showInputInteractions() {

    //Todo(Veena): Remove static initialization and use values from constructor for gaeCustomizationArgsMap and interactionInstanceId
    var gaeCustomizationArgsMap = HashMap<String, GaeCustomizationArgs>()
    var interactionInstanceId = "MultipleChoiceInput"
    var sampleData: GaeCustomizationArgs =
      GaeCustomizationArgs(true, "<p>The numerator.</p>, <p>The denominator.</p>, <p>I can't remember!</p>]")
    gaeCustomizationArgsMap?.put("choices", sampleData)
    interactionInstanceId = "MultipleChoiceInput"

    // Todo(veena): Keep the below code for actual implementation
    val gaeCustomizationArgs: Any? = gaeCustomizationArgsMap!!.get("choices")?.value

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

  fun addCheckbox(optionsArray: Array<String>) {
    for (row in 0..0) {
      val ll = LinearLayout(context)
      ll.orientation = LinearLayout.VERTICAL

      for (i in 0..optionsArray.size - 1) {
        val cb = CheckBox(context)
        cb.id = View.generateViewId()
        cb.text = convertHtmlToString(optionsArray[i], cb).toString()

        cb.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
          val msg = "You have " + (if (isChecked) "checked" else "unchecked") + " this Check it Checkbox."
          Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        })
        
        ll.addView(cb)
      }
      binding.root.radioGroup.addView(ll)
    }
  }

  fun addRadioButtons(optionsArray: Array<String>) {
    for (row in 0..0) {
      val rg = RadioGroup(context)
      rg.orientation = LinearLayout.VERTICAL

      for (i in 0..optionsArray.size - 1) {
        val rdbtn = RadioButton(context)
        rdbtn.id = View.generateViewId()
        rdbtn.text = convertHtmlToString(optionsArray[i], rdbtn).toString()// + rdbtn.id
        rdbtn.gravity = (Gravity.RIGHT); (Gravity.CENTER)

        rg.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {
          override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
            for (i in 0 until group.childCount) {
              val btn = group.getChildAt(i) as RadioButton
              if (btn.id == checkedId) {
                val text = btn.text
                Toast.makeText(context, "" + text, Toast.LENGTH_LONG).show()
                return
              }
            }

          }
        })
        rg.addView(rdbtn)
      }
      binding.root.radioGroup.addView(rg)
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
