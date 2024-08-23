package org.oppia.android.app.testing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.ObservableField
import org.oppia.android.R
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.TextInputLayoutBindingAdaptersTestFragmentBinding
import javax.inject.Inject

/** Test-only fragment for verifying behaviors of [TextInputLayoutBindingAdapters]. */
class TextInputLayoutBindingAdaptersTestFragment : InjectableFragment() {

  private lateinit var binding: TextInputLayoutBindingAdaptersTestFragmentBinding
  @Inject
  lateinit var appLanguageResourceHandler: AppLanguageResourceHandler

  /** Observable field representing the selected item from the dropdown. */
  val selectedLanguage = ObservableField(OppiaLanguage.LANGUAGE_UNSPECIFIED)

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = TextInputLayoutBindingAdaptersTestFragmentBinding.inflate(
      inflater,
      container,
      false
    )

    binding.apply {
      lifecycleOwner = this@TextInputLayoutBindingAdaptersTestFragment
      fragment = this@TextInputLayoutBindingAdaptersTestFragment
    }

    val adapter = ArrayAdapter(
      requireContext(),
      R.layout.onboarding_language_dropdown_item,
      R.id.onboarding_language_text_view,
      OppiaLanguage.values()
    )

    binding.testAutocompleteView.apply {
      setRawInputType(EditorInfo.TYPE_NULL)
      setAdapter(adapter)
      onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
        val selectedItem = adapter.getItem(position) as? String
        selectedItem?.let {
          val localizedNameMap = OppiaLanguage.values().associateBy { oppiaLanguage ->
            appLanguageResourceHandler.computeLocalizedDisplayName(oppiaLanguage)
          }
          selectedLanguage.set(localizedNameMap[it] ?: OppiaLanguage.ENGLISH)
        }
      }
    }

    return binding.root
  }
}
