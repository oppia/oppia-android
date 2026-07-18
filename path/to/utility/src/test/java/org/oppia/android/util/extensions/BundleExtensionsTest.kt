// Update test to use type check to verify exact type
import android.os.Bundle
import androidx.core.os.BundleCompat
import org.junit.Assert.assertThat
import org.junit.Test

class BundleExtensionsTest {
    @Test
    fun testGetTypedSerializable() {
        val bundle = Bundle()
        bundle.putString("key", "value")
        val value = BundleCompat.getSerializable(bundle, "key", String::class.java)
        assertThat(value).isInstanceOf(String::class.java)
    }
}