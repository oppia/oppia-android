package org.oppia.app.help

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CategoryViewModel : ViewModel{
  var title = ""

  constructor() : super()

  constructor(category: Category) : super() {
    this.title = category.title
  }

  var arrayListMutableLiveData = MutableLiveData<ArrayList<CategoryViewModel>>()
  var arrayList = ArrayList<CategoryViewModel>()

  fun getArrayList():MutableLiveData<ArrayList<CategoryViewModel>>{
    val category1 = Category("Frequently Asked Question (FAQs)")
    val category2 = Category("Send Feedback")
    val categoryViewModel1: CategoryViewModel = CategoryViewModel(category1)
    val categoryViewModel2: CategoryViewModel = CategoryViewModel(category2)
    arrayList!!.add(categoryViewModel1)
    arrayList!!.add(categoryViewModel2)
    arrayListMutableLiveData.value = arrayList
    return arrayListMutableLiveData
  }

}