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

## Compose compiler ⚙

<div class="card">
  Generates metadata to satisfy the <b>Runtime</b> needs.
</div>

* Kotlin compiler plugin Targeting Kotlin <span class="blueText">1.4 IR</span>.
* Scans for all <span class="blueText">`@Composable`</span> functions.
* Rewrites them (generate convenient IR) to include relevant info when called.
* Unlocks runtime optimizations.
* <span class="blueText">*(Smart recomposition, offload compositions to different threads, avoid redundant boilerplate for "Stable" apis...)*</span>

---

## Compose compiler ⚙

<img src="assets/Compose Compiler.png"/>

<div class="card">
  Composer will drive the composition / recomposition <b>at runtime</b>.
</div>

---

## Compose compiler ⚙

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

* How can we ensure the <span class="blueText">`Composer`</span> can be <span class="blueText">passed at all levels?</span>

---

## Compiler checks ⚙

<div class="card">
    👇 <b>Imposing a calling context</b> 👇
</div>

* Requirement imposed by the Compiler <span class="blueText">frontend</span> phase <span class="yellowText">(static checks)</span> 👉 fast feedback loop.
* Can <span class="blueText">only be called from other `@Composable` functions</span>.
* Standard functions can't call composable ones 🙅
* Ensure a pure <span class="blueText">@Composable</span> function call stack.
* The compiler can make the <span class="blueText">Composer</span> available at all levels.

---

## Compose compiler ⚙

<div class="card">
    With this, our @Composable codebase would be <b>ready for the runtime</b>.
</div>

* All the required metadata has been added.

<br/>
<br/>
<h1>👍</h1>

---

## Compose Runtime 🏃

<div class="card">
    The Compose runtime is <b>declarative</b>.
</div>

<img src="assets/Runtime concern separation.png"/>

* The interpreter has the big picture 👉 Can decide how to execute / optimize the program.
* The interpreter is <span class="blueText">decoupled</span> from the program.

---

## Compose Runtime 🏃

<div class="card">
    In-memory representation 👉 The <b>Slot table</b>
</div>

* Built by the <span class="blueText">Composer</span> 👉 during composition.
* First composition 👉 Adds nodes to the tree.
* Every recomposition 👉 Updates the table.
* Table interpreted later on 👉 <span class="blueText">materializes UI</span> on screen 📱

<img src="assets/Runtime concern separation 2.png"/>

* But does it need to be UI? 🤔

---

## Compose Runtime 🏃

<div class="card">
    <b>No!</b> Compose runtime works with <b>generic nodes</b> of type <b>N</b>.
</div>

* Slot table 👉 generic node structure.
* While reading our composable function tree 👉 Composer <span class="blueText">adds, removes, replaces, or moves</span> nodes based on our logics <span class="yellowText">(think of conditional logics)</span>.
* Those operations are represented by <span class="blueText">emitting</span> generic changes over the table.

```kotlin
internal typealias Change<N> // N is the chosen node type
```
<!-- .element: class="arrow" data-highlight-only="true" -->

---

## Compose Runtime 🏃

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

## Compose Runtime 🏃

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

## Compose Runtime 🏃

<div class="card">
  What <b>types of nodes</b> can we have on the table?
</div>

