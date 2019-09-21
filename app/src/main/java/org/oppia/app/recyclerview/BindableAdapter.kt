package org.oppia.app.recyclerview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import kotlin.reflect.KClass

/** A function that returns the type of view that should can bind the specified data object. */
typealias ComputeViewType<T> = (T) -> Int

/**
 * The default type of all views used for adapters that do not need to specify different view types.
 */
const val DEFAULT_VIEW_TYPE = 0

private typealias ViewHolderFactory<T> = (ViewGroup) -> BindableAdapter.BindableViewHolder<T>

/**
 * A generic [RecyclerView.Adapter] that can be initialized using Android data-binding, and bind its
 * own child views using Android data-binding (or custom View bind methods).
 *
 * This is loosely based on https://android.jlelse.eu/1bd08b4796b4 except the concept was extended
 * to include seamlessly binding to views using data-binding in a type-safe and lifecycle-safe way.
 */
class BindableAdapter<T : Any> internal constructor(
  private val computeViewType: ComputeViewType<T>,
  private val viewHolderFactoryMap: Map<Int, ViewHolderFactory<T>>,
  private val dataClassType: KClass<T>
) : RecyclerView.Adapter<BindableAdapter.BindableViewHolder<T>>() {
  private val dataList: MutableList<T> = ArrayList()

  // TODO(#170): Introduce support for stable IDs.

  /** Sets the data of this adapter. This is expected to be called by Android via data-binding. */
  fun setData(newDataList: List<T>) {
    dataList.clear()
    dataList += newDataList
    // TODO(#171): Introduce diffing to notify subsets of the view to properly support animations
    //  rather than re-binding the entire list upon any change.
    notifyDataSetChanged()
  }

  /**
   * Sets the data of this adapter in the same way as [setData], except with a different type.
   *
   * This method ensures the type of data being passed in is compatible with the type of this
   * adapter. This helps ensure type compatibility at runtime in cases where the generic type of the
   * adapter object is lost.
   */
  fun <T2 : Any> setDataUnchecked(newDataList: List<T2>) {
    // NB: This check only works if the list has any data in it. Since we can't use a reified type
    // here (due to Android data binding not supporting custom adapters with inline functions), this
    // method will succeed if types are different for empty lists (that is, List<T1> == List<T2>
    // when T1 is not assignable to T2). This likely won't have bad side effects since any time a
    // non-empty list is attempted to be bound, this crash will be correctly triggered.
    newDataList.firstOrNull()?.let {
      check(it.javaClass.isAssignableFrom(dataClassType.java)) {
        "Trying to bind incompatible data to adapter. Data class type: ${it.javaClass}, " +
            "expected adapter class type: $dataClassType."
      }
    }
    @Suppress("UNCHECKED_CAST") // This is safe. See the above check.
    setData(newDataList as List<T>)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindableViewHolder<T> {
    val viewHolderFactory = viewHolderFactoryMap[viewType]
    checkNotNull(viewHolderFactory) { "Encountered missing view factory for type: $viewType" }
    return viewHolderFactory(parent)
  }

  override fun getItemCount(): Int {
    return dataList.size
  }

  override fun getItemViewType(position: Int): Int {
    return computeViewType(dataList[position])
  }

  override fun onBindViewHolder(holder: BindableViewHolder<T>, position: Int) {
    holder.bind(dataList[position])
  }

  /** A generic [RecyclerView.ViewHolder] that generically binds data to the specified view. */
  abstract class BindableViewHolder<T> internal constructor(
    view: View
  ) : RecyclerView.ViewHolder(view) {
    internal abstract fun bind(data: T)
  }

  /**
   * Constructs a new [BindableAdapter]. Each type returned by the computer should have an
   * associated view binder. If no computer is set, then [DEFAULT_VIEW_TYPE] is the default view
   * type.
   *
   * Instances of [Builder] should be instantiated using [newBuilder].
   */
  class Builder<T : Any>(private val dataClassType: KClass<T>) {
    private var computeViewType: ComputeViewType<T> = { DEFAULT_VIEW_TYPE }
    private var viewHolderFactoryMap: MutableMap<Int, ViewHolderFactory<T>> = HashMap()

    /**
     * Registers a function which returns the type of view a specific data item corresponds to. This defaults to always
     * assuming all views are the same time: [DEFAULT_VIEW_TYPE].
     *
     * This function must be used if multiple views need to be supported by the [RecyclerView].
     *
     * @return this
     */
    fun registerViewTypeComputer(computeViewType: ComputeViewType<T>): Builder<T> {
      this.computeViewType = computeViewType
      return this
    }

    /**
     * Registers a [View] inflater and bind function for views of the specified view type (with default value
     * [DEFAULT_VIEW_TYPE] for single-view [RecyclerView]s). Note that the viewType specified here must correspond to a
     * view type registered in [registerViewTypeComputer] if non-default, or if any view type computer has been
     * registered.
     *
     * The inflateView and bindView functions passed in here must not hold any references to UI objects except those
     * that own the RecyclerView.
     *
     * @param inflateView function that takes a parent [ViewGroup] and returns a newly inflated [View] of type [V]
     * @param bindView function that takes a [RecyclerView]-owned [View] of type [V] and binds a data element typed [T]
     *     to it
     * @return this
     */
    fun <V : View> registerViewBinder(
      viewType: Int = DEFAULT_VIEW_TYPE, inflateView: (ViewGroup) -> V, bindView: (V, T) -> Unit
    ): Builder<T> {
      checkViewTypeIsUnique(viewType)
      val viewHolderFactory: ViewHolderFactory<T> = { viewGroup ->
        // This is lifecycle-safe since it will become dereferenced when the factory method returns.
        // The version referenced in the anonymous BindableViewHolder object should be copied into a
        // class field that binds that reference's lifetime to the view holder's lifetime. This
        // approach avoids needing to perform an unsafe cast later when binding the view.
        val inflatedView = inflateView(viewGroup)
        object : BindableViewHolder<T>(inflatedView) {
          override fun bind(data: T) {
            bindView(inflatedView, data)
          }
        }
      }
      viewHolderFactoryMap[viewType] = viewHolderFactory
      return this
    }

    /**
     * Behaves in the same way as [registerViewBinder] except the inflate and bind methods correspond to a [View]
     * data-binding typed [DB].
     *
     * @param inflateDataBinding a function that inflates the root view of a data-bound layout (e.g.
     *     MyDataBinding::inflate). This may also be a function that initializes the data-binding with additional
     *     properties as necessary.
     * @param setViewModel a function that initializes the view model in the data-bound view (e.g.
     *     MyDataBinding::setSpecialViewModel). This may also be a function that initializes the view model & other
     *     view-accessible properties as necessary.
     * @return this
     */
    fun <DB : ViewDataBinding> registerViewDataBinder(
      viewType: Int = DEFAULT_VIEW_TYPE,
      inflateDataBinding: (LayoutInflater, ViewGroup, Boolean) -> DB,
      setViewModel: (DB, T) -> Unit
    ): Builder<T> {
      checkViewTypeIsUnique(viewType)
      val viewHolderFactory: ViewHolderFactory<T> = { viewGroup ->
        // See registerViewBinder() comments for why this approach should be lifecycle safe and not
        // introduce memory leaks.
        val binding = inflateDataBinding(
          LayoutInflater.from(viewGroup.context),
          viewGroup,
          /* attachToRoot= */ false
        )
        object : BindableViewHolder<T>(binding.root) {
          override fun bind(data: T) {
            setViewModel(binding, data)
          }
        }
      }
      viewHolderFactoryMap[viewType] = viewHolderFactory
      return this
    }

    private fun checkViewTypeIsUnique(viewType: Int) {
      check(!viewHolderFactoryMap.containsKey(viewType)) {
        "Cannot register a second view binder for view type: $viewType (current binder: " +
            "${viewHolderFactoryMap[viewType]}."
      }
    }

    /** Returns a new [BindableAdapter]. */
    fun build(): BindableAdapter<T> {
      val computeViewType = this.computeViewType
      check(viewHolderFactoryMap.isNotEmpty()) { "At least one view binder must be registered" }
      return BindableAdapter(computeViewType, viewHolderFactoryMap, dataClassType)
    }

    companion object {
      /** Returns a new [Builder]. */
      inline fun <reified T : Any> newBuilder(): Builder<T> {
        return Builder(T::class)
      }
    }
  }
}
