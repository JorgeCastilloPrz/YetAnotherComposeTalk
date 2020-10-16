package com.fortyseven.sampleapp.interpreters

import arrow.fx.coroutines.IOPool
import arrow.fx.coroutines.evalOn
import arrow.fx.coroutines.milliseconds
import arrow.fx.coroutines.stream.Stream
import com.fortyseven.sampleapp.AccountService
import com.fortyseven.sampleapp.User
import memeid.UUID

fun stubService(defaultAccounts: List<User>? = null) =
  object : AccountService {
    override fun fetchAccountsFromNetwork(): Stream<List<User>> =
      Stream.effect {
        evalOn(IOPool) {
          defaultAccounts ?: listOf(
            User(UUID.V4.squuid(), "Simon"),
            User(UUID.V4.squuid(), "Raul"),
            User(UUID.V4.squuid(), "Jorge")
          )
        }
      }.delayBy(1300.milliseconds)
  }
