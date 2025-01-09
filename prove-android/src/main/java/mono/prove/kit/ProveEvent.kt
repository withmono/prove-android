package mono.prove.kit

import org.json.JSONException
import org.json.JSONObject

class ProveEvent(val eventName: String, val data: JSONObject) {
  companion object {
    @Throws(JSONException::class)
    fun fromString(data: String): ProveEvent {
      val event = JSONObject(data)
      val type = event.getString("type")

      val name: String =
        when (type) {
          "mono.prove.widget_opened" -> "OPENED"
          "mono.prove.error_occurred" -> "ERROR"
          "mono.prove.widget.closed" -> "CLOSED"
          "mono.prove.identity_verified" -> "IDENTITY_VERIFIED"
          else -> "UNKNOWN"
        }

      val body: JSONObject =
        if (event.has("data")) {
          event.getJSONObject("data")
        } else {
          JSONObject()
        }

      return ProveEvent(name, body)
    }
  }
}
