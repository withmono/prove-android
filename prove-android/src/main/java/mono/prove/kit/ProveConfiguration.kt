package mono.prove.kit

import android.content.Context

data class ProveConfiguration(
  val sessionId: String,
  val context: Context,
  val onSuccess: ProveSuccessCallback,
  val reference: String? = null,
  val onClose: ProveCloseCallback? = null,
  val onEvent: ProveEventCallback? = null,
) {
  class Builder(
    private val context: Context,
    private val sessionId: String,
    private val onSuccess: ProveSuccessCallback,
  ) {
    private var reference: String? = null
    private var onClose: ProveCloseCallback? = null
    private var onEvent: ProveEventCallback? = null

    fun addReference(reference: String?) = apply { this.reference = reference }

    fun addOnClose(onClose: ProveCloseCallback?) = apply { this.onClose = onClose }

    fun addOnEvent(onEvent: ProveEventCallback?) = apply { this.onEvent = onEvent }

    fun build(): ProveConfiguration {
      return ProveConfiguration(
        sessionId = sessionId,
        context = context,
        onSuccess = onSuccess,
        reference = reference,
        onClose = onClose,
        onEvent = onEvent,
      )
    }
  }
}
