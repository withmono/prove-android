package mono.prove.kit.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import mono.prove.kit.*
import mono.prove.kit.sample.ui.theme.MonoProveWidgetTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MonoProveWidgetTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          ProveKitSample(modifier = Modifier.padding(innerPadding))
        }
      }
    }
  }
}

@Composable
fun ProveKitSample(modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val sessionId = context.getString(R.string.prove_session_id)

  val config =
    ProveConfiguration.Builder(context, sessionId) { println("Successfully verified.") }
      .addReference("sometestref0")
      .addOnEvent { event ->
        println("Triggered: ${event.eventName}")
        if (event.data.has("reference")) {
          println("ref: ${event.data.getString("reference")}")
        }
      }
      .addOnClose { println("Widget closed.") }
      .build()

  val mProveKit = Prove.create(config)

  Surface(modifier) {
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Button(onClick = { mProveKit.show() }) { Text(text = "Launch Widget") }
    }
  }
}
