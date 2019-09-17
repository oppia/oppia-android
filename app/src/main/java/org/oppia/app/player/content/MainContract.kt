package org.oppia.app.player.content

import org.oppia.data.backends.gae.model.GaeSubtitledHtml

import java.util.ArrayList

interface MainContract {

  /**
   * Call when user interact with the view and other when view OnDestroy()
   */
  interface presenter {

    fun onDestroy()

    fun requestDataFromServer()

  }

  /**
   * showProgress() and hideProgress() would be used for displaying and hiding the progressBar
   * while the setDataToRecyclerView and onResponseFailure is fetched from the GetNoticeInteractorImpl class
   */
  interface MainView {

    fun showProgress()

    fun hideProgress()

    fun setDataToRecyclerView(contentList: MutableList<GaeSubtitledHtml>)

    fun onResponseFailure(throwable: Throwable)

  }

  /**
   * Intractors are classes built for fetching data from your database, web services, or any other data source.
   */
  interface GetContentCardIntractor {
    interface OnFinishedListener {
      fun onFinished(contentList: MutableList<GaeSubtitledHtml>)
      fun onFailure(t: Throwable)
    }

    fun getContentCardArrayList(onFinishedListener: OnFinishedListener)
  }
}
