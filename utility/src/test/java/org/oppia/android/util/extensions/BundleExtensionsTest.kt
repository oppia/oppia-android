package org.oppia.android.util.extensions

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class BundleExtensionsTest {

  // testGetProto_noProtoInBundle_returnsDefault
  // testPutProto_noProtoInBundle_addsDataToProto
  // testGetProto_protoInBundle_sameType_returnsCorrectProto
  // testGetProto_protoInBundle_sameType_defaultWithData_returnsUnmergedProto
  // testPutProto_protoAlreadyInBundle_overridesExistingProto
  // testGetProto_oldProtoInBundle_differentButCompatibleType_returnsInteroperableProto
  // testGetProto_protoInBundle_incompatibelType_throws
  // testPutProto_multipleProtos_eachCanBeRetrieved
}
