package com.example.productivitylauncher.ui.apps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productivitylauncher.data.AppEntry
import com.example.productivitylauncher.data.LauncherState
import com.example.productivitylauncher.data.exactNameMatches
import com.example.productivitylauncher.data.openAppInfo
import com.example.productivitylauncher.ui.components.AppIcon
import com.example.productivitylauncher.ui.components.AppText
import com.example.productivitylauncher.ui.components.Caption
import com.example.productivitylauncher.ui.components.Ic
import com.example.productivitylauncher.ui.components.TextLink
import com.example.productivitylauncher.ui.components.clickableRole
import com.example.productivitylauncher.ui.home.Hair
import com.example.productivitylauncher.ui.theme.AppColors
import com.example.productivitylauncher.ui.theme.Display
import com.example.productivitylauncher.ui.theme.Motion
import com.example.productivitylauncher.ui.theme.enterStagger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Find an app. There is no app list: you type the full name of an app and it appears. It opens over Home with the
 * keyboard already up, so finding an app is one swipe (or one tap on the search pill) and a name.
 */
@Composable
fun AppSearch(state: LauncherState, open: Boolean, onClose: () -> Unit, onOpenApp: (AppEntry) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    AnimatedVisibility(
        visible = open,
        enter = fadeIn(Motion.enter(300)) + slideInVertically(Motion.enter(420)) { it / 14 },
        exit = fadeOut(Motion.exit(200)) + slideOutVertically(Motion.exit(260)) { it / 14 },
    ) {
        var query by remember { mutableStateOf("") }
        val focus = remember { FocusRequester() }
        val keyboard = LocalSoftwareKeyboardController.current
        LaunchedEffect(Unit) { focus.requestFocus(); keyboard?.show() }
        DisposableEffect(Unit) { onDispose { keyboard?.hide() } }

        Column(
            Modifier
                .fillMaxSize()
                .background(AppColors.phone)
                // Swallow touches so nothing underneath reacts while searching.
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
        ) {
            Row(
                Modifier.fillMaxWidth().padding(start = 28.dp, end = 20.dp, top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Caption("Find an app", Modifier.padding(vertical = 10.dp))
                Box(
                    Modifier.size(40.dp).border(1.dp, AppColors.line, CircleShape).semantics { contentDescription = "Close search" }.clickableRole(onClose),
                    contentAlignment = Alignment.Center,
                ) { AppIcon(Ic.Close, AppColors.text, size = 16.dp) }
            }

            // One big line of type, like the clock. The cursor is the only decoration.
            Column(Modifier.padding(start = 28.dp, end = 28.dp, top = 28.dp)) {
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = TextStyle(color = AppColors.text, fontSize = 36.sp, fontFamily = Display, fontWeight = FontWeight.Medium, letterSpacing = (-1.4).sp),
                    cursorBrush = SolidColor(AppColors.text),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { exactNameMatches(state.apps, query).firstOrNull()?.let { app -> scope.launch { onClose(); delay(220); onOpenApp(app) } } }),
                    modifier = Modifier.fillMaxWidth().focusRequester(focus),
                    decorationBox = { inner ->
                        Box(Modifier.padding(bottom = 14.dp)) {
                            if (query.isEmpty()) AppText("Type a full name", size = 36.sp, weight = FontWeight.Medium, color = AppColors.dot, letterSpacing = (-1.4).sp, maxLines = 1)
                            inner()
                        }
                    },
                )
                Hair(0)
            }

            val shown = exactNameMatches(state.apps, query)
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp, vertical = 22.dp)) {
                when {
                    query.isBlank() -> AppText(
                        "Type the full name of any app. Your six Home apps are already on Home.",
                        size = 15.sp, color = AppColors.muted, lineHeight = 22.sp, display = false,
                    )
                    shown.isEmpty() -> AppText("No app has exactly that name.", size = 15.sp, color = AppColors.muted, display = false)
                    else -> shown.forEachIndexed { i, app ->
                        key(app.packageName + "/" + app.activityName) {
                            val onHome = app.packageName in state.homeApps
                            Column(Modifier.fillMaxWidth().enterStagger(i).padding(bottom = 20.dp)) {
                                Caption(if (onHome) "On Home · opens at once" else "Choose how long next")
                                AppText(
                                    app.label,
                                    Modifier.fillMaxWidth().clickableRole({ scope.launch { onClose(); delay(220); onOpenApp(app) } }).padding(top = 8.dp, bottom = 4.dp),
                                    size = 34.sp, weight = FontWeight.Medium, maxLines = 2, color = AppColors.text,
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    if (onHome) {
                                        TextLink("Remove from Home", { state.updateHomeApps(state.homeApps - app.packageName) }, Modifier.padding(start = 0.dp))
                                    } else {
                                        TextLink(if (state.homeApps.size < 6) "Add to Home" else "Home is full", { state.updateHomeApps(state.homeApps + app.packageName) }, enabled = state.homeApps.size < 6)
                                    }
                                    TextLink("App info", { openAppInfo(context, app.packageName) })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
