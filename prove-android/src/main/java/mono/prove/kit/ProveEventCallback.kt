package mono.prove.kit

import org.json.JSONException

fun interface ProveEventCallback {
  @Throws(JSONException::class) fun run(event: ProveEvent)
}
