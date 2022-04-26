package org.oppia.android.instrumentation.profile

/** Tests for Profile. */
class AddProfileActivityTest {
  private lateinit var device: UiDevice

  @Before
  fun setUp() {
    // Initialize UiDevice instance
    device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    device.startOppiaFromScratch()
  }

  @Test
  fun testAddANewProfile() {
  }

  private fun navigateToAddProfileActivity() {
      device.findObjectByRes("skip_text_view").click()
      device.findObjectByRes("get_started_button").click()
      device.waitForRes("profile_select_text")
      device.findObjectByRes("profile_add_button").click()
    }
}
