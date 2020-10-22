package com.fortyseven.sampleapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun AppBar() {
  TopAppBar {
    Image(
      modifier = Modifier
        .padding(8.dp)
        .align(Alignment.CenterVertically),
      asset = imageResource(id = R.drawable.rickandmorty)
    )

    Text(
      text = stringResource(id = R.string.toolbar_title),
      style = MaterialTheme.typography.h6,
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.CenterVertically)
        .padding(start = 8.dp)
    )
  }
}