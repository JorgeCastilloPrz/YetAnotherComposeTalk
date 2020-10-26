package com.fortyseven.sampleapp

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.contentColor
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.ui.tooling.preview.Preview
import com.fortyseven.sampleapp.theme.DayNightTheme

enum class CourseTabs(
  @StringRes val title: Int,
  @DrawableRes val icon: Int
) {
  TAB1(R.string.tab1, R.drawable.ic_account),
  TAB2(R.string.tab2, R.drawable.ic_list),
  TAB3(R.string.tab3, R.drawable.ic_location)
}

@Composable
fun TabBar(selectedTab: CourseTabs, setSelectedTab: (CourseTabs) -> Unit) {
  val tabs = CourseTabs.values()
  BottomNavigation {
    tabs.forEach { tab ->
      BottomNavigationItem(
        icon = { Icon(vectorResource(tab.icon)) },
        label = { Text(stringResource(tab.title).toUpperCase(Locale.current)) },
        selected = tab == selectedTab,
        onClick = { setSelectedTab(tab) },
        alwaysShowLabels = false,
        selectedContentColor = MaterialTheme.colors.secondary,
        unselectedContentColor = contentColor()
      )
    }
  }
}

@Preview
@Composable
fun TabBarPreview() {
  DayNightTheme {
    TabBar(selectedTab = CourseTabs.TAB1, setSelectedTab = {})
  }
}
