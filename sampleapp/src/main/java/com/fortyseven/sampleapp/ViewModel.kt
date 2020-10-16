package com.fortyseven.sampleapp

import arrow.fx.coroutines.stream.Stream
import arrow.fx.coroutines.stream.concurrent.SignallingAtomic

/**
 * The ViewModel is a simple class that works as a cache for delivering the view state and
 * observing it. It survives configuration changes only because the fragment that instantiates
 * it is retained.
 */
class ViewModel<A>(default: A) : StreamViewModel<A> {

  private val state: SignallingAtomic<A> = SignallingAtomic.unsafe(default)

  override fun viewState(): Stream<A> = state.discrete()

  override fun post(a: A): Stream<Unit> =
    Stream.effect { state.update { a } }
}
