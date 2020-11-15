package com.fortyseven.sampleapp

import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.ui.tooling.preview.Preview
import com.fortyseven.sampleapp.theme.DayNightTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

data class Speaker(val id: String, val name: String)

data class TouchHandler(var enabled: Boolean = true)

@Composable
fun MyScreen(drawerTouchHandler: TouchHandler) {
  val drawerState = rememberDrawerState(DrawerValue.Closed)

  SideEffect {
    drawerTouchHandler.enabled = drawerState.isOpen
  }

  // ...
}

@Preview(showBackground = true)
@Composable
fun CounterPreview() {
  DayNightTheme {
    Counter()
  }
}
