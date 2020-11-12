package com.fortyseven.sampleapp

import android.util.Log
import androidx.compose.foundation.Text
import androidx.compose.material.Button
import androidx.compose.runtime.*
import androidx.ui.tooling.preview.Preview
import com.fortyseven.sampleapp.theme.DayNightTheme

@Composable
fun Counter() {
  val count = remember { mutableStateOf(0) }

  Text("${count.value}")
  Log.d("Effect", "Composition runs.")

  SideEffect {
    Log.d("Effect", "Runs!")
  }

  Button(onClick = { count.value += 1 }) {
    Text("Current count ${count.value}")
  }
}

@Preview(showBackground = true)
@Composable
fun CounterPreview() {
  DayNightTheme {
    Counter()
  }
}
