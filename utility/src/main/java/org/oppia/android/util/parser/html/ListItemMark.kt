package org.oppia.android.util.parser.html

/** Interface for invisible spans that aren't used for styling. */
sealed class ListItemMark

/** Marks the opening tag location of a list item inside an <ul> element. */
class BulletListItem : ListItemMark()

/** Marks the opening tag location of a list item inside an <ol> element. */
class NumberListItem(val number: Int) : ListItemMark()