* Literally anything driven by a <span class="blueText">@Composable function</span>.
* <span class="blueText">UI nodes</span> (like LayoutNodes or DOMElements).
* <span class="blueText">State</span> nodes (by composable functions like remember).
* <span class="blueText">Composable function calls</span> are also recorded as nodes.
* <span class="blueText">Providers and Ambients</span> (they're also composable functions).
* <span class="blueText">SideEffects</span>.

<div class="card">
  Any relevant data required to <b>materialize UI for a snapshot in time</b>.
</div>

---

## Compose Runtime 🏃

<div class="card">
  Old but remains equally useful to understand the <b>slot table in depth</b>.
</div>

* https://www.youtube.com/watch?v=6BRlI5zfCCk

<img style="margin-top:60px;" src="assets/ComposeDemistifiedTalk.jpg"/>

---

## Compose Runtime 🏃

<img src="assets/The order of Runtime.png"/>

* Composition first 👉 <span class="blueText">Adds relevant data to the table</span>.
* Once done 👉 <span class="blueText">Apply all recorded changes</span> 📲
* Composables visible! 👉 time for <span class="blueText">Lifecycle events</span>.
* Lifecycle events triggered! 👉 <span class="blueText">time to run recorded SideEffects</span>.

* Recomposition required? 👉 <span class="blueText">back to step one</span> ⏫

---

## Compose Runtime 🏃

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

## Compose Runtime 🏃

* `updateScope` invoked by the Runtime <span class="blueText">for recomposition</span>.
* `$composer.end()` returns `null` if no observable model was read during the composition.
* In that case <span class="blueText">recomposition will not be needed.</span>

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

* All boilerplate is added <span class="blueText">by the Compiler</span>.
* `RecomposeScope` works via the <span class="blueText">Composer 👉 Recomposer</span>.

---

## Compose Runtime 🏃

<div class="card">
 <b>Positional Memoization</b> when reading from the slot table.
</div>

* The Runtime can <span class="blueText">remember the result of a `@Composable` function call</span> and return it without computing it again whenever not required.
* <span class="yellowText">Think of the `remember` function</span>.

```kotlin
@Composable
fun Modifier.verticalGradientScrim(color: Color, numStops: Int = 16): Modifier =
  composed {
    val colors = remember(color, numStops) { computeColors(color, numStops) }

    var height by remember { mutableStateOf(0f) }

    val brush = remember(color, numStops, startYPercentage, endYPercentage, height) {
      VerticalGradient(
        colors = colors,
        startY = height * startYPercentage,
        endY = height * endYPercentage
      )
    }

    drawBehind {
      height = size.height
      drawRect(brush = brush)
    }
  }
```
<!-- .element: class="arrow" data-highlight-only="true" -->

---

## Compose Runtime 🏃

<div class="card">
  Composition / recomposition intentionally <b>coupled to KotlinX Coroutines</b>.
</div>

* Uses it for <span class="blueText">structured concurrency</span> 👉 Parallel recomposition, offload recomposition to different threads...
* Uses it for <span class="blueText">automatic Cancellation</span> in effect handlers ⏩
* <span class="blueText">Can't replace</span> it unless you write your complete runtime 👉 Including everything we've seen so far 😅
* But you can <span class="blueText">provide your own Applier<N> and nodes</span> 👉 We'll show that ahead on this talk.

---

## Compose UI 📲

<div class="card">
  Materialize all our recorded changes <b>into ultimate Android UI</b>.
</div>

* This module <span class="blueText">bridges the gap between the Runtime and the chosen Platform</span>.
* The chosen <span class="blueText">`Applier<N>`</span> implementation does the job.
* Provides integration with the device: layout, drawing (skia), user input...

<img src="assets/Runtime concern separation 2.png"/>

---

## Compose UI 📲

<div class="card">
 <b>Other use cases?</b> 🤷 We could imagine a few.
</div>

* <span class="blueText">UI testing libs</span> that interpret changes by creating abstractions of the UI elements to assert over.
* Support <span class="blueText">other platforms like desktop</span> -> DOMElements for the nodes, DesktopApplier implementation for the bridging.
* <span class="blueText">Synchronizing an object graph</span> by serializing / sending diffs.
* <span class="blueText">Control hardware</span> 👉 minimize commands to reflect a change.
* Etc

---

## Compose UI 📲

<div class="card">
 Built-in Applier implementation for <b>Android</b>: The <b>UiApplier</b>.
</div>

* The <span class="blueText">Applier<N></span> is a visitor that <span class="blueText">visits</span> the whole node tree element by element.
* Supports <span class="blueText">both ViewGroups and Composable LayoutNodes</span>

```kotlin
class UiApplier(private val root: Any) : Applier<Any> {
    private val stack = Stack<Any>()
    private val pendingInserts = Stack<PendingInsert>()

    override var current: Any = root

    override fun down(node: Any) { // adds a node
        stack.push(current)
        current = node
    }

    override fun up() { // pops a node to materialize it
        val instance = current
        val parent = stack.pop()
        current = parent
        // ...
        if (pendingInserts.isNotEmpty()) {
            val pendingInsert = pendingInserts.peek()
            if (pendingInsert.instance == instance) {
                val index = pendingInsert.index
                pendingInserts.pop()
                when (parent) {
                    is ViewGroup ->
                        when (instance) {
                            is View -> {
                                adapter?.willInsert(instance, parent)
                                parent.addView(instance, index)
                                adapter?.didInsert(instance, parent)
                            }
                            is LayoutNode -> {
                                val composeView = AndroidComposeView(parent.context)
                                parent.addView(composeView, index)
                                composeView.root.insertAt(0, instance)
                            }
                            else -> invalidNode(instance)
                        }
                    is LayoutNode ->
                        when (instance) {
                            is View -> {
                                // Wrap the instance in an AndroidViewHolder, unless the instance
                                // itself is already one.
                                @OptIn(InternalInteropApi::class)
                                val androidViewHolder =
                                    if (instance is AndroidViewHolder) {
                                        instance
                                    } else {
                                        ViewBlockHolder<View>(instance.context).apply {
                                            view = instance
                                        }
                                    }

                                parent.insertAt(index, androidViewHolder.toLayoutNode())
                            }
                            is LayoutNode -> parent.insertAt(index, instance)
                            else -> invalidNode(instance)
                        }
                    else -> invalidNode(parent)
                }
                return
            }
        }
    }
    // ...
}
```
<!-- .element: class="arrow" data-highlight-only="true" -->

---

## Compose UI 📲

<div class="card">
  <b>LayoutNode</b> is an element in the Layout hierarchy <b>yielded by Compose UI</b>.
</div>

* <span class="blueText">Attached to an Owner</span> when materialized.
* Owner 👉 <span class="blueText">Connection with the View system</span> 👉 layout, input, drawing, accessibility...
* It <span class="blueText">keeps a reference to its parent</span> or the Owner when it's root.

<img style="margin-top:40px;" src="assets/LayoutNode hierarchy.png"/>

* It <span class="blueText">keeps a reference to its children</span>.
* It keeps track of all children <span class="blueText">measuring / placing</span> and measures itself.
* Requests remeasuring itself and its parent when attached / removed.

---

## Compose UI 📲

<div class="card">
  Compose UI gives us the basic piece of UI, the <b>Layout</b>.
</div>

* Any other existing <span class="blueText">@Composable</span> layouts are built using it.
* Remember 👉 It emits a `Change<LayoutNode>`.

```kotlin
@Composable
fun MyOwnColumn(
    modifier: Modifier = Modifier,
    children: @Composable() () -> Unit
) {
    Layout(modifier = modifier, children = children) { measurables, constraints ->
        val placeables = measurables.map { measurable ->
            // Measure children based on parent constraints
            measurable.measure(constraints)
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
            var yPosition = 0

            placeables.forEach { placeable -> // Place children in parent
                placeable.placeRelative(x = 0, y = yPosition)
                yPosition += placeable.height
            }
        }
    }
}
```
<!-- .element: class="arrow" data-highlight-only="true" -->

---

## Compose UI 📲

<div class="card">
 <b>Custom nodes and Applier</b> - Practical use case (Andrei Shikov <b>@shikasd_</b>)👇
</div>

* Building a <b>web app</b> with Compose
* <span class="blueText">Server side composition</span> 👉 apply changes (Applier) as commands sent to client via websocket.
* <span class="blueText">Client side rendering / user interaction events</span>.
* In-memory DOM representation via `Applier<HtmlNode>`.
* <span class="blueText">HtmlNode</span> 👉 representation of a UI element.

https://medium.com/@shikasd/composing-in-the-wild-145761ad62c3

<img style="width:600px" src="assets/Compose Web Server.png"/>

---

## Compose Material & Foundation 🛠

<div class="card">
  Not covered on this talk.
</div>

* Material 👉 @Composables following the <span class="blueText">Material guidelines</span>.
* Foundation 👉 <span class="blueText">generic @Composables like Box</span>.
* Foundation also includes pieces reused by both like utilities for <span class="blueText">text, shapes, corners, etc</span>.

---

## Compose Animation 🎬 and AndroidX UI

<div class="card">
  Not covered on this talk.
</div>

* <span class="blueText">Compose Animation</span> 👉 All animation APIs for compose.
* <span class="blueText">AndroidX UI</span> 👉 Tooling around compose: Compose testing utilities, support for the tooling around Compose like the layout inspector or the preview.


---

## Effect handlers 🌀

<div class="card">
  They belong to the <b>Runtime</b>, but let's cover them separately.
</div>

* <span class="blueText">All apps contain effects</span>.
* Don't run effects directly from composables 🙅 🤔 (They'd run on <span class="yellowText">every recomposition</span>) 👉 unexpected behavior.
* Wrap them in effect handlers to make the effect lifecycle aware 👉 Make sure effects run <span class="blueText">on the correct lifecycle step</span> + <span class="blueText">correct environment</span> + <span class="blueText">are bound by the Composable lifecycle</span>.

