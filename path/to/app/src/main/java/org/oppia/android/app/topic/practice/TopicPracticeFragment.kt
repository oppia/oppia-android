// Replace Bundle calls with BundleCompat calls
import android.os.Bundle
import androidx.core.os.BundleCompat

class TopicPracticeFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Replace Bundle calls with BundleCompat calls
        val bundle = arguments
        val value = BundleCompat.getSerializable(bundle, "key", String::class.java)
        // ...
    }
}