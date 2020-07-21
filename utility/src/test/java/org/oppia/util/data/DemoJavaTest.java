package org.oppia.util.data;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.LooperMode;

import static com.google.common.truth.Truth.assertThat;

@RunWith(AndroidJUnit4.class)
@LooperMode(LooperMode.Mode.PAUSED)
public class DemoJavaTest {

  @Test
  public void testIsTrue() {
    boolean result = true;
    assertThat(result).isTrue();
  }

}