---

## Effect handlers 🌀

<div class="card">
  They're <b>under heavy development</b> iterations.
</div>

* <span class="blueText">Disclaimer:</span> Names and existing variants vary frequently.
* This covers effect handlers as of today <span class="blueText">(1.0.0-SNAPSHOT)</span>.
* Expect changes! ⚠️

---

## Effect handlers 🌀

<div class="card">
  There are <b>two categories of Effect Handlers</b>.
</div>

* <span class="blueText">Non suspending effects</span> 👉 E.g: Run a side effect to initialize some property when the Composable enters the composition.
* <span class="blueText">Suspending effects</span> 👉 E.g: Load data from network to feed some UI state.

---

## Effect handlers 🌀

<div class="card">
  <b>DisposableEffect</b> (old onCommit + onDispose)
</div>

* Side effect of composition <span class="blueText">lifecycle</span> (onEnter / onLeave).
* Fired <span class="blueText">first time and every time the inputs change</span>.
* Requires `onDispose` callback at the end 👉 disposed on leaving composition and every time inputs change.
* E.g: Initialize a callback when entering composition 👇

```kotlin
@Composable
fun backPressHandler(onBackPressed: () -> Unit, enabled: Boolean = true) {
    val dispatcher = BackPressedDispatcherAmbient.current.onBackPressedDispatcher

    val backCallback = remember {
        object : OnBackPressedCallback(enabled) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        }
    }

    DisposableEffect(dispatcher) { // dispose/relaunch if dispatcher changes
        dispatcher.addCallback(backCallback)
        onDispose {
            backCallback.remove() // avoid leaks!
        }
    }
}
```
<!-- .element: class="arrow" data-highlight-only="true" -->

