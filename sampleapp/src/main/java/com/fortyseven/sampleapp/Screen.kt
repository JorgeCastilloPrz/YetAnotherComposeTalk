package com.fortyseven.sampleapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.ui.tooling.preview.Preview
import com.fortyseven.sampleapp.theme.DayNightTheme

@Composable
fun Screen(modifier: Modifier = Modifier) {
  Surface(
    color = MaterialTheme.colors.background,
    modifier = modifier.then(Modifier.fillMaxSize())
  ) {
    // FullScreenLoading()
    Box(alignment = Alignment.Center) {
      // Counter()
    }
  }
}

@Preview(showBackground = true)
@Composable
fun ScreenPreview() {
  DayNightTheme {
    Screen()
  }
}
