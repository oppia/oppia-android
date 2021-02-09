package org.oppia.android.app.recyclerview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.app.recyclerview.BindableAdapter.MultiTypeBuilder.Companion.newBuilder
import org.oppia.android.app.recyclerview.BindableAdapter.SingleTypeBuilder.Companion.newBuilder
import java.lang.ref.WeakReference
import kotlin.reflect.KClass

/** A function that returns the integer-based type of view that can bind the specified object. */
private typealias ComputeIntViewType<T> = (T) -> Int

/** A function that returns the enum-based type of view that can bind the specified data object. */
typealias ComputeViewType<T, E> = (T) -> E

/** The default type of all views used in single-type adapters. */
private const val DEFAULT_VIEW_TYPE = 0

private typealias ViewHolderFactory<T> = (ViewGroup) -> BindableAdapter.BindableViewHolder<T>

/**
 * A generic [RecyclerView.Adapter] that can be initialized using Android data-binding, and bind its
 * own child views using Android data-binding (or custom View bind methods).
 *
 * This is loosely based on https://android.jlelse.eu/1bd08b4796b4 except the concept was extended
 * to include seamlessly binding to views using data-binding in a type-safe and lifecycle-safe way.
 */
class BindableAdapter<T : Any> internal constructor(
  private val computeIntViewType: ComputeIntViewType<T>,
  private val viewHolderFactoryMap: Map<Int, ViewHolderFactory<T>>,
  private val dataClassType: KClass<T>
) : RecyclerView.Adapter<BindableAdapter.BindableViewHolder<T>>() {
  private val dataList: MutableList<T> = ArrayList()

  // TODO(#170): Introduce support for stable IDs.

  /** Sets the data of this adapter. This is expected to be called by Android via data-binding. */
  private fun setData(newDataList: List<T>) {
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
      check(dataClassType.java.isAssignableFrom(it.javaClass)) {
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
    return computeIntViewType(dataList[position])
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
   * The base builder for [BindableAdapter]. This class should not be used directly--use either
   * [SingleTypeBuilder] or [MultiTypeBuilder] instead.
   */
  abstract class BaseBuilder<BuilderType : Any> {
    /**
     * A [WeakReference] to a [LifecycleOwner] for databinding inflation. See [setLifecycleOwner].
     * Note that this needs to be a weak reference so that long-held references to the adapter do
     * not potentially leak lifecycle owners (such as fragments and activities).
     */
    private var lifecycleOwnerRef: WeakReference<LifecycleOwner>? = null

    /**
     * Sets the [LifecycleOwner] corresponding to this adapter. This will automatically be used as
     * the lifecycle owner for all databinding classes created during view inflation. Note that the
     * adapter holds a weak reference to the owner to ensure long-lived references to the adapter
     * class itself does not result in leaks, however it's up to the caller's responsibility to make
     * sure that the adapter itself is not actually used after the lifecycle owner has expired
     * (otherwise the app may crash).
     *
     * @return this
     */
    fun setLifecycleOwner(lifecycleOwner: LifecycleOwner): BuilderType {
      check(lifecycleOwnerRef == null) {
        "A lifecycle owner has already been bound to this adapter."
      }
      lifecycleOwnerRef = WeakReference(lifecycleOwner)

      // This cast is not, strictly speaking, safe, but child classes are expected to inherit from
      // the builder & pass their own type in.
      @Suppress("UNCHECKED_CAST") return this as BuilderType
    }

    /**
     * Returns the [LifecycleOwner] bound to this adapter, or null if there isn't one. This method
     * will throw if there was a lifecycle owner bound but is now expired.
     */
    protected fun getLifecycleOwner(): LifecycleOwner? {
      // Crash if the lifecycle owner has been cleaned up since it's not valid to use the adapter
      // with an old lifecycle owner, and silently ignoring this may result in part of the layout
      // not responding to events.
      return lifecycleOwnerRef?.let { ref ->
        checkNotNull(ref.get()) {
          "Attempted to inflate data binding with expired lifecycle owner"
        }
      }
    }
  }

  /**
   * Constructs a new [BindableAdapter] that for a single view type.
   *
   * Instances of [SingleTypeBuilder] should be instantiated using [newBuilder].
   */
  class SingleTypeBuilder<T : Any>(
    private val dataClassType: KClass<T>
  ) : BaseBuilder<SingleTypeBuilder<T>>() {
    private lateinit var viewHolderFactory: ViewHolderFactory<T>

    /**
     * Registers a [View] inflater and bind function for views in the recycler view.
     *
     * The inflateView and bindView functions passed in here must not hold any references to UI
     * objects except those that own the RecyclerView.
     *
     * @param inflateView function that takes a parent [ViewGroup] and returns a newly inflated
     *     [View] of type [V]
     * @param bindView function that takes a [RecyclerView]-owned [View] of type [V] and binds a
     *     data element typed [T] to it
     * @return this
     */
    fun <V : View> registerViewBinder(
      inflateView: (ViewGroup) -> V,
      bindView: (V, T) -> Unit
    ): SingleTypeBuilder<T> {
      check(!::viewHolderFactory.isInitialized) { "A view binder is already initialized" }
      viewHolderFactory = { viewGroup ->
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
      return this
    }

    /** See [registerViewDataBinder]. */
    fun <DB : ViewDataBinding> registerViewDataBinderWithSameModelType(
      inflateDataBinding: (LayoutInflater, ViewGroup, Boolean) -> DB,
      setViewModel: (DB, T) -> Unit
    ): SingleTypeBuilder<T> {
      return registerViewDataBinder(
        inflateDataBinding = inflateDataBinding,
        setViewModel = setViewModel
      )
    }

    /**
     * Behaves in the same way as [registerViewBinder] except the inflate and bind methods
     * correspond to a [View] data-binding typed [DB].
     *
     * @param inflateDataBinding a function that inflates the root view of a data-bound layout (e.g.
     *     MyDataBinding::inflate). This may also be a function that initializes the data-binding
     *     with additional properties as necessary.
     * @param setViewModel a function that initializes the view model in the data-bound view (e.g.
     *     MyDataBinding::setSpecialViewModel). This may also be a function that initializes the
     *     view model & other view-accessible properties as necessary.
     * @return this
     */
    private fun <DB : ViewDataBinding> registerViewDataBinder(
      inflateDataBinding: (LayoutInflater, ViewGroup, Boolean) -> DB,
      setViewModel: (DB, T) -> Unit
    ): SingleTypeBuilder<T> {
      check(!::viewHolderFactory.isInitialized) { "A view binder is already initialized" }
      viewHolderFactory = { viewGroup ->
        // See registerViewBinder() comments for why this approach should be lifecycle safe and not
        // introduce memory leaks.
        val binding = inflateDataBinding(
          LayoutInflater.from(viewGroup.context),
          viewGroup,
          /* attachToRoot= */ false
        )
        binding.lifecycleOwner = getLifecycleOwner()
        object : BindableViewHolder<T>(binding.root) {
          override fun bind(data: T) {
            setViewModel(binding, data)
          }
        }
      }
      return this
    }

    /** Returns a new [BindableAdapter]. */
    fun build(): BindableAdapter<T> {
      check(::viewHolderFactory.isInitialized) { "A view binder must be initialized" }
      return BindableAdapter(
        { DEFAULT_VIEW_TYPE },
        mapOf(DEFAULT_VIEW_TYPE to viewHolderFactory),
        dataClassType
      )
    }

    companion object {
      /** Returns a new [SingleTypeBuilder]. */
      inline fun <reified T : Any> newBuilder(): SingleTypeBuilder<T> {
        return SingleTypeBuilder(T::class)
      }
    }
  }

  /**
   * Constructs a new [BindableAdapter] that supports multiple view types. Each type returned by the
   * computer should have an associated view binder.
   *
   * Instances of [MultiTypeBuilder] should be instantiated using [newBuilder].
   */
  class MultiTypeBuilder<T : Any, E : Enum<E>>(
    private val dataClassType: KClass<T>,
    private val computeViewType: ComputeViewType<T, E>
  ) : BaseBuilder<MultiTypeBuilder<T, E>>() {
    private var viewHolderFactoryMap: MutableMap<E, ViewHolderFactory<T>> = HashMap()

    /**
     * Registers a [View] inflater and bind function for views of the specified view type (with
     * default value [DEFAULT_VIEW_TYPE] for single-view [RecyclerView]s). Note that the viewType
     * specified here must be properly returned in the [ComputeViewType] function passed into
     * [newBuilder].
     *
     * The inflateView and bindView functions passed in here must not hold any references to UI
     * objects except those that own the RecyclerView.
     *
     * @param viewType the type of the view being bound
     * @param inflateView function that takes a parent [ViewGroup] and returns a newly inflated
     *     [View] of type [V]
     * @param bindView function that takes a [RecyclerView]-owned [View] of type [V] and binds a
     *     data element typed [T] to it
     * @return this
     */
    fun <V : View> registerViewBinder(
      viewType: E,
      inflateView: (ViewGroup) -> V,
      bindView: (V, T) -> Unit
    ): MultiTypeBuilder<T, E> {
      checkViewTypeIsUnique(viewType)
      val viewHolderFactory: ViewHolderFactory<T> = { viewGroup ->
        // Note on lifecycle safety: this is lifecycle-safe since it will become dereferenced when
        // the factory method returns. The version referenced in the anonymous BindableViewHolder
        // object should be copied into a class field that binds that reference's lifetime to the
        // view holder's lifetime. This approach avoids needing to perform an unsafe cast later when
        // binding the view.
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

    /** See [registerViewDataBinder]. */
    fun <DB : ViewDataBinding> registerViewDataBinderWithSameModelType(
      viewType: E,
      inflateDataBinding: (LayoutInflater, ViewGroup, Boolean) -> DB,
      setViewModel: (DB, T) -> Unit
    ): MultiTypeBuilder<T, E> {
      return registerViewDataBinder(
        viewType = viewType, inflateDataBinding = inflateDataBinding, setViewModel = setViewModel,
        transformViewModel = { it }
      )
    }

    /**
     * Behaves in the same way as [registerViewBinder] except the inflate and bind methods
     * correspond to a [View] data-binding typed [DB].
     *
     * @param viewType the type of the view being bound
     * @param inflateDataBinding a function that inflates the root view of a data-bound layout (e.g.
     *     MyDataBinding::inflate). This may also be a function that initializes the data-binding
     *     with additional properties as necessary.
     * @param setViewModel a function that initializes the view model in the data-bound view (e.g.
     *     MyDataBinding::setSpecialViewModel). This may also be a function that initializes the
     *     view model & other view-accessible properties as necessary.
     * @param transformViewModel a function that converts the input model to a model of another type
     *     (such as for cases when subclassing is used to represent more complex lists of data).
     * @return this
     */
    fun <DB : ViewDataBinding, T2 : T> registerViewDataBinder(
      viewType: E,
      inflateDataBinding: (LayoutInflater, ViewGroup, Boolean) -> DB,
      setViewModel: (DB, T2) -> Unit,
      transformViewModel: (T) -> T2
    ): MultiTypeBuilder<T, E> {
      checkViewTypeIsUnique(viewType)
      val viewHolderFactory: ViewHolderFactory<T> = { viewGroup ->
        // See registerViewBinder() comments for why this approach should be lifecycle safe and not
        // introduce memory leaks.
        val binding = inflateDataBinding(
          LayoutInflater.from(viewGroup.context),
          viewGroup,
          /* attachToRoot= */ false
        )
        binding.lifecycleOwner = getLifecycleOwner()
        object : BindableViewHolder<T>(binding.root) {
          override fun bind(data: T) {
            setViewModel(binding, transformViewModel(data))
          }
        }
      }
      viewHolderFactoryMap[viewType] = viewHolderFactory
      return this
    }

    private fun checkViewTypeIsUnique(viewType: E) {
      check(!viewHolderFactoryMap.containsKey(viewType)) {
        "Cannot register a second view binder for view type: $viewType (current binder: " +
          "${viewHolderFactoryMap[viewType]}."
      }
    }

    /** Returns a new [BindableAdapter]. */
    fun build(): BindableAdapter<T> {
      check(viewHolderFactoryMap.isNotEmpty()) { "At least one view binder must be registered" }
      return BindableAdapter(
        { value -> computeViewType(value).ordinal },
        viewHolderFactoryMap.mapKeys { entry -> entry.key.ordinal },
        dataClassType
      )
    }

    companion object {
      /**
       * Returns a new [MultiTypeBuilder] with the specified function that returns the enum type of
       * view a specific data item corresponds to.
       */
      inline fun <reified T : Any, reified E : Enum<E>> newBuilder(
        noinline computeViewType: ComputeViewType<T, E>
      ): MultiTypeBuilder<T, E> {
        return MultiTypeBuilder(T::class, computeViewType)
      }
    }
  }
}
