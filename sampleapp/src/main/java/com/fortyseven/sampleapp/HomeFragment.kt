package com.fortyseven.sampleapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Layout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import arrow.fx.coroutines.CancelToken
import arrow.fx.coroutines.Environment
import arrow.fx.coroutines.evalOn
import arrow.fx.coroutines.stream.Stream
import arrow.fx.coroutines.stream.cancellable
import arrow.fx.coroutines.stream.drain
import com.fortyseven.sampleapp.interpreters.stubPersistence
import com.fortyseven.sampleapp.interpreters.stubService
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers.Main

class HomeFragment : Fragment() {

  private val viewModel = ViewModel<HomeViewState>(HomeViewState.Idle)
  private val networkService = stubService()
  private val persistence = stubPersistence()
  private val adapter = UserAdapter()

  private lateinit var list: RecyclerView
  private lateinit var pullToRefresh: SwipeRefreshLayout

  @Composable
  fun SomeComposable() {
    Layout() {

    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    retainInstance = true
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? = inflater.inflate(R.layout.fragment_home, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    list = view.findViewById(R.id.list)
    pullToRefresh = view.findViewById(R.id.pullToRefresh)

    val interactions = object : HomeInteractions {
      override fun pullToRefresh(): Stream<Unit> = pullToRefresh.refreshes()
      override fun favClicks(): Stream<User> = favClicksStream()
    }

    list.adapter = adapter

    val env = Environment()
    env.unsafeRunAsync {
      HomeDependencies.create(interactions, networkService, persistence, viewModel)
        .program(render(view, adapter))
        .interruptWhen(lifecycleDestroy())
        .drain()
    }
  }

  private fun favClicksStream() = Stream.cancellable {
    val favListener = { user: User -> emit(user) }
    adapter.favListener = favListener
    CancelToken { adapter.favListener = {} }
  }

  private fun render(view: View, adapter: UserAdapter): Stream<Unit> =
    viewModel.viewState().effectMap { state ->
      evalOn(Main) {
        when (state) {
          HomeViewState.Idle -> {
            pullToRefresh.isRefreshing = false
          }
          HomeViewState.Loading -> {
            pullToRefresh.isRefreshing = true
          } // Use default loading ad
          is HomeViewState.Content -> {
            pullToRefresh.isRefreshing = false
            adapter.submitList(state.items)
          }
          is HomeViewState.Error -> {
            pullToRefresh.isRefreshing = false
            Snackbar.make(
              view,
              getString(R.string.error, state.t),
              BaseTransientBottomBar.LENGTH_SHORT
            ).show()
          }
        }
      }
    }
}
