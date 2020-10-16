package com.fortyseven.sampleapp

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import arrow.fx.coroutines.CancelToken
import arrow.fx.coroutines.stream.Stream
import arrow.fx.coroutines.stream.cancellable

fun SwipeRefreshLayout.refreshes(): Stream<Unit> = Stream.cancellable {
  val listener = SwipeRefreshLayout.OnRefreshListener {
    emit(Unit)
  }
  this@refreshes.setOnRefreshListener(listener)
  CancelToken { this@refreshes.setOnRefreshListener(null) }
}

fun Fragment.lifecycleDestroy(): Stream<Boolean> = Stream.cancellable {
  val lifecycleObserver = LifecycleEventObserver { _, event ->
    if (event == Lifecycle.Event.ON_DESTROY) {
      emit(true)
    }
  }

  viewLifecycleOwner.lifecycle.addObserver(lifecycleObserver)
  CancelToken { viewLifecycleOwner.lifecycle.removeObserver(lifecycleObserver) }
}
