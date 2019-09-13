package org.oppia.app.player.content

import android.content.Context
import android.text.Html
import android.text.Spannable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.app.utility.URLImageParser
import org.oppia.data.backends.gae.model.GaeSubtitledHtml

import java.util.ArrayList

class ContentCardAdapter(internal var context: Context, contentList: List<GaeSubtitledHtml>) :
  RecyclerView.Adapter<ContentCardAdapter.MyViewHolder>() {
  internal var contentList: List<GaeSubtitledHtml> = ArrayList()

  init {
    // TODO Auto-generated constructor stub
    this.contentList = contentList

  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
    // System.out.println("hiiiiii"+shopList.get(0).getShop_name());
    val itemView = LayoutInflater.from(parent.context)
      .inflate(R.layout.content_card_items, parent, false)

    return MyViewHolder(itemView)
  }

  override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

    val imageGetter = URLImageParser(holder.tvContents, context)
    val html: Spannable
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
      html = Html.fromHtml(contentList[position].html, Html.FROM_HTML_MODE_LEGACY, imageGetter, null) as Spannable
    } else {
      html = Html.fromHtml(contentList[position].html, imageGetter, null) as Spannable
    }
    holder.tvContents.text = html
  }

  inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    internal var tvContents: TextView

    init {
      tvContents = view.findViewById(R.id.tvContents)

    }
  }

  override fun getItemCount(): Int {
    return contentList.size
  }

}
