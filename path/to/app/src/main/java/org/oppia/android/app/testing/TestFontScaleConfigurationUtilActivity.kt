// Replace Bundle calls with BundleCompat calls
import android.os.Bundle
import androidx.core.os.BundleCompat

class TestFontScaleConfigurationUtilActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Replace Bundle calls with BundleCompat calls
        val bundle = intent.extras
        val value = BundleCompat.getSerializable(bundle, "key", String::class.java)
        // ...
    }
}