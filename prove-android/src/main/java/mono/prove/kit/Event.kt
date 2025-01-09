package mono.prove.kit

import org.json.JSONException
import org.json.JSONObject

class Event(val type: String, private val data: JSONObject) {
  companion object {
    @Throws(JSONException::class)
    fun fromString(data: String): Event {
      val event = JSONObject(data)
      val type = event.getString("type")

      val body: JSONObject =
        if (event.has("data")) {
          event.getJSONObject("data")
        } else {
          JSONObject()
        }

      return Event(type, body)
    }
  }

  fun getData(): JSONObject = data
}
