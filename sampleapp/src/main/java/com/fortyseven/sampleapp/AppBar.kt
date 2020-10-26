package com.fortyseven.sampleapp

import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import com.fortyseven.sampleapp.theme.DayNightTheme

@Composable
fun AppBar() {
  TopAppBar(title = {
    Text(
      text = stringResource(id = R.string.toolbar_title),
      style = MaterialTheme.typography.h6
    )
  })
}

@Preview(showBackground = true)
@Composable
fun AppBarPreview() {
  DayNightTheme {
    AppBar()
  }
}
