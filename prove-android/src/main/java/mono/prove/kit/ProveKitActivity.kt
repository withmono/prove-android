package mono.prove.kit

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.*
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.json.JSONException
import org.json.JSONObject

class ProveKitActivity : AppCompatActivity() {
  private lateinit var webView: WebView
  private lateinit var loader: ProgressBar
  private lateinit var progressContainer: View

  private val permissions = arrayOf(Manifest.permission.CAMERA)
  private val requestCode = 0
  private lateinit var url: String

  override fun onCreate(savedInstanceState: Bundle?) {
    supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
    window.setFlags(
      WindowManager.LayoutParams.FLAG_FULLSCREEN,
      WindowManager.LayoutParams.FLAG_FULLSCREEN,
    )

    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)

    setup()
  }

  private fun isPermissionGranted(): Boolean {
    return permissions.all {
      ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }
  }

  private fun askPermissions() {
    ActivityCompat.requestPermissions(this, permissions, requestCode)
  }

  @SuppressLint("SetJavaScriptEnabled")
  private fun setup() {
    webView = findViewById(R.id.prove_web_view)
    loader = findViewById(R.id.prove_loader)
    progressContainer = findViewById(R.id.progress_container)

    url = intent.getStringExtra(Constants.KEY_URL) ?: ""

    webView.settings.apply {
      javaScriptEnabled = true
      loadWithOverviewMode = true
      useWideViewPort = true
      domStorageEnabled = true
      allowContentAccess = true
      allowFileAccess = true
      allowFileAccessFromFileURLs = true
      allowUniversalAccessFromFileURLs = true
      javaScriptCanOpenWindowsAutomatically = true
      builtInZoomControls = true
      mediaPlaybackRequiresUserGesture = true
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        safeBrowsingEnabled = true
      }
      setSupportZoom(true)
    }

    webView.webViewClient =
      object : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
          progressContainer.visibility = View.GONE
          loader.visibility = View.GONE

          // Trigger OPENED event
          val data = JSONObject()
          val unixTime = System.currentTimeMillis() / 1000

          try {
            data.put("timestamp", unixTime)
            val openedEvent = ProveEvent("OPENED", data)
            ProveWebInterface.getInstance().triggerEvent(openedEvent)
          } catch (e: JSONException) {
            e.printStackTrace()
          }
        }

        override fun shouldOverrideUrlLoading(
          view: WebView?,
          request: WebResourceRequest?,
        ): Boolean {
          return true
        }
      }

    webView.webChromeClient =
      object : WebChromeClient() {
        override fun onPermissionRequest(request: PermissionRequest) {
          request.grant(request.resources)
        }
      }

    // Add JavaScript interface
    val webInterface = ProveWebInterface.getInstance().apply { setActivity(this@ProveKitActivity) }
    webView.addJavascriptInterface(webInterface, "MonoClientInterface")

    if (!isPermissionGranted()) {
      askPermissions()
    } else {
      webView.loadUrl(url)
    }

    // Trigger OPENED event
    val data = JSONObject()
    val unixTime = System.currentTimeMillis() / 1000
    try {
      data.put("timestamp", unixTime)
      val reference = ProveWebInterface.getInstance().getReference()
      reference?.let {
        data.put("reference", it)
        data.put("type", "mono.prove.widget_opened")
      }
      val event = ProveEvent("OPENED", data)
      ProveWebInterface.getInstance().triggerEvent(event)
    } catch (e: JSONException) {
      e.printStackTrace()
    }
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray,
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == this.requestCode) {
      if (
        grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
      ) {
        webView.loadUrl(url)
      } else {
        Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show()
        finish()
      }
    }
  }
}
