package com.fortyseven.sampleapp

import arrow.fx.coroutines.stream.Stream
import arrow.fx.coroutines.stream.handleErrorWith

/**
 * This needs to load accounts from DB fist, and in case there's an error or the DB accounts list
 * is empty, fallback to loading accounts from network.
 */
fun HomeDependencies.loadAccountsRepo(): Stream<List<User>> =
  loadAccountsFromDatabase()
    .handleErrorWith { fetchAccountsFromNetwork() }
    .flatMap {
      if (it.isEmpty()) {
        fetchAccountsFromNetwork()
      } else {
        Stream(it)
      }
    }.effectTap { updateAccounts(it) }
