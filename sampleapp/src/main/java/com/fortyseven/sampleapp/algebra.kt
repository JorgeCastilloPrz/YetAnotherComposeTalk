package com.fortyseven.sampleapp

import arrow.fx.coroutines.stream.Stream
import memeid.UUID
import kotlin.coroutines.CoroutineContext

data class User(val id: UUID, val name: String, val isFavorite: Boolean = false)

interface AccountService {
  fun fetchAccountsFromNetwork(): Stream<List<User>>
}

interface AccountPersistence {
  fun loadAccountsFromDatabase(): Stream<List<User>>
  fun dbPool(): CoroutineContext
  suspend fun updateAccounts(accounts: List<User>): Unit
  suspend fun invalidateDatabase(): Unit
  suspend fun toggleFavInDatabase(user: User): Unit
}

interface HomeInteractions {
  fun pullToRefresh(): Stream<Unit>
  fun favClicks(): Stream<User>
}

interface StreamViewModel<A> {
  fun post(a: A): Stream<Unit>
  fun viewState(): Stream<A>
}

interface HomeDependencies : HomeInteractions, AccountService, AccountPersistence,
  StreamViewModel<HomeViewState> {
  companion object {
    fun create(
      interactions: HomeInteractions,
      service: AccountService,
      persistence: AccountPersistence,
      viewModel: StreamViewModel<HomeViewState>
    ): HomeDependencies = object : HomeDependencies,
      HomeInteractions by interactions,
      AccountService by service,
      AccountPersistence by persistence,
      StreamViewModel<HomeViewState> by viewModel {}
  }
}
