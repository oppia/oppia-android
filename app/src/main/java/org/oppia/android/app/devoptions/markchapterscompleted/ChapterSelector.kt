package org.oppia.android.app.devoptions.markchapterscompleted

/** Interface to update the selectedChapterList in [MarkChaptersCompletedFragmentPresenter]. */
interface ChapterSelector {
  /** This chapter will get added to selectedTopicList in [MarkChaptersCompletedFragmentPresenter]. */
  fun chapterSelected(chapterIndex: Int, nextStoryIndex: Int, explorationId: String)

  /** This chapter will get removed from selectedTopicList in [MarkChaptersCompletedFragmentPresenter]. */
  fun chapterUnselected(chapterIndex: Int, nextStoryIndex: Int)
}
