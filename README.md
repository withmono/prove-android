# Mono Prove Android SDK

The Mono Prove SDK is a quick and secure way to onboard your users from within your Android app. Mono Prove is a customer onboarding product that offers businesses faster customer onboarding and prevents fraudulent sign-ups, powered by the MDN and facial recognition technology.

For accessing customer accounts and interacting with Mono's API (Identity, Transactions, Income, DirectPay) use the server-side [Mono API](https://docs.mono.co/docs).

## Documentation

For complete information about Mono Prove, head to the [docs](https://docs.mono.co/docs).

## Getting Started

1. Register on the [Mono](https://app.mono.com) website and get your public and secret keys.
2. Retrieve a `sessionId` for a customer by calling the [initiate endpoint](https://docs.mono.co/api)

## Requirements

- Java 8 or higher
- The latest version of the Mono Prove Android SDK

### Installation Guides
Follow the integrations guides for [Jetpack Compose](#jetpack-compose), [Kotlin Language](#kotlin-integration), and [Java Language](#java-integration).

### Jetpack Compose
## Installation
### Set up dependencies

Make sure your `build.gradle` files are set up as follows:

In build.gradle (app module):
```gradle
android {
// Rest of the code
    kotlinOptions {
        jvmTarget = '1.8'
    }
    buildFeatures {
        compose true
    }
    composeOptions {
        kotlinCompilerExtensionVersion '1.4.1'
    }
}
```
In build.gradle (project level):
```gradle
buildscript {
    ext {
        compose_version = '1.4.1'
    }
}

plugins {
// Rest of the code
    id 'org.jetbrains.kotlin.android' version '2.0.0' apply false
}
```
In the `settings.gradle` file, add the following:
```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

And in the `dependencies` section of the `build.gradle` file, add the following:

```gradle
implementation 'com.github.withmono:mono-prove-android:v1.0.0'
```

## Usage

### Add ProveKitActivity to the manifest

Add the following code to your AndroidManifest.xml file:
```xml
<application>
    <!-- Rest of the code -->
    <activity
        android:name="mono.prove.kit.ProveKitActivity"
        android:theme="@style/YourAppTheme" />
</application>
```
Add the Material theme to your app theme:
```xml
<resources>
<style name="YourAppTheme" parent="Theme.MaterialComponents.DayNight.NoActionBar">
    <!-- Customize your theme here. -->
</style>
</resources>
```

### Add the Mono Prove SDK to your app

Add the following code to your Jetpack Compose activity file:

```kotlin
@Composable
fun ProveKitSample() {
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

  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Button(onClick = { mProveKit.show() }) { Text(text = "Launch Widget") }
  }
}

```

## Configuration Options

- [`sesssionId`](#sesssionId)
- [`onSuccess`](#onSuccess)
- [`onClose`](#onClose)
- [`onEvent`](#onEvent)
- [`reference`](#reference)

### <a name="sesssionId"></a> `sesssionId`
**String: Required**

This is the session ID returned after calling the [initiate endpoint](https://docs.mono.co/api).

```kotlin
val config =
  ProveConfiguration.Builder(
      context,
      "PRV...", // sessionId
    ) {
      println("Successfully verified.")
    } // onSuccess function
    .build()

```

### <a name="onSuccess"></a> `onSuccess`
**() -> Unit: Required**

The closure is called when a user has successfully verified their identity.

```kotlin
val config =
  ProveConfiguration.Builder(
      context,
      "PRV...", // sessionId
    ) {
      println("Successfully verified.")
    } // onSuccess function
    .build()

```

### <a name="onClose"></a> `onClose`
**() -> Unit: Optional**

The optional closure is called when a user has specifically exited the Mono Prove flow. It does not take any arguments.

```kotlin
val config =
  ProveConfiguration.Builder(
      context,
      "PRV...", // sessionId
    ) {
      println("Successfully verified.")
    } // onSuccess function
    .addOnClose { println("Widget closed.") } // onClose function
    .build()

```

### <a name="onEvent"></a> `onEvent`
**(ProveEvent event) -> Unit: Optional**

This optional closure is called when certain events in the Mono Prove flow have occurred, for example, when the user opens or closes the widget. This enables your application to gain further insight into what is going on as the user goes through the Mono Prove flow.

See the [ProveEvent](#ProveEvent) object below for details.

```kotlin
val config =
  ProveConfiguration.Builder(
      context,
      "PRV...", // sessionId
    ) {
      println("Successfully verified.")
    } // onSuccess function
    .addOnEvent { event -> println("Triggered: ${event.eventName}") } // onEvent function
    .build()

```

### <a name="reference"></a> `reference`
**String: Optional**

When passing a reference to the configuration it will be passed back on all onEvent calls.

```kotlin
val config =
  ProveConfiguration.Builder(
      context,
      "PRV...", // sessionId
    ) {
      println("Successfully verified.")
    } // onSuccess function
    .addReference("random_string")
    .build()

```

## API Reference

### Prove Object

The Prove Object exposes the `Prove.create(config: ProveConfiguration)` method that takes a [ProveConfiguration](#ProveConfiguration) for easy interaction with the Mono Prove Widget.

### <a name="ProveConfiguration"></a> ProveConfiguration

The configuration option is passed to Prove.create(config: ProveConfiguration).

```kotlin
sessionId: String // required
context: Context // required
onSuccesss: () -> Unit // required
onClose: () -> Unit // optional
onEvent: (ProveEvent event) -> Unit // optional
reference: String // optional
```
#### Usage

```kotlin
val config =
  ProveConfiguration.Builder(context, "PRV...") { println("Successfully verified.") }
    .addReference("testref")
    .addOnEvent { event ->
      println("Triggered: ${event.eventName}")
      if (event.data.has("reference")) {
        println("ref: ${event.data.getString("reference")}")
      }
    }
    .addOnClose { println("Widget closed.") }
    .build()

````

### <a name="proveEvent"></a> ProveEvent

#### <a name="eventName"></a> `eventName: String`

Event names correspond to the `type` key returned by the event data. Possible options are in the table below.

| Event Name           | Description                                                   |
|----------------------|---------------------------------------------------------------|
| OPENED               | Triggered when the user opens the Prove Widget.               |
| CLOSED               | Triggered when the user closes the Prove Widget.              |
| IDENTITY_VERIFIED     | Triggered when the user successfully verifies their identity. |
| ERROR                | Triggered when the widget reports an error.                   |


#### <a name="dataObject"></a> `data: JSONObject`

The data object of type JSONObject returned from the onEvent callback. You can access any property by `event.data.get("PROPERTY_NAME")` then casting it to the corresponding type.

```kotlin
type: String // type of event mono.prove.xxxx
reference: String? // reference passed through the prove config
pageName: String? // name of page the widget exited on
errorType: String? // error thrown by widget
errorMessage: String? // error message describing the error
timestamp: int // unix timestamp of the event as an Integer
```

### Kotlin Integration
## Installation

There are two options to add the Mono Prove Android Kotlin SDK to your project:

Option 1: Add the following to your project's build.gradle file:

```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation 'com.github.withmono:mono-prove-android:v1.0.0'
}
```

Option 2: Add the following to your project's settings.gradle file:

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation 'com.github.withmono:mono-prove-android:v1.0.0'
}
```

## Usage

```kotlin
private lateinit var mProveKit: ProveKit

private fun setup() {
  // replace your session id in strings.xml
  val sessionId = context.getString(R.string.prove_session_id)

  val config =
    ProveConfiguration.Builder(context, sessionId) { println("Successfully verified.") }
      .addReference("testref")
      .addOnEvent { event ->
        println("Triggered: ${event.eventName}")
        if (event.data.has("reference")) {
          println("ref: ${event.data.getString("reference")}")
        }
      }
      .addOnClose { println("Widget closed.") }
      .build()

  mProveKit = Prove.create(config)
}
```

Call the show() method of the ProveKit instance to launch the Mono Prove widget:

```kotlin
val onClickListener = View.OnClickListener { mProveKit.show() }

findViewById<View>(ButtonViewID).setOnClickListener(onClickListener)

```


### Java Integration
## Installation

### Gradle

```sh
build.gradle

allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```

```sh
dependencies {
  implementation 'com.github.withmono:mono-prove-android:v1.0.0'
}
```

## Usage

#### Import ProveKit
```java
import mono.prove.kit.*;
```

#### Create a ProveConfiguration
```java
ProveConfiguration config = new ProveConfiguration.Builder(this,
  "PRV...", // your sessionId
  () -> {
    System.out.println("Successfully verified identity.");
  }) // onSuccess function
  .addReference("testref")
  .addOnEvent((event) -> {
    System.out.println("Triggered: " + event.getEventName());
  }) // onEvent function
  .addOnClose(() -> {
    System.out.println("Widget closed.");
  }) // onClose function
  .build();
```

#### Initialize a Mono Prove Widget
```java
ProveKit widget = Prove.create(config);
```

#### Show the Widget
```java
View.OnClickListener onClickListener = new View.OnClickListener() {
  @Override
  public void onClick(View v) {
    widget.show();
  }
};

findViewById(ButtonViewID).setOnClickListener(onClickListener);
```

## Support
If you're having general trouble with Mono Prove Android SDK or your Mono integration, please reach out to us at <support@mono.co> or come chat with us on Slack. We're proud of our level of service, and we're more than happy to help you out with your integration to Mono.

## Contributing
If you would like to contribute to the Mono Prove Android SDK, please make sure to read our [contributor guidelines](https://github.com/withmono/prove-android/tree/main/CONTRIBUTING.md).


## License

[MIT](https://github.com/withmono/prove-android/tree/main/LICENSE) for more information.