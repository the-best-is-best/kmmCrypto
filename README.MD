<h1 align="center">KMM Crypto</h1><br>

<div align="center">
<a href="https://opensource.org/licenses/Apache-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"/></a>
<a href="https://android-arsenal.com/api?level=21" rel="nofollow">
    <img alt="API" src="https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat" style="max-width: 100%;">
</a>
  <img src="https://img.shields.io/badge/Platform-Android-brightgreen.svg?logo=android" alt="Badge Android" />
  <img src="https://img.shields.io/badge/Platform-iOS%20%2F%20macOS-lightgrey.svg?logo=apple" alt="Badge iOS" />
  <img src="https://img.shields.io/badge/Platform-JVM-8A2BE2.svg?logo=openjdk" alt="Badge JVM" />
    <img src="https://img.shields.io/badge/Platform-WASM%20%2F%20JS-yellow.svg?logo=javascript" alt="Badge JS" />
<a href="https://github.com/the-best-is-best/"><img alt="Profile" src="https://img.shields.io/badge/github-%23181717.svg?&style=for-the-badge&logo=github&logoColor=white" height="20"/></a>
</div>

### This package for encrypt or decrypt data in kotlin multiplatform

<hr>

[![Maven Central](https://img.shields.io/maven-central/v/io.github.the-best-is-best/kmm-crypto)](https://central.sonatype.com/artifact/io.github.the-best-is-best/kmm-crypto)

KMM Crypto is available on `mavenCentral()`.

```kotlin
implementation("io.github.the-best-is-best:kmm-crypto:1.2.0")
```

## Important Note

🔹 Starting from version `1.1.4`, there is **no need to add** `pod 'KServices'` in the `Podfile`. The
package will work automatically on iOS.

## How to use it

### First in androidMain

```kotlin
AndroidKMMCrypto.init(this, "key0") // add alias key name
```

### Second how in common make save data

```kotlin
val kmmCrypto = KMMCrypto()
kmmCrypto.saveData(
    "test",
    "group",
    "Any data here"
)
```

### Third how in common load data

```kotlin
val scope = rememberCoroutineScope()
val kmmCrypto = KMMCrypto()
scope.launch {
    val data = kmmCrypto.loadData("test", "group")
}
```

### Third how in common delete data

```kotlin
val scope = rememberCoroutineScope()
val kmmCrypto = KMMCrypto()
scope.launch {
    kmmCrypto.deleteData("test", "group")
}
```

