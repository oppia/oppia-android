package org.oppia.app.drawer


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.app.drawer.model.DrawerModel

class DrawerAdapter(private val context: Context, arrayList: ArrayList<DrawerModel>) : RecyclerView.Adapter<DrawerAdapter.ViewHolder>() {

  internal var arrayList = ArrayList<DrawerModel>()
  private val inflater: LayoutInflater

  init {
    inflater = LayoutInflater.from(context)
    this.arrayList = arrayList
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view = inflater.inflate(R.layout.lv_item, parent, false)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {

    holder.title.setText(arrayList[position].getNames())
    holder.ivicon.setImageResource(arrayList[position].getImages())
  }

  override fun getItemCount(): Int {
    return arrayList.size
  }

  inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var title: TextView
    var ivicon: ImageView

    init {
      title = itemView.findViewById(R.id.name) as TextView
      ivicon = itemView.findViewById(R.id.ivicon) as ImageView
    }
  }
}
