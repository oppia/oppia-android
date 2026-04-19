package org.oppia.android.app.customview.interaction;

import androidx.databinding.InverseBindingAdapter;
import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingListener;
import android.widget.EditText;

public class MissingAndroidXInverseAdapters {
  @BindingAdapter("app:iconTint")
  public static void setIconTint(org.oppia.android.app.administratorcontrols.learneranalytics.CopyIdMaterialButtonView view, int color) {
    view.setIconTint(android.content.res.ColorStateList.valueOf(color));
  }

  @BindingAdapter("android:text")
  public static void setText(EditText view, CharSequence text) {
    androidx.databinding.adapters.TextViewBindingAdapter.setText(view, text);
  }

  @BindingAdapter("android:text")
  public static void setAppCompatEditTextText(androidx.appcompat.widget.AppCompatEditText view, CharSequence text) {
    androidx.databinding.adapters.TextViewBindingAdapter.setText(view, text);
  }

  @BindingAdapter("android:text")
  public static void setFractionText(FractionInputInteractionView view, CharSequence text) {
    androidx.databinding.adapters.TextViewBindingAdapter.setText(view, text);
  }

  @BindingAdapter("android:text")
  public static void setMathText(MathExpressionInteractionsView view, CharSequence text) {
    androidx.databinding.adapters.TextViewBindingAdapter.setText(view, text);
  }

  @BindingAdapter("android:text")
  public static void setNumericText(NumericInputInteractionView view, CharSequence text) {
    androidx.databinding.adapters.TextViewBindingAdapter.setText(view, text);
  }

  @BindingAdapter("android:text")
  public static void setRatioText(RatioInputInteractionView view, CharSequence text) {
    androidx.databinding.adapters.TextViewBindingAdapter.setText(view, text);
  }

  @BindingAdapter("android:text")
  public static void setTextInputText(TextInputInteractionView view, CharSequence text) {
    androidx.databinding.adapters.TextViewBindingAdapter.setText(view, text);
  }

  @BindingAdapter(value = {"android:textAttrChanged"}, requireAll = false)
  public static void setTextWatcher(EditText view, InverseBindingListener textAttrChanged) {
    androidx.databinding.adapters.TextViewBindingAdapter.setTextWatcher(view, null, null, null, textAttrChanged);
  }

  @BindingAdapter(value = {"android:textAttrChanged"}, requireAll = false)
  public static void setAppCompatTextWatcher(androidx.appcompat.widget.AppCompatEditText view, InverseBindingListener textAttrChanged) {
    androidx.databinding.adapters.TextViewBindingAdapter.setTextWatcher(view, null, null, null, textAttrChanged);
  }

  @BindingAdapter(value = {"android:textAttrChanged"}, requireAll = false)
  public static void setFractionTextWatcher(FractionInputInteractionView view, InverseBindingListener textAttrChanged) {
    androidx.databinding.adapters.TextViewBindingAdapter.setTextWatcher(view, null, null, null, textAttrChanged);
  }

  @BindingAdapter(value = {"android:textAttrChanged"}, requireAll = false)
  public static void setMathTextWatcher(MathExpressionInteractionsView view, InverseBindingListener textAttrChanged) {
    androidx.databinding.adapters.TextViewBindingAdapter.setTextWatcher(view, null, null, null, textAttrChanged);
  }

  @BindingAdapter(value = {"android:textAttrChanged"}, requireAll = false)
  public static void setNumericTextWatcher(NumericInputInteractionView view, InverseBindingListener textAttrChanged) {
    androidx.databinding.adapters.TextViewBindingAdapter.setTextWatcher(view, null, null, null, textAttrChanged);
  }

  @BindingAdapter(value = {"android:textAttrChanged"}, requireAll = false)
  public static void setRatioTextWatcher(RatioInputInteractionView view, InverseBindingListener textAttrChanged) {
    androidx.databinding.adapters.TextViewBindingAdapter.setTextWatcher(view, null, null, null, textAttrChanged);
  }

  @BindingAdapter(value = {"android:textAttrChanged"}, requireAll = false)
  public static void setTextInputTextWatcher(TextInputInteractionView view, InverseBindingListener textAttrChanged) {
    androidx.databinding.adapters.TextViewBindingAdapter.setTextWatcher(view, null, null, null, textAttrChanged);
  }

  @InverseBindingAdapter(attribute = "android:text")
  public static CharSequence getEditTextText(EditText view) {
    CharSequence text = view.getText();
    return text != null ? text : "";
  }

  @InverseBindingAdapter(attribute = "android:text")
  public static String getAppCompatEditTextText(androidx.appcompat.widget.AppCompatEditText view) {
    CharSequence text = view.getText();
    return text != null ? text.toString() : "";
  }

  @InverseBindingAdapter(attribute = "android:text")
  public static CharSequence getFractionText(FractionInputInteractionView view) {
    CharSequence text = view.getText();
    return text != null ? text : "";
  }

  @InverseBindingAdapter(attribute = "android:text")
  public static CharSequence getMathExpressionText(MathExpressionInteractionsView view) {
    CharSequence text = view.getText();
    return text != null ? text : "";
  }

  @InverseBindingAdapter(attribute = "android:text")
  public static CharSequence getNumericInputText(NumericInputInteractionView view) {
    CharSequence text = view.getText();
    return text != null ? text : "";
  }

  @InverseBindingAdapter(attribute = "android:text")
  public static CharSequence getRatioInputText(RatioInputInteractionView view) {
    CharSequence text = view.getText();
    return text != null ? text : "";
  }

  @InverseBindingAdapter(attribute = "android:text")
  public static CharSequence getTextInputText(TextInputInteractionView view) {
    CharSequence text = view.getText();
    return text != null ? text : "";
  }
}
