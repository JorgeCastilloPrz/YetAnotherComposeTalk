## Compose Architecture

<div class="card">
    The Jetpack Compose <b>libraries</b>
</div>

* Compiler plugin - <span class="blueText">compose.compiler</span>
* Runtime - <span class="blueText">compose.runtime</span>
* UI - <span class="blueText">compose.ui</span>
* Material - <span class="blueText">compose.material</span>
* Foundation - <span class="blueText">compose.foundation</span>
* Animation - <span class="blueText">compose.animation</span>
* UI Tooling - <span class="blueText">androidx.ui</span>

---

## Compiler plugin

<div class="card">
  Decorates <b>@Composable</b> functions based on the <b>Runtime</b> needs.
</div>

* Scans for all <span class="blueText">`@Composable`</span> functions.
* Rewrites them (generate convenient IR) to include relevant info when called 👉 (<span class="blueText">`Composer`</span> and unique keys).
* Targets Kotlin 1.4 IR.
* Enables runtime optimizations.

---

## Compiler plugin

<div class="card">
    Passes <b>Composer</b> and compiler generated unique keys to all <b>@Composable</b> functions.
</div>

```kotlin
@Composable
fun MyComposable($composer: Composer, $key: Int) {
  val count = remember($composer, 123) { mutableStateOf(0) }

  Button($composer, 456, onClick = { count.value += 1 }) {
    Text($composer, "Current count ${count.value}")
  }
}
```
<!-- .element: class="arrow" data-highlight-only="true" -->

* `@Composable` is the **calling context**.
* Ensures `Composer` will be available.

---

## Composable functions

<div class="card">
    Similar to <b>suspend</b> functions.
</div>

* Can only be called from other <span class="blueText">`@Composable`</span> functions.
* Require a calling context 👉 Composer.
* Composer 👉 part of the Runtime, drives composition / recomposition.

---

## Concern separation

<img src="assets/algebrasvsruntime.png"/>

* Memory representation of the program (algebras).
* Decoupled runtime - optimizations.
* <span style="color:#64b5f6;">Simple example: Kotlin `Sequences` 👉 terminal ops to consume - `toList()`</span>

---

## Another example of this?

<div class="card">
    <b>Jetpack Compose</b> 😲
</div>

<img src="/assets/jetpack-compose.svg"/>

---

<div>
  <img style="vertical-align:middle;width:120px;" src="/assets/jetpack-compose.svg"/>
  <h2 style="display:inline;"><span style="">Algebras</span></h2>
</div>

<div class="card">
    The composition tree 🌲
</div>

* In-memory description of the composition.
* Built by composable functions.

---

<div>
  <img style="vertical-align:middle;width:120px;" src="/assets/jetpack-compose.svg"/>
  <h2 style="display:inline;"><span style="">Runtime</span></h2>
</div>

<div class="card">
  All driven by the <b>Slot Table</b>.
</div>

* Slot table is created during execution.
* The **Applier** constructs and maintains the output composable tree.
* The runtime **heavily depends on kotlinx.coroutines**.
* 🎬 The Compose Runtime, Demystified - by @intellijibabble

---

<div>
  <img style="vertical-align:middle;width:120px;" src="/assets/jetpack-compose.svg"/>
  <h2 style="display:inline;"><span style="">Runtime</span></h2>
</div>

<span style="color:#64b5f6;">*(Offload compositions to arbitrary threads, run those in parallel, different order, smart recomposition...).*</span>

---

## Composable functions

<div class="card">
    Similar to <b>suspend</b> functions 🍃
</div>

* <span style="color:#64b5f6;">Description of a UI effect</span>.
* Callable from within other composable functions or a prepared **environment** 👉 integration point 👉 `setContent {}`
* Ensures a `Composer` object can be implicitly passed (contains the Slot table).
* <span style="color:#64b5f6;">Enforces a usage scope</span> to keep control over it.
* Makes the effect <span style="color:#64b5f6;">compile time tracked</span>.

---

## Suspend functions

<div class="card">
    This is how <b>suspend</b> works 🍃.
</div>

* <span style="color:#64b5f6;">Description of an effect</span> 🌀 (Not only UI).
* Callable from within other suspend functions or a prepared **environment** 👉 integration point 👉 coroutine.
* Ensures a `Continuation` can be implicitly passed.
* <span style="color:#64b5f6;">Enforces a usage scope</span> to keep control over it.
* Makes the effect <span style="color:#64b5f6;">compile time tracked</span>.

---

## Composable functions

<div class="card">
    Some extra capabilities.
</div>

* Allows memoization based on what's stored in the slot table 👉 `remember {}`
* `state {}` 👉 `remember { State(initial() }`
* Recomposition 👉 re-invoked multiple times 👉 idempotent.

---

## Platform integration

<div class="card">
  Provided for Android by <b>compose-ui</b>.
</div>

```kotlin
class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    setContent { // integration point for Android
      AppTheme {
        MainContent() // composable tree
      }
    }
  }
}
```
<!-- .element: class="arrow" data-highlight-only="true" -->

* The integration point interprets the in-memory UI tree 👉 skia in Android.
* Totally decoupled from the runtime.

---

## Decoupled runtime

<div class="card">
  Decoupled from the node types used.
</div>

* Supports multiple tree and node types.
* Could analyze / compare in memory trees with nodes of any types, not only composable nodes.

---

## Effect handlers

<div class="card">
  Running effects <b>from composable functions</b>.
</div>

* Not recommended to run effects from composables 🤔
* They would run on every recomposition.
* <span style="color:#64b5f6;">Composable functions need to be pure (idempotent).</span>

---

## Effect handlers

<div class="card">
  Part of <b>compose-runtime</b>.
</div>

* Effect handlers to keep effects under control 👉 `suspend`.
* <span style="color:#64b5f6;">Tied to KotlinX Coroutines.</span>

---

## Effect handlers

<div class="card">
  Stored as description of effects in the Composer.
</div>

* The `Composer` is read only.
* Slot table updates are pushed to the `Composer` as deferred ops to be applied later.
* They're descriptions of effects.

---

## But we'll need a runtime

<div class="card">
    Every suspended program <b>requires an environment</b> (runtime) to run.
</div>

* Suspend makes our program <u>declarative</u> 👉 description of effects.
* Crawls the call stack up until the integration point 👉 coroutine launch.

---

## Environment in KotlinX

* KotlinX Coroutines builders 👉 launch, async.

```kotlin
class MyFragment: Fragment() {
  override fun onViewCreated(...) {
    /* ... */
    viewLifecycleOwner.lifecycleScope.launch {
      // suspended program 🍃
    }
  }
}
```
<!-- .element: class="arrow" data-highlight-only="true" -->