---

## Effect handlers 🌀

<div class="card">
  <b>DisposableEffect(true)</b> (old onActive / onCommit(true))
</div>

* Same thing, but given constant argument 👉 fired <span class="blueText">only first time</span> and never more.
* Disposed on leaving composition.

```kotlin
@Composable
fun backPressHandler(onBackPressed: () -> Unit, enabled: Boolean = true) {
    val dispatcher = BackPressedDispatcherAmbient.current.onBackPressedDispatcher

    val backCallback = remember {
        object : OnBackPressedCallback(enabled) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        }
    }

    DisposableEffect(true) { // Will never relaunch (constant key)
        dispatcher.addCallback(backCallback)
        onDispose {
            backCallback.remove() // avoid leaks!
        }
    }
}
```
<!-- .element: class="arrow" data-highlight-only="true" -->

---

## Effect handlers 🌀

<div class="card">
  <b>SideEffect</b>
</div>

* Recorded to run after composition changes applied and lifecycle events triggered.
* For <span class="blueText">non suspended effects that do not require disposing</span>.
* <span class="blueText">Runs after every composition / recomposition</span>.
* Useful to publish updates to <span class="blueText">external states</span> unrelated to the composition.

```kotlin
@Composable
fun MyScreen(drawerTouchHandler: TouchHandler) {
  val drawerState = rememberDrawerState(DrawerValue.Closed)

  SideEffect {
    drawerTouchHandler.enabled = drawerState.isOpen
  }

  // ...
}
```
<!-- .element: class="arrow" data-highlight-only="true" -->

