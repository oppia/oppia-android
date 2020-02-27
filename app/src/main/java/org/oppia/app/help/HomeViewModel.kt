package org.oppia.app.help

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel{
  var title = ""

  constructor() : super()

  constructor(category: Category) : super() {
    this.title = category.title
  }

  var arrayListMutableLiveData = MutableLiveData<ArrayList<HomeViewModel>>()
  var arrayList = ArrayList<HomeViewModel>()

  fun getArrayList():MutableLiveData<ArrayList<HomeViewModel>>{
    val category1 = Category("Frequently Asked Question (FAQs)")
    val homeViewModel1: HomeViewModel = HomeViewModel(category1)
    arrayList!!.add(homeViewModel1)
    arrayListMutableLiveData.value = arrayList
    return arrayListMutableLiveData
  }

}