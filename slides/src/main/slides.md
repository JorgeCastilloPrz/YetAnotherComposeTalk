## Compose Architecture

<div class="card">
    The Jetpack Compose <b>libraries</b>
</div>

* Compiler plugin - <span class="blueText">compose.compiler</span>
* Runtime - <span class="blueText">compose.runtime</span>
* UI - <span class="blueText">compose.ui</span>
* <span class="fadedOut">Material - <span class="blueText">compose.material</span></span>
* <span class="fadedOut">Foundation - <span class="blueText">compose.foundation</span></span>
* <span class="fadedOut">Animation - <span class="blueText">compose.animation</span></span>
* <span class="fadedOut">UI Tooling - <span class="blueText">androidx.ui</span></span>

---

## Compose compiler ⚙

<div class="card">
  <b>Plugin</b> to generate metadata to satisfy the <b>Runtime</b> needs.
</div>

* Scans for <span class="blueText">`@Composable`</span> functions 👉 <span class="blueText">generates convenient IR</span> to include relevant info when called (1.4 IR)
* Makes them <span class="blueText">restartable / cacheable</span>.
* Unlocks runtime optimizations.

<br/>

<span class="blueText">*(Smart recomposition, parallel composition, flag "stable" apis...)*</span>

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
fun Counter($composer: Composer, $key: Int) {
  val count = remember($composer, 123) { mutableStateOf(0) }

  Button($composer, 456, onClick = { count.value += 1 }) {
    Text($composer, 789, "Current count ${count.value}")
  }
}
```
<!-- .element: class="arrow" data-highlight-only="true" -->

* How can we ensure <span class="blueText">`Composer`</span> is <span class="blueText">passed across levels?</span>

---

## Compiler checks ⚙

<div class="card">
    👇 <b>Impose a calling context</b> 👇
</div>

* Requirement imposed by the Compiler <span class="blueText">frontend</span> phase <span class="yellowText">(static checks)</span> 👉 fast feedback loop.
* Can <span class="blueText">only be called from other `@Composable` functions</span>.
* Compiler can make the <span class="blueText">Composer available at all levels</span>.

---

## Compose Runtime 🏃

<div class="card">
    Compose runtime is <b>declarative</b>.
</div>

<img src="assets/Runtime concern separation.png"/>

* The interpreter has the big picture 👉 Can decide how to execute / consume the program.
* Interpreter <span class="blueText">decoupled from the description of the program</span>.

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
    <b>No</b>. Compose runtime works with <b>generic nodes</b> of type <b>N</b>.
</div>

* Slot table 👉 generic node structure.
* Reading composable tree 👉 emits changes over the table.
* Changes to <span class="blueText">add, remove, replace, move</span> nodes based on logics <span class="yellowText">(think of conditional logics)</span>.
* Changes emitted are generic 👉 <span class="blueText">`Change<N>`</span>.

---

## Compose Runtime 🏃

<div class="card">
  See how <b>Layout</b> emits a change to the table.
</div>

```kotlin
@Composable inline fun Layout(...) {
    emit<LayoutNode, Applier<Any>>( // emits change to insert a node
        ctor = { LayoutNode() },
        update = {
            set(measureBlocks, LayoutEmitHelper.setMeasureBlocks)
            set(DensityAmbient.current, LayoutEmitHelper.setDensity)
            set(LayoutDirectionAmbient.current, LayoutEmitHelper.setLayoutDirection)
        },
        ...
        children = children
    )
}
```
<!-- .element: class="arrow" data-highlight-only="true" -->

* `emit` "records" a <span class="blueText">change for inserting a UI node</span> 👇

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
  Change 👉 <b>lambda</b> that represents an <b>effect</b>.
</div>

```kotlin
internal typealias Change<N> = (
    applier: Applier<N>, // interpreter 👉 materializes changes
    slots: SlotWriter, // write changes to the table
    lifecycleManager: LifecycleManager // lifecycle is relevant when applying changes
) -> Unit
```
<!-- .element: class="arrow" data-highlight-only="true" -->

* onEnter / onLeave <span class="blueText">LifecycleObservers</span> are called when adding / removing / updating elements on the table.

```kotlin
internal interface LifecycleManager {
    fun entering(instance: CompositionLifecycleObserver)
    fun leaving(instance: CompositionLifecycleObserver)
    fun sideEffect(effect: () -> Unit)
}
```
<!-- .element: class="arrow" data-highlight-only="true" -->

* Another type of Change can be recording a side effect.

---

## Compose Runtime 🏃

<div class="card">
  What can be <b>stored in the slot table</b>?
</div>

Any relevant data required to materialize a UI snapshot.

* Operations to add / remove / replace <span class="blueText">UI nodes</span>.
* Operations to store <span class="blueText">State</span>.
* Operations to store remembered data (`remember`).
* <span class="blueText">Composable function calls</span> and their parameters.
* <span class="blueText">Providers and Ambients</span>.
* <span class="blueText">Side effects</span> of composition lifecycle (onEnter / onLeave).
* ...

---

## Compose Runtime 🏃

<div class="card">
  <b>Slot table in depth</b>.
</div>

* https://www.youtube.com/watch?v=6BRlI5zfCCk

<img style="margin-top:60px;" src="assets/ComposeDemistifiedTalk.jpg"/>

---

## Compose Runtime 🏃

<img src="assets/The order of Runtime.png"/>

* Recomposition required? 👉 <span class="blueText">back to step one</span> ⏮
* Recorded side effects run after lifecycle events <span class="blueText">to ensure onEnter before</span>.
* Side effects are discarded after a <span class="blueText">@Composable</span> leaves the composition.

---

## Compose Runtime 🏃

<div class="card">
  What about <b>recomposition</b>?
</div>

* Composer can <span class="blueText">discard pending compositions</span> when composition fails, and also smartly <span class="blueText">skip recomposition</span> via the <span class="blueText">RecomposerScope</span>.

```kotlin
@Composable
fun Counter($composer: Composer) {
  $composer.start()
  // ...our composable logics
  $composer.end()?.updateScope { nextComposer -> // this block will drive recomposition!
    Counter(nextComposer)
  }
}
```
<!-- .element: class="arrow" data-highlight-only="true" -->

* `$composer.end()` returns `null` if no observable model was read during the composition 👉 recomposition not needed.

---

## Compose Runtime 🏃

<div class="card">
 <b>Positional Memoization</b> when reading from the slot table.
</div>

* <span class="blueText">Remember result of a `@Composable` call</span> and return it without computing it again.
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

* <span class="blueText">Structured concurrency</span> 👉 Parallel recomposition, offload recomposition to different threads...
* <span class="blueText">Automatic Cancellation</span> in effect handlers ⏩
* <span class="blueText">Can't replace</span> it, but we can <span class="blueText">provide your own Applier<N> impl and node types</span>.

---

## Compose UI 📲

<div class="card">
  Materialize all recorded changes <b>into ultimate Android UI</b>.
</div>

* <span class="blueText">Bridges the gap between the Runtime and the Platform</span>.
* The chosen <span class="blueText">`Applier<N>`</span> implementation does the job.
* Provides integration with the device: layout, drawing (skia), user input...

<img src="assets/Runtime concern separation 2.png"/>

---

## Compose UI 📲

<div class="card">
 Built-in Applier implementation for <b>Android</b>: The <b>UiApplier</b>.
</div>

* Supports <span class="blueText">both ViewGroups and Composable LayoutNodes</span>
* The <span class="blueText">Applier<N></span> is a visitor that <span class="blueText">visits</span> the whole node tree element by element.

```kotlin
class UiApplier(private val root: Any) : Applier<Any> {
    private val stack = Stack<Any>()

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
        when (parent) {
          is ViewGroup ->
            when (instance) {
              is View -> { parent.addView(instance, index) }
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
                val androidViewHolder = ViewBlockHolder<View>(instance.context).apply {
                  view = instance
                }
                parent.insertAt(index, androidViewHolder.toLayoutNode())
              }
              is LayoutNode -> parent.insertAt(index, instance)
              else -> invalidNode(instance)
            }
          else -> invalidNode(parent)
        }
    }
}
```
<!-- .element: class="arrow" data-highlight-only="true" -->

---

## Compose UI 📲

<div class="card">
  <b>LayoutNode</b> is an in memory representation of a UI node
</div>

* <span class="blueText">Attached to an Owner</span> when materialized.
* Owner 👉 <span class="blueText">Connection with the View system</span>.
* It <span class="blueText">keeps a reference to its parent</span> or the Owner when it's root.

<img style="margin-top:40px;" src="assets/LayoutNode hierarchy.png"/>

* It keeps track of all children and how to <span class="blueText">measure / place</span> those and itself.

---

## Compose UI 📲

<div class="card">
 <b>Other use cases for custom Appliers / nodes?</b> 🤷
</div>

* <span class="blueText">UI testing libs</span> that interpret changes by creating abstractions of the UI elements to assert over.
* Support <span class="blueText">other platforms like desktop or web</span>.
* <span class="blueText">Control hardware</span> 👉 minimize commands to reflect a change.
* ... 🤔💡

---

## Compose UI 📲

<div class="card">
 <b>Custom nodes and Applier</b> - Practical use case (Andrei Shikov <b>@shikasd_</b>)👇
</div>

* Building a <b>web app</b> with Compose
* <span class="blueText">Server side composition</span> and client communication via websocket.
* Custom `Applier<HtmlNode>`.

https://medium.com/@shikasd/composing-in-the-wild-145761ad62c3

<img style="width:600px" src="assets/Compose Web Server.png"/>

---

## Effect handlers 🌀

<div class="card">
  They belong to the <b>Runtime</b>
</div>

* <span class="blueText">Apps contain effects</span>.
* Don't run effects directly from composables. 🙅 ➡ Composables are restartable (<span class="yellowText">might run multiple times</span>).
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

* <span class="blueText">Non suspending effects</span> 👉 E.g: Run a side effect to initialize a callback when the Composable enters the composition, and dipose it when it leaves.
* <span class="blueText">Suspending effects</span> 👉 E.g: Load data from network to feed some UI state.

---

## Effect handlers 🌀

<div class="card">
  <b>DisposableEffect</b> (old onCommit + onDispose)
</div>

* Side effect of composition <span class="blueText">lifecycle</span> (observing onEnter / onLeave).
* Fired <span class="blueText">first time and every time the inputs change</span>.
* Requires `onDispose` at the end 👉 disposed on leaving composition and every time inputs change.

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
  <b>DisposableEffect(Unit)</b> (old onActive / onCommit(Unit))
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

    DisposableEffect(Unit) { // Will never relaunch (constant key)
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

* More like a <span class="blueText">fire on this composition or forget</span>. (Discarded if composition fails).
* For effects that <span class="blueText">do not require disposing</span>.
* Runs <span class="blueText">after every composition / recomposition</span>.
* Useful to publish updates to <span class="blueText">external states</span>.

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

* Invalidates composition locally 👉 <span class="blueText">enforces recomposition</span>.
* ⚠️ <span class="yellowText">Use sparingly!</span> ⚠️ observe state instead 👉 smart recomposition when changes.
* For animations there are APIs to await for next frame.
* Requires handling thread safety manually.
* <span class="blueText">When source of truth is not a compose `State`</span>.

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
  <b>rememberCoroutineScope</b> - suspended effects.
</div>

* Creates `CoroutineScope` bound to this composition.
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
  <b>LaunchedEffect</b> - suspended effects.
</div>

* Runs the effect on the applier dispatcher (Usually `AndroidUiDispatcher.Main`) when entering.
* <span class="blueText">Cancels</span> the effect when leaving.
* <span class="blueText">Cancels and relaunches</span> the effect when subject changes.
* Useful to span a job across recompositions.

```kotlin
@Composable
fun SpeakerList(eventId: String) {
  var speakers by remember { mutableStateOf<List<Speaker>>(emptyList()) }
  LaunchedEffect(eventId) { // cancelled / relaunched when eventId varies
    speakers = viewModel.loadSpeakers(eventId) // suspended effect
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
    viewModel.loadSpeakers(eventId) // suspended effect
  }

  ItemsVerticalList(uiState.value)
}
```
<!-- .element: class="arrow" data-highlight-only="true" -->

---

## Surviving config changes?

<div class="card">
  <b>No time!</b> Will need to write a post 😅 🙏
</div>

<br />
Special thanks to Adam Powell 🙋‍

---

## Thank you! 🙌

<div>
  <img style="vertical-align:middle;width:140px;margin-right:20px;" src="/assets/twitter_logo.jpg"/>
  <h2 style="display:inline;"><span style=""><u>@JorgeCastilloPr</u></span></h2>
</div>

<img src="/assets/jetpack-compose.svg"/>
