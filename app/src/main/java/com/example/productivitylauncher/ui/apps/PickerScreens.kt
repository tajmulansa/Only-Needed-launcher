package com.example.productivitylauncher.ui.apps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productivitylauncher.data.LauncherState
import com.example.productivitylauncher.ui.Nav
import com.example.productivitylauncher.ui.components.AppText
import com.example.productivitylauncher.ui.components.CheckCircle
import com.example.productivitylauncher.ui.components.PageHeader
import com.example.productivitylauncher.ui.components.clickableRole
import com.example.productivitylauncher.ui.components.hairlineBelow
import com.example.productivitylauncher.ui.theme.AppColors

/** Pick up to six apps for the Home page. */
@Composable
fun HomePickerScreen(state: LauncherState, nav: Nav) {
    Column(Modifier.fillMaxSize()) {
        PageHeader("Home apps", onBack = nav.back)
        HomePickerBody(state, Modifier.weight(1f))
    }
}

@Composable
fun HomePickerBody(state: LauncherState, modifier: Modifier = Modifier) {
    Column(modifier) {
        AppText(
            "Choose up to six apps for your Home page. They open at once. Every other app is found by typing its full name. (${state.homeApps.size} of 6)",
            size = 15.sp, color = AppColors.muted, lineHeight = 22.sp, modifier = Modifier.padding(horizontal = 28.dp, vertical = 4.dp),
        )
        LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 28.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
            items(state.apps, key = { it.packageName + "/" + it.activityName }) { app ->
                val on = app.packageName in state.homeApps
                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 60.dp)
                        .clickableRole({
                            if (on) state.updateHomeApps(state.homeApps - app.packageName)
                            else if (state.homeApps.size < 6) state.updateHomeApps(state.homeApps + app.packageName)
                        })
                        .hairlineBelow()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    AppText(app.label, Modifier.weight(1f), size = 24.sp, weight = FontWeight.Medium, letterSpacing = (-0.72).sp, maxLines = 1)
                    CheckCircle(on, size = 28.dp)
                }
            }
        }
    }
}
