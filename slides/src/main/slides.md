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
  Example of how <b>Layout</b> ends up emitting a change to the table.
</div>

```kotlin
@Composable inline fun Layout(...) {
    emit<LayoutNode, Applier<Any>>( // inserts the node
        ctor = LayoutEmitHelper.constructor, // LayoutNode constructor lambda
        update = { // initialize / update the node
            set(measureBlocks, LayoutEmitHelper.setMeasureBlocks)
            set(DensityAmbient.current, LayoutEmitHelper.setDensity)
            set(LayoutDirectionAmbient.current, LayoutEmitHelper.setLayoutDirection)
        },
        skippableUpdate = materializerOf(modifier),
        children = children
    )
}
```
<!-- .element: class="arrow" data-highlight-only="true" -->

* `emit` ultimately records the change of inserting a UI node via `recordApplierOperation` 👇

```kotlin
recordApplierOperation { applier, _, _ ->
    applier.insert(insertIndex, node)
    // ...
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

<div class="card">
  Old but remains equally useful to understand the <b>slot table in depth</b>.
</div>

* https://www.youtube.com/watch?v=6BRlI5zfCCk

<img style="margin-top:60px;" src="assets/ComposeDemistifiedTalk.jpg"/>

---

## Runtime 🏃

<img src="assets/The order of Runtime.png"/>

* Composition first 👉 <span class="blueText">Adds relevant data to the table</span>.
* Once done 👉 <span class="blueText">Apply all recorded changes</span> 📲
* Composables visible! 👉 time for <span class="blueText">Lifecycle events</span>.
* Lifecycle events triggered! 👉 <span class="blueText">time to run the SideEffects</span>.

* Recomposition required? 👉 <span class="blueText">back to step one</span> ⏫

---

## Runtime 🏃

<div class="card">
  What about <b>recomposition</b>?
</div>

* The Composer can <span class="blueText">discard pending compositions</span> when finding errors during the composition, and also smartly <span class="blueText">skip recomposition</span> via the <span class="blueText">RecomposerScope</span>.

```kotlin
@Composable
fun Counter($composer: Composer) {
  $composer.start()
  val count = remember($composer) { mutableStateOf(0) }

  Button($composer, onClick = { count.value += 1 }) {
    Text($composer, "Current count ${count.value}")
  }
  $composer.end()?.updateScope { nextComposer ->
    Counter(nextComposer)
  }
}
```
<!-- .element: class="arrow" data-highlight-only="true" -->

* Note the update block added after `composer.end()`.

---

## Runtime 🏃

* `updateScope` invoked by the Runtime <span class="blueText">for recomposition</span>.
* `$composer.end()` returns `null` if no observable model was read during the composition.
* In that case 👉 we can avoid recomposing! <span class="blueText">(not needed)</span>

```kotlin
@Composable
fun HelloWorld($composer: Composer) {
  $composer.start()

  Text($composer, "This is a hello world!")

  $composer.end()?.updateScope {
    HelloWorld(nextComposer)
  }
}
```
<!-- .element: class="arrow" data-highlight-only="true" -->

* All the boilerplate added is <span class="blueText">generated by the Compiler</span>.
* `RecomposeScope` provides the `updateScope` function and works via the <span class="blueText">Composer 👉 Recomposer</span>.

---

## Runtime 🏃

<div class="card">
 <b>Positional Memoization</b> when reading from the slot table.
</div>

* the Runtime can <span class="blueText">remember the result of a `@Composable` function call</span> and return it without computing it again whenever not required.
* Think of the <span class="blueText">`remember`</span> function.

```kotlin
@Composable
fun HelloWorld() {
  val count = remember { mutableStateOf("Default value") }
}
```
<!-- .element: class="arrow" data-highlight-only="true" -->

---

## Runtime 🏃

<div class="card">
  Composition & recomposition is intentionally <b>coupled to KotlinX Coroutines</b>.
</div>

* <span class="blueText">Can't replace</span> it unless you write your complete runtime 👉 Including everything we've described so far 😅
* Uses it for <span class="blueText">structured concurrency</span> 👉 Parallel recomposition, offload recomposition to different threads...
* Uses it for <span class="blueText">automatic Cancellation</span> in effect handlers ⏩
* But you can <span class="blueText">provide your own Applier<N> and node types</span> 👉 We'll show that ahead in this talk.

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
