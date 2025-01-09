package mono.prove.kit

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.*
import android.webkit.WebView.setWebContentsDebuggingEnabled
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import org.json.JSONException
import org.json.JSONObject

class ProveKitActivity : AppCompatActivity() {
  private lateinit var url: String
  private val permissions = arrayOf(Manifest.permission.CAMERA)

  private val permissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
      val allGranted = result.values.all { it }
      if (allGranted) {
        initializeWebView()
      } else {
        Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show()
        finish()
      }
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Fetch the URL from the intent
    url = intent.getStringExtra(Constants.KEY_URL) ?: ""

    // Check and request permissions immediately
    checkPermissionsAndInitialize()
  }

  private fun checkPermissionsAndInitialize() {
    val deniedPermissions =
      permissions.filter {
        ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
      }
    if (deniedPermissions.isNotEmpty()) {
      permissionLauncher.launch(deniedPermissions.toTypedArray())
    } else {
      initializeWebView()
    }
  }

  private fun initializeWebView() {
    enableEdgeToEdge()
    setContent { ProveWidget(url, onLoadWebViewContent = { onLoadWebViewContent() }) }
  }

  private fun onLoadWebViewContent() {
    val data = JSONObject()
    val unixTime = System.currentTimeMillis() / 1000

    try {
      // Trigger OPENED event
      data.put("timestamp", unixTime)
      val reference: String? = ProveWebInterface.getInstance().getReference()

      if (reference != null) {
        data.put("reference", reference)
        data.put("type", "mono.prove.widget_opened")
      }
      val proveEvent = ProveEvent("OPENED", data)
      ProveWebInterface.getInstance().triggerEvent(proveEvent)
    } catch (e: JSONException) {
      e.printStackTrace()
    }
  }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ProveWidget(url: String, onLoadWebViewContent: () -> Unit) {
  val context = LocalContext.current
  val webView = remember { WebView(context) }
  var isLoading by remember { mutableStateOf(true) }

  LaunchedEffect(Unit) {
    // Configure WebView settings
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
      setWebContentsDebuggingEnabled(true)
    }

    // Set WebView client and Chrome client
    webView.webViewClient =
      object : WebViewClient() {
        override fun onPageStarted(
          view: WebView?,
          url: String?,
          favicon: android.graphics.Bitmap?,
        ) {
          super.onPageStarted(view, url, favicon)
          isLoading = true
        }

        override fun onPageFinished(view: WebView, url: String) {
          super.onPageFinished(view, url)
          isLoading = false

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
    val monoWebInterface = ProveWebInterface.getInstance()
    (context as? AppCompatActivity)?.let { monoWebInterface.setActivity(it) }
    webView.addJavascriptInterface(monoWebInterface, "MonoClientInterface")

    // Load the URL
    webView.loadUrl(url)

    onLoadWebViewContent()
  }

  Surface {
    Box(Modifier.fillMaxSize()) {
      // WebView content
      AndroidView(
        factory = {
          webView.apply {
            layoutParams =
              ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
              )
          }
        },
      )

      // Progress indicator overlay
      if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
      }
    }
  }
}
