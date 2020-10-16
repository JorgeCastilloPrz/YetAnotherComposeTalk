package com.fortyseven.sampleapp.interpreters

import arrow.fx.coroutines.CancelToken
import arrow.fx.coroutines.IOPool
import arrow.fx.coroutines.evalOn
import arrow.fx.coroutines.milliseconds
import arrow.fx.coroutines.stream.Stream
import arrow.fx.coroutines.stream.cancellable
import arrow.fx.coroutines.stream.parJoinUnbounded
import com.fortyseven.sampleapp.AccountPersistence
import com.fortyseven.sampleapp.User
import kotlin.coroutines.CoroutineContext

fun stubPersistence(defaultAccounts: List<User>? = null, pool: CoroutineContext = IOPool) =
  object : AccountPersistence {
    private var accounts = defaultAccounts ?: emptyList()

    private lateinit var updateTrigger: () -> Unit

    override fun loadAccountsFromDatabase(): Stream<List<User>> = Stream(
      Stream.cancellable<List<User>> {
        updateTrigger = { emit(accounts); }
        CancelToken { updateTrigger = {} }
      },
      Stream.effect {
        evalOn(pool) {
          accounts // this would be an effect to connect to the db and access accounts irl.
        }
      }.delayBy(700.milliseconds)
    ).parJoinUnbounded()

    override fun dbPool() = pool

    override suspend fun updateAccounts(accounts: List<User>) {
      this.accounts = accounts
    }

    override suspend fun invalidateDatabase() {
      accounts = emptyList()
      updateTrigger()
    }

    override suspend fun toggleFavInDatabase(user: User): Unit {
      accounts = accounts.map {
        if (it.id == user.id) user.copy(isFavorite = !user.isFavorite) else it
      }
      updateTrigger()
    }
  }
