package org.oppia.app.help.faq.faqsingle

import android.os.Build
import android.text.Html
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.databinding.FaqSingleActivityBinding
import javax.inject.Inject

/** The presenter for [FAQSingleActivity]. */
@ActivityScope
class FAQSingleActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private lateinit var faqSingleActivityToolbar: Toolbar

  fun handleOnCreate(question: String, answer: String) {
    val binding = DataBindingUtil.setContentView<FaqSingleActivityBinding>(
      activity,
      R.layout.faq_single_activity
    )
    binding.apply {
      lifecycleOwner = activity
    }

    faqSingleActivityToolbar = binding.faqSingleActivityToolbar
    activity.setSupportActionBar(faqSingleActivityToolbar)
    activity.supportActionBar!!.title = activity.resources.getString(R.string.FAQs)
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    binding.faqSingleActivityToolbar.setNavigationOnClickListener {
      (activity as FAQSingleActivity).finish()
    }
    activity.findViewById<TextView>(R.id.faq_question_text_view).text = question
    activity.findViewById<TextView>(R.id.faq_answer_text_view).text = if (
      Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    ) {
      Html.fromHtml(answer, Html.FROM_HTML_MODE_COMPACT)
    } else {
      Html.fromHtml(answer)
    }
  }
}
