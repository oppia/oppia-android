package org.oppia.domain.exploration

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.app.model.Exploration
import org.oppia.util.data.AsyncResult
import java.io.IOException
import android.content.res.AssetManager
import androidx.annotation.Nullable
import org.json.JSONObject



const val TEST_EXPLORATION_ID_0 = "test_exp_id_0"
const val TEST_EXPLORATION_ID_1 = "test_exp_id_1"

private val EVICTION_TIME_MILLIS = TimeUnit.DAYS.toMillis(1)

/** Controller for retrieving an exploration. */
@Singleton
class ExplorationDataController @Inject constructor(private val context: Context) {
  /**
   * Returns an  [Exploration] given an ID.
   */
  fun getExplorationById(ID: String): LiveData<AsyncResult<Exploration>> {
    return MutableLiveData(AsyncResult.success(createExploration(ID)))
  }

  private fun createExploration(ID: String): Exploration {
    return if (ID == TEST_EXPLORATION_ID_0) createExploration0() else createExploration1()
    }

  // Returns the "welcome" exploration
  private fun createExploration0(): Exploration {
    val welcomeObject = loadJSONFromAsset("welcome.json")
    return Exploration.newBuilder()
     // Add fields
      .build()
  }

  private fun createExploration1(): Exploration {
    val aboutOppiaObject = loadJSONFromAsset("about_oppia.json")
    return Exploration.newBuilder()
     // Add fields
      .build()
  }

  @Nullable
  fun loadJSONFromAsset(assetName: String): JSONObject? {
    val am = context.assets

    var jsonObject: JSONObject?
    try {
      val `is` = am.open(assetName)
      val size = `is`.available()
      val buffer = ByteArray(size)
      `is`.read(buffer)
      `is`.close()
      val json = String(buffer, Charsets.UTF_8)
      jsonObject = JSONObject(json)
    } catch (ex: IOException) {
      ex.printStackTrace()
      return null
    }

    return jsonObject
  }
}
