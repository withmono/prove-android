package mono.prove.kit

import android.app.Activity
import android.util.Log
import android.webkit.JavascriptInterface
import java.lang.ref.WeakReference
import org.json.JSONException
import org.json.JSONObject

class ProveWebInterface private constructor() {
  private var onSuccess: ProveSuccessCallback? = null
  private var onClose: ProveCloseCallback? = null
  private var onEvent: ProveEventCallback? = null
  private var reference: String? = null
  private var mActivityRef: WeakReference<Activity>? = null

  @JavascriptInterface
  @Throws(JSONException::class)
  fun postMessage(message: String) {
    try {
      val event = Event.fromString(message)
      val proveEvent = ProveEvent.fromString(message)

      Log.d("Type", event.type)

      onEvent?.run(proveEvent)

      val activity = mActivityRef?.get()
      when (event.type) {
        "mono.prove.widget.closed" -> {
          onClose?.run()
          activity?.finish()
        }

        "mono.prove.identity_verified" -> {
          onSuccess?.let { callback ->
            callback.run()

            // Trigger SUCCESS event
            val data = JSONObject()
            val unixTime = System.currentTimeMillis() / 1000L
            try {
              data.put("timestamp", unixTime)
              val successEvent = ProveEvent("SUCCESS", data)
              getInstance().triggerEvent(successEvent)
              onClose?.run()
              activity?.finish()
            } catch (e: JSONException) {
              e.printStackTrace()
            }
          }
          activity?.finish()
        }
      }
    } catch (e: JSONException) {
      Log.e("ProveWebInterface", "Failed to parse message: $message", e)
    }
  }

  fun setActivity(activity: Activity) {
    mActivityRef = WeakReference(activity)
  }

  fun setOnSuccess(callback: ProveSuccessCallback) {
    onSuccess = callback
  }

  fun getOnSuccess(): ProveSuccessCallback? = onSuccess

  fun setOnClose(callback: ProveCloseCallback) {
    onClose = callback
  }

  fun getOnClose(): ProveCloseCallback? = onClose

  fun setOnEvent(callback: ProveEventCallback) {
    onEvent = callback
  }

  fun getOnEvent(): ProveEventCallback? = onEvent

  fun setReference(ref: String) {
    reference = ref
  }

  fun getReference(): String? = reference

  @Throws(JSONException::class)
  fun triggerEvent(proveEvent: ProveEvent) {
    onEvent?.run(proveEvent)
  }

  companion object {
    @Volatile private var instance: ProveWebInterface? = null

    fun getInstance(): ProveWebInterface {
      return instance
        ?: synchronized(this) { instance ?: ProveWebInterface().also { instance = it } }
    }

    fun reset() {
      synchronized(this) { instance = ProveWebInterface() }
    }
  }
}
