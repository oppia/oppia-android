package org.oppia.app.drawer

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.*

import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.HomeFragment

import org.oppia.app.R
import org.oppia.app.drawer.model.DrawerModel
import org.oppia.app.drawer.ui.help.HelpFragment

class DrawerFragment : Fragment() {

  private var views: View? = null
  private var mDrawerToggle: ActionBarDrawerToggle? = null
  private var mDrawerLayout: DrawerLayout? = null
  private var drawerAdapter: DrawerAdapter? = null
  private var containerView: View? = null
  private var recyclerView: RecyclerView? = null
  private val names = arrayOf("Friends List", "Notification")
  private val images = intArrayOf(R.drawable.ic_help_grey_48dp, R.drawable.ic_logout_48dp)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    // Inflate the layout for this fragment
    views = inflater!!.inflate(R.layout.fragment_drawer, container, false)
    recyclerView = views!!.findViewById<View>(R.id.listview) as RecyclerView
    drawerAdapter = DrawerAdapter(activity!!, populateList())

    recyclerView!!.adapter = drawerAdapter
    recyclerView!!.layoutManager = LinearLayoutManager(activity)
    recyclerView!!.addOnItemTouchListener(RecyclerTouchListener(activity!!, recyclerView!!, object : ClickListener {
      override fun onClick(view: View, position: Int) {
        openFragment(position)
        mDrawerLayout!!.closeDrawer(containerView!!)
      }

      override fun onLongClick(view: View?, position: Int) {

      }
    }))

    openFragment(0)

    return views
  }

  private fun openFragment(position: Int) {

    when (position) {
      0 -> removeAllFragment(HomeFragment(), "Friends")
      1 -> removeAllFragment(HelpFragment(), "Notifiaction")

      else -> {
      }
    }
  }

  fun removeAllFragment(replaceFragment: Fragment, tag: String) {
    val manager = activity!!.supportFragmentManager
    val ft = manager.beginTransaction()
    manager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

    ft.replace(R.id.container_body, replaceFragment)
    ft.commitAllowingStateLoss()
  }

  fun setUpDrawer(fragmentId: Int, drawerLayout: DrawerLayout, toolbar: Toolbar) {
    containerView = activity!!.findViewById(fragmentId)
    mDrawerLayout = drawerLayout
    mDrawerToggle = object : ActionBarDrawerToggle(activity, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
      override fun onDrawerOpened(drawerView: View) {
        super.onDrawerOpened(drawerView)
        activity!!.invalidateOptionsMenu()
      }

      override fun onDrawerClosed(drawerView: View) {
        super.onDrawerClosed(drawerView)
        activity!!.invalidateOptionsMenu()
      }

      override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
        super.onDrawerSlide(drawerView, slideOffset)
        toolbar.alpha = 1 - slideOffset / 2
      }
    }

    mDrawerLayout!!.setDrawerListener(mDrawerToggle)
    mDrawerLayout!!.post { mDrawerToggle!!.syncState() }

  }

  private fun populateList(): ArrayList<DrawerModel> {

    val list = ArrayList<DrawerModel>()

    for (i in names.indices) {
      val drawerModel = DrawerModel()
      drawerModel.name = names[i]
      drawerModel.image = images[i]
      list.add(drawerModel)
    }
    return list
  }

  interface ClickListener {
    fun onClick(view: View, position: Int)

    fun onLongClick(view: View?, position: Int)
  }

  internal class RecyclerTouchListener(context: Context, recyclerView: RecyclerView, private val clickListener: ClickListener?) : RecyclerView.OnItemTouchListener {

    private val gestureDetector: GestureDetector

    init {
      gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
          return true
        }

        override fun onLongPress(e: MotionEvent) {
          val child = recyclerView.findChildViewUnder(e.x, e.y)
          if (child != null && clickListener != null) {
            clickListener.onLongClick(child, recyclerView.getChildPosition(child))
          }
        }
      })
    }

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {

      val child = rv.findChildViewUnder(e.x, e.y)
      if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
        clickListener.onClick(child, rv.getChildPosition(child))
      }
      return false
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

    }
  }
}