---

## Effect handlers 🌀

<div class="card">
  <b>invalidate</b>
</div>

* <span class="blueText">An effect</span> itself, not an effect handler.
* Invalidates composition locally 👉 <span class="blueText">enforces recomposition</span>.
* ⚠️ <span class="yellowText">Use sparingly!</span> ⚠️ observe state instead 👉 smart recomposition when changes.
* For animations there are APIs to await for next frame.
* Requires handling thread safety manually.

```kotlin
@Composable
fun MyComposable(presenter: Presenter) {
    val user = presenter.loadUser { invalidate() } // not a State!

    Text(text = "The loaded user: ${user.name})
}
```
<!-- .element: class="arrow" data-highlight-only="true" -->

---

## Effect handlers 🌀

<div class="card">
  <b>rememberCoroutineScope</b>
</div>

* <span class="blueText">To run suspended effects</span>.
* Creates `CoroutineScope` bound to this point in composition.
* Scope <span class="blueText">cancelled when leaving composition</span>.
* Same scope returned across recompositions.
* Use this scope to <span class="blueText">launch jobs in response to user interactions</span>.

```kotlin
@Composable
fun SearchScreen() {
  val scope = rememberCoroutineScope()
  var currentJob by remember { mutableStateOf<Job?>(null) }
  var items by remember { mutableStateOf<List<Item>>(emptyList()) }

  Column {
    Row {
      TextInput(
        afterTextChange = { text ->
          currentJob?.cancel()
          currentJob = scope.async {
            delay(threshold)
            items = viewModel.search(query = text)
          }
        }
      )
    }
    Row { ItemsVerticalList(items) }
  }
}
```
<!-- .element: class="arrow" data-highlight-only="true" -->

---

## Effect handlers 🌀

<div class="card">
  <b>LaunchedEffect</b>
</div>

* <span class="blueText">To run suspended effects</span>.
* Runs the effect on the applier dispatcher (Usually `AndroidUiDispatcher.Main`) when entering the composition.
* <span class="blueText">Cancels</span> the effect when leaving the composition.
* <span class="blueText">Cancels and relaunches</span> the effect when the subject changes.
* Useful to span a job across recompositions.

```kotlin
@Composable
fun SpeakerList(eventId: String) {
  var speakers by remember { mutableStateOf<List<Speaker>>(emptyList()) }
  LaunchedTask(eventId) { // cancelled / relaunched when eventId varies
    speakers = viewModel.loadSpeakers() // suspended effect
  }

  ItemsVerticalList(speakers)
}
```
<!-- .element: class="arrow" data-highlight-only="true" -->

---

## Effect handlers 🌀

<div class="card">
  <b>LaunchedEffect</b> can be simplified with <b>produceState</b>
</div>

* When used to feed a state.
* Relies on `LaunchedEffect`.

```kotlin
@Composable
fun SearchScreen(eventId: String) {
  val uiState = produceState(initialValue = emptyList<Speaker>(), eventId) {
    viewModel.loadSpeakers() // suspended effect
  }

  ItemsVerticalList(uiState.value)
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
