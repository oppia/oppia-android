package org.oppia.android.app.player.state.itemviewmodel.submittedanswers

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.databinding.SubmittedAnswerListItemBinding
import org.oppia.android.databinding.SubmittedHtmlAnswerItemBinding

// TODO: doc
class SubmittedAnswerRecyclerView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {
  init {
    adapter =
      BindableAdapter.SingleTypeBuilder
        .newBuilder<SubmittedHtmlAnswerListViewModel>()
        .registerViewDataBinderWithSameModelType(
          inflateDataBinding = SubmittedAnswerListItemBinding::inflate,
          setViewModel = SubmittedAnswerListItemBinding::setViewModel
        )
        .build()
  }
}
