package org.oppia.app.player.content;

import org.jetbrains.annotations.NotNull;
import org.oppia.data.backends.gae.model.GaeSubtitledHtml;

import java.util.ArrayList;
import java.util.List;

public class MainPresenterImpl implements MainContract.presenter, MainContract.GetContentCardIntractor.OnFinishedListener {

  private MainContract.MainView mainView;
  private MainContract.GetContentCardIntractor getContentCardIntractor;

  public MainPresenterImpl(MainContract.MainView mainView, MainContract.GetContentCardIntractor getContentCardIntractor) {
    this.mainView = mainView;
    this.getContentCardIntractor = getContentCardIntractor;
  }

  @Override
  public void onDestroy() {

    mainView = null;

  }

  @Override
  public void requestDataFromServer() {
    getContentCardIntractor.getContentCardArrayList(this);
  }

  @Override
  public void onFailure(Throwable t) {
    if(mainView != null){
      mainView.onResponseFailure(t);
      mainView.hideProgress();
    }
  }

  @Override
  public void onFinished(@NotNull List<GaeSubtitledHtml> contentList) {
    if(mainView != null){
      mainView.setDataToRecyclerView(contentList);
      mainView.hideProgress();
    }
  }
}
