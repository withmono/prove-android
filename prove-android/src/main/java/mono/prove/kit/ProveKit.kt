package mono.prove.kit

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

class ProveKit {
  private val sessionId: String
  private val context: Context

  private val params = mutableMapOf<String, String>()

  constructor(context: Context, sessionId: String) {
    this.context = context
    this.sessionId = sessionId
  }

  constructor(config: ProveConfiguration) {
    this.context = config.context
    this.sessionId = config.sessionId

    config.reference?.let {
      params[Constants.KEY_REFERENCE] = it
      ProveWebInterface.getInstance().setReference(it)
    }

    config.onSuccess.let { ProveWebInterface.getInstance().setOnSuccess(it) }

    config.onClose?.let { ProveWebInterface.getInstance().setOnClose(it) }

    config.onEvent?.let { ProveWebInterface.getInstance().setOnEvent(it) }
  }

  private fun startWidgetActivity() {
    if (ProveWebInterface.getInstance().getOnSuccess() == null) {
      Log.e(Constants.TAG, "onSuccess can't be null")
      return
    }

    val intent =
      Intent(context, ProveKitActivity::class.java).apply { putExtra(Constants.KEY_URL, getUrl()) }
    context.startActivity(intent)
  }

  fun show() {
    startWidgetActivity()
  }

  private fun getUrl(): String {
    val builder =
      Uri.Builder().apply {
        scheme(Constants.URL_SCHEME)
        authority(Constants.PROVE_URL)
        path("/$sessionId")

        params.forEach { (key, value) -> appendQueryParameter(key, value) }
      }

    return builder.build().toString()
  }
}
