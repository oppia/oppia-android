// Replace getTypedSerializable helper method with BundleCompat calls
import android.os.Bundle
import androidx.core.os.BundleCompat

fun <T> Bundle.getTypedSerializable(key: String, clazz: Class<T>): T? {
    return BundleCompat.getSerializable(this, key, clazz)
}