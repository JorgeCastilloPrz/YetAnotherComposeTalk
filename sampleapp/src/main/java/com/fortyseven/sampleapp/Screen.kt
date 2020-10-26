package com.fortyseven.sampleapp

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.ui.tooling.preview.Preview
import com.fortyseven.sampleapp.theme.DayNightTheme

@Composable
fun Screen(modifier: Modifier = Modifier) {
  Surface(
    color = MaterialTheme.colors.background,
    modifier = modifier
  ) {
    FullScreenLoading()
  }
}

@Preview(showBackground = true)
@Composable
fun ScreenPreview() {
  DayNightTheme {
    Screen()
  }
}
