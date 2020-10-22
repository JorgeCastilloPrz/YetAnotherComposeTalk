package com.fortyseven.sampleapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.setContent
import androidx.ui.tooling.preview.Preview
import com.fortyseven.sampleapp.theme.DayNightTheme

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      DayNightTheme {
        AppContent()
      }
    }
  }
}

@Composable
fun AppContent() {
  val (selectedTab, setSelectedTab) = remember { mutableStateOf(CourseTabs.CHARACTERS) }
  Scaffold(
    backgroundColor = MaterialTheme.colors.primarySurface,
    topBar = { AppBar() },
    bottomBar = { TabBar(selectedTab, setSelectedTab) }
  ) { innerPadding ->
    val modifier = Modifier.padding(innerPadding)
    when (selectedTab) {
      CourseTabs.CHARACTERS -> Screen(modifier)
      CourseTabs.EPISODES -> Screen(modifier)
      CourseTabs.SEARCH -> Screen(modifier)
    }
  }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
  DayNightTheme {
    AppContent()
  }
}
