package org.oppia.android.app.testing

import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.R
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.utility.lifecycle.LifecycleSafeTimerFactory
import org.oppia.android.databinding.DragDropTestFragmentBinding
import java.util.Collections
import javax.inject.Inject
import kotlin.math.abs

/** The presenter for [DragDropTestFragment]. */
class DragDropTestFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val singleTypeBuilderFactory: BindableAdapter.SingleTypeBuilder.Factory
) {

  private var dataList = mutableListOf("Item 1", "Item 2", "Item 3", "Item 4")
  private lateinit var binding: DragDropTestFragmentBinding
  private val LONG_PRESS_TIMEOUT_MS = 300L
  /** This handles OnCreateView() of [DragDropTestFragment]. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?
  ): View? {
    binding = DragDropTestFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    binding.dragDropRecyclerView.apply {
      adapter = createBindableAdapter()
      (adapter as BindableAdapter<*>).setDataUnchecked(dataList)
    }
    return binding.root
  }
  fun addListener(
    context: Context,
    fragment: Fragment,
    lifecycleSafeTimerFactory: LifecycleSafeTimerFactory,
    itemTouchHelper: ItemTouchHelper?
  ) {

    binding.dragDropRecyclerView.apply {
      itemTouchHelper?.attachToRecyclerView(this)
      addOnItemTouchListener(
        createTouchListener(
          context,
          fragment,
          lifecycleSafeTimerFactory,
          itemTouchHelper
        )
      )
    }
  }
  private fun createTouchListener(
    context: Context,
    fragment: Fragment,
    lifecycleSafeTimerFactory: LifecycleSafeTimerFactory,
    itemTouchHelper: ItemTouchHelper?
  ): RecyclerView.OnItemTouchListener {
    return object : RecyclerView.OnItemTouchListener {
      private var startX = 0f
      private var startY = 0f
      private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
      private var currentTimer: LiveData<Any>? = null

      override fun onInterceptTouchEvent(recyclerView: RecyclerView, event: MotionEvent): Boolean {
        when (event.action) {
          MotionEvent.ACTION_DOWN -> {
            startX = event.x
            startY = event.y
            cancelCurrentTimer()

            fragment.let { fragmentOwner ->
              currentTimer = lifecycleSafeTimerFactory.createTimer(LONG_PRESS_TIMEOUT_MS).apply {
                observeOnce(fragmentOwner.viewLifecycleOwner) {
                  recyclerView.findChildViewUnder(startX, startY)?.let { childView ->
                    recyclerView.findContainingViewHolder(childView)?.let { viewHolder ->
                      itemTouchHelper?.startDrag(viewHolder)
                    }
                  }
                }
              }
            }
          }

          MotionEvent.ACTION_MOVE -> {
            if (abs(event.x - startX) > touchSlop || abs(event.y - startY) > touchSlop) {
              cancelCurrentTimer()
            }
          }

          MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
            cancelCurrentTimer()
          }
        }
        return false
      }

      override fun onTouchEvent(recyclerView: RecyclerView, event: MotionEvent) {}

      override fun onRequestDisallowInterceptTouchEvent(disallow: Boolean) {}

      private fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, observer: Observer<T>) {
        observe(
          owner,
          object : Observer<T> {
            override fun onChanged(t: T?) {
              observer.onChanged(t)
              removeObserver(this)
            }
          }
        )
      }

      private fun cancelCurrentTimer() {
        currentTimer?.let { timer ->
          fragment.viewLifecycleOwner.let { lifecycleOwner ->
            timer.removeObservers(lifecycleOwner)
          }
        }
        currentTimer = null
      }
    }
  }

  private fun createBindableAdapter(): BindableAdapter<String> {
    return singleTypeBuilderFactory.create<String>()
      .registerViewBinder(
        inflateView = this::inflateTextViewForStringWithoutDataBinding,
        bindView = this::bindTextViewForStringWithoutDataBinding
      )
      .build()
  }

  private fun bindTextViewForStringWithoutDataBinding(textView: TextView, data: String) {
    textView.text = data
  }

  private fun inflateTextViewForStringWithoutDataBinding(viewGroup: ViewGroup): TextView {
    val inflater = LayoutInflater.from(activity)
    return inflater.inflate(
      R.layout.test_text_view_for_string_no_data_binding, viewGroup, /* attachToRoot= */ false
    ) as TextView
  }

  /** This handles dragging of items from given position in [DragDropTestFragment]. */
  fun onItemDragged(
    indexFrom: Int,
    indexTo: Int,
    adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
  ) {
    if (indexFrom == indexTo) return
    Collections.swap(dataList, indexFrom, indexTo)

    adapter.notifyItemMoved(indexFrom, indexTo)
    (adapter as BindableAdapter<*>).setDataUnchecked(dataList)
  }

  /** This receives dragEndedEvent and unchecks data list in [DragDropTestFragment]. */
  fun onDragEnded(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
    (adapter as BindableAdapter<*>).setDataUnchecked(dataList)
  }
}
