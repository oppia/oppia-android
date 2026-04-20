package org.oppia.android.app.databinding.adapters;

import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.view.View;
import android.widget.TextView;
import androidx.databinding.BindingAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.widget.CompoundButton;

public class MissingAndroidXAdapters {
  @BindingAdapter("android:onCheckedChanged")
  public static void setOnCheckedChanged(CompoundButton view, CompoundButton.OnCheckedChangeListener listener) {
    view.setOnCheckedChangeListener(listener);
  }

  @BindingAdapter("android:clickable")
  public static void setIsClickable(View view, boolean isClickable) {
    view.setClickable(isClickable);
  }

  @BindingAdapter("android:onClick")
  public static void setOnClick(View view, View.OnClickListener listener) {
    view.setOnClickListener(listener);
  }

  @BindingAdapter("android:paddingStart")
  public static void setPaddingStart(View view, float padding) {
    view.setPaddingRelative(
      (int) padding,
      view.getPaddingTop(),
      view.getPaddingEnd(),
      view.getPaddingBottom()
    );
  }

  @BindingAdapter("android:paddingEnd")
  public static void setPaddingEnd(View view, float padding) {
    view.setPaddingRelative(
      view.getPaddingStart(),
      view.getPaddingTop(),
      (int) padding,
      view.getPaddingBottom()
    );
  }

  @BindingAdapter("android:paddingEnd")
  public static void setPaddingEnd(View view, int padding) {
    view.setPaddingRelative(
      view.getPaddingStart(),
      view.getPaddingTop(),
      padding,
      view.getPaddingBottom()
    );
  }

  @BindingAdapter("android:paddingTop")
  public static void setPaddingTop(View view, float padding) {
    view.setPadding(
      view.getPaddingLeft(),
      (int) padding,
      view.getPaddingRight(),
      view.getPaddingBottom()
    );
  }

  @BindingAdapter("android:paddingBottom")
  public static void setPaddingBottom(View view, float padding) {
    view.setPadding(
      view.getPaddingLeft(),
      view.getPaddingTop(),
      view.getPaddingRight(),
      (int) padding
    );
  }

  @BindingAdapter("android:maxLength")
  public static void setMaxLength(TextView view, int length) {
    InputFilter[] filters = view.getFilters();
    if (filters == null) {
      filters = new InputFilter[0];
    }
    List<InputFilter> newFilters = new ArrayList<>(Arrays.asList(filters));
    newFilters.add(new LengthFilter(length));
    view.setFilters(newFilters.toArray(new InputFilter[0]));
  }

  @BindingAdapter("android:textSize")
  public static void setOppiaTextSize(TextView textView, float textSizeInPx) {
    textView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, textSizeInPx);
  }
}