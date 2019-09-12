package org.oppia.app.utility

import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Before

import org.junit.Assert.*
import org.junit.Test

class LoggerTest {

  var blockingDispatcher: CoroutineDispatcher? = null

  @Before
  fun setUp() {
    blockingDispatcher = Dispatchers.Default
  }

  @Test
  fun testlogger(){
    Logger(ApplicationProvider.getApplicationContext(), blockingDispatcher!!).e("Test","Test456")
  }

  @After
  fun tearDown() {
  }
}
