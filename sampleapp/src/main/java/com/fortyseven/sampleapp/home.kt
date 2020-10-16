package com.fortyseven.sampleapp

import arrow.fx.coroutines.stream.Stream
import arrow.fx.coroutines.stream.handleErrorWith
import arrow.fx.coroutines.stream.parJoinUnbounded

sealed class HomeViewState {
  object Idle : HomeViewState()
  object Loading : HomeViewState()
  data class Content(val items: List<User>) : HomeViewState()
  data class Error(val t: Throwable) : HomeViewState()
}

/*
 Our home program exist out of refreshing items on P2R & loading initial data.
 */
fun HomeDependencies.program(render: Stream<Unit>): Stream<Unit> = Stream(
  loadAccounts(),
  pullToRefresh().effectMap { invalidateDatabase() },
  favClicks().effectMap { toggleFavInDatabase(it) },
  render
).parJoinUnbounded()

fun HomeDependencies.loadAccounts(): Stream<Unit> =
  post(HomeViewState.Loading)
    .flatMap { loadAccountsRepo() }
    .map(HomeViewState::Content).flatMap { post(it) }
    .handleErrorWith { post(HomeViewState.Error(it)) }
