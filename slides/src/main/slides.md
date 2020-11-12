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

## Compiler plugin ⚙

<div class="card">
  Generates metadata to satisfy the <b>Runtime</b> needs.
</div>

* Kotlin compiler plugin Targeting Kotlin <span class="blueText">1.4 IR</span>.
* Scans for all <span class="blueText">`@Composable`</span> functions.
* Rewrites them (generate convenient IR) to include relevant info when called 👉 (<span class="blueText">`Composer`</span> and unique keys).
* Unlocks runtime optimizations.
* <span class="blueText">*(Smart recomposition, offloading compositions to different threads, avoiding extra boilerplate for "Stable" apis... etc)*</span>

---

## Compiler plugin ⚙

<div class="card">
  The Composer will drive the composition <b>at runtime</b>.
</div>

<img src="assets/Compose Compiler.png"/>

---

## Compiler plugin ⚙

<div class="card">
    <b>Compiler generated unique keys</b> are also passed to all <b>@Composable</b> functions.
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

* But how do we know <span class="blueText">@Composable</span> functions are <span class="blueText">called from the right context</span>? 🤔

---

## Compiler checks ⚙

<div class="card">
    👇 Composable functions are similar to suspend functions 👇<br/><b>impose a calling context</b>.
</div>

* Requirement imposed by the Compiler <span class="blueText">frontend</span> phase <span class="yellowText">(static checks)</span> 👉 fast feedback loop.
* Can <span class="blueText">only be called from other `@Composable` functions</span>
* Never have standard functions calling composable ones.
* Ensure a pure <span class="blueText">@Composable</span> function call stack.
* The compiler can make the <span class="blueText">Composer</span> available at all levels.

---

## Compiler ⚙

<div class="card">
    With this, our @Composable codebase would be <b>ready for the runtime</b>.
</div>

* All the required metadata has been added.

<br/>
<br/>
<h1>👍</h1>

---

## Runtime 🏃

<div class="card">
    The Compose runtime is <b>declarative</b>.
</div>

<img src="assets/Runtime concern separation.png"/>

* The interpreter has the big picture 👉 Can decide how to execute / optimize it.
* The interpreter is <span class="blueText">decoupled</span> from the program.

---

## Runtime 🏃

<div class="card">
    Creating the in-memory representation 👉 The <b>Slot table</b>
</div>

* Driven by the <span class="blueText">Composer</span> 👉 Built during composition.
* First composition 👉 Adds all nodes to the tree.
* Every recomposition 👉 Updates the table.
* Table interpreted later on 👉 <span class="blueText">materializes UI</span> on screen 📱

<img src="assets/Runtime concern separation 2.png"/>

* But does it need to be UI? 🤔

---

## Runtime 🏃

<div class="card">
    <b>No!</b> Compose runtime works with <b>generic nodes</b> of type <b>N</b>.
</div>

* Slot table 👉 generic node structure.
* While reading our composable function tree 👉 Composer <span class="blueText">adds, removes, replaces, or moves</span> nodes based on our logics <span class="yellowText">(think of conditional logics)</span>.
* Those operations are represented by <span class="blueText">emitting</span> changes over the table.

```kotlin
internal typealias Change<N> = (
    applier: Applier<N>,
    slots: SlotWriter,
    lifecycleManager: LifecycleManager
) -> Unit
```
<!-- .element: class="arrow" data-highlight-only="true" -->

---

## Runtime 🏃

```kotlin
internal typealias Change<N> = (
    applier: Applier<N>, // interpreter interface we can implement 👉 materializes changes
    slots: SlotWriter, // writes changes to the table when the time comes
    lifecycleManager: LifecycleManager // side effects and lifecycle events are also recorded!
) -> Unit
```
<!-- .element: class="arrow" data-highlight-only="true" -->

* Lifecycle events of a <span class="blueText">@Composable</span> can be entering / leaving the composition, and also `SideEffects` 👇

```kotlin
internal interface LifecycleManager {
    fun entering(instance: CompositionLifecycleObserver)
    fun leaving(instance: CompositionLifecycleObserver)
    fun sideEffect(effect: () -> Unit)
}
```
<!-- .element: class="arrow" data-highlight-only="true" -->

---

## Runtime 🏃

<div class="card">
  What <b>types of nodes</b> can we have on the table?
</div>

* Literally anything driven by a <span class="blueText">@Composable function</span>.
* <span class="blueText">UI nodes</span> (like LayoutNodes).
* <span class="blueText">State</span> nodes (by composable functions like remember).
* <span class="blueText">Composable function calls</span> are also recorded as nodes.
* <span class="blueText">Providers and Ambients</span> (they're also composable functions).
* <span class="blueText">SideEffects</span>.

<div class="card">
  Any relevant data required to <b>materialize UI for a snapshot in time</b>.
</div>

---

## Runtime 🏃

<img src="assets/The order of Runtime.png"/>

* Composition first 👉 <span class="blueText">Adds relevant data to the table</span>.
* Once done 👉 <span class="blueText">Apply all recorded changes</span> 📲
* Composables visible! 👉 time for <span class="blueText">Lifecycle events</span>.
* Lifecycle events triggered! 👉 <span class="blueText">time to run the SideEffects</span>.

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

---

## Thank you! 🙌

<div>
  <img style="vertical-align:middle;width:140px;margin-right:20px;" src="/assets/twitter_logo.jpg"/>
  <h2 style="display:inline;"><span style=""><u>@JorgeCastilloPr</u></span></h2>
</div>

<img src="/assets/jetpack-compose.svg"/>
