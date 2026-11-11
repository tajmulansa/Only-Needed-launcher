package com.example.productivitylauncher.ui.apps

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productivitylauncher.data.AppEntry
import com.example.productivitylauncher.data.LauncherState
import com.example.productivitylauncher.data.exactNameMatches
import com.example.productivitylauncher.data.filterApps
import com.example.productivitylauncher.data.openAppInfo
import com.example.productivitylauncher.data.sectionOf
import com.example.productivitylauncher.ui.components.AppText
import com.example.productivitylauncher.ui.components.Caption
import com.example.productivitylauncher.ui.theme.AppColors
import com.example.productivitylauncher.ui.theme.Motion
import com.example.productivitylauncher.ui.theme.Sans
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * All apps as a bottom sheet, like the prototype's drawer: 88% high, soft top corners, a grab handle you can drag down,
 * a search pill and an A to Z list. Opened by the All apps pill or by swiping up on Home.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDrawer(state: LauncherState, open: Boolean, onClose: () -> Unit, onOpenApp: (AppEntry) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var options by remember { mutableStateOf<AppEntry?>(null) }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val sheetH = with(density) { (maxHeight * 0.88f).toPx() }
        val hidden = sheetH * 1.02f
        val y = remember(hidden) { Animatable(hidden) }
        LaunchedEffect(open, hidden) {
            if (open) query = ""
            y.animateTo(if (open) 0f else hidden, Motion.slow(650))
        }
        val visible = y.value < hidden - 0.5f
        val veil = (1f - (y.value / sheetH).coerceIn(0f, 1f)) * 0.28f

        if (visible) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = veil))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClose),
            )
            val shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(maxHeight * 0.88f)
                    .graphicsLayer { translationY = y.value }
                    .shadow(24.dp, shape, clip = false, ambientColor = Color.Black.copy(alpha = 0.14f), spotColor = Color.Black.copy(alpha = 0.14f))
                    .background(AppColors.phone, shape)
                    .border(1.dp, AppColors.line, shape),
            ) {
                // Grab handle: drag it down to close.
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 8.dp)
                        .semantics { contentDescription = "Close all apps" }
                        .pointerInput(hidden) {
                            detectVerticalDragGestures(
                                onDragEnd = { scope.launch { if (y.value > 110.dp.toPx()) onClose() else y.animateTo(0f, Motion.slow(400)) } },
                                onDragCancel = { scope.launch { y.animateTo(0f, Motion.slow(400)) } },
                                onVerticalDrag = { _, dy -> scope.launch { y.snapTo((y.value + dy).coerceAtLeast(0f)) } },
                            )
                        }
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClose),
                    contentAlignment = Alignment.Center,
                ) { Box(Modifier.width(40.dp).height(4.dp).background(AppColors.dot, RoundedCornerShape(2.dp))) }

                Box(Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 12.dp)) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .background(AppColors.card, RoundedCornerShape(23.dp))
                            .border(1.dp, AppColors.line, RoundedCornerShape(23.dp))
                            .padding(horizontal = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicTextField(
                            value = query,
                            onValueChange = { query = it },
                            singleLine = true,
                            textStyle = TextStyle(color = AppColors.text, fontSize = 16.sp, fontFamily = Sans),
                            cursorBrush = SolidColor(AppColors.text),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { inner ->
                                Box { if (query.isEmpty()) AppText("Type an app's full name", color = AppColors.muted, display = false); inner() }
                            },
                        )
                    }
                }

                // Nothing is listed until the full name of an app is typed.
                val shown = exactNameMatches(state.apps, query)
                if (shown.isEmpty()) {
                    AppText(
                        if (query.isBlank()) "Type an app's full name to find it. Your Home apps are on the Home page." else "No app has exactly that name.",
                        color = AppColors.muted, display = false, modifier = Modifier.padding(horizontal = 24.dp, vertical = 40.dp),
                    )
                }
                LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 40.dp)) {
                    var last = ""
                    val grouped = shown.map { app ->
                        val sec = sectionOf(app.label)
                        val header = if (sec != last) sec else null
                        last = sec
                        app to header
                    }
                    items(grouped, key = { it.first.packageName + "/" + it.first.activityName }) { (app, header) ->
                        if (header != null) {
                            AppText(header, Modifier.fillMaxWidth().background(AppColors.phone).padding(top = 14.dp, bottom = 2.dp), size = 15.sp, weight = FontWeight.Medium, color = AppColors.muted, display = true)
                        }
                        val src = remember { MutableInteractionSource() }
                        val pressed by src.collectIsPressedAsState()
                        val shift by androidx.compose.animation.core.animateDpAsState(if (pressed) 8.dp else 0.dp, Motion.state(250), label = "shift")
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .graphicsLayer { translationX = shift.toPx() }
                                .combinedClickable(
                                    interactionSource = src, indication = null, role = Role.Button,
                                    onClick = { scope.launch { onClose(); delay(250); onOpenApp(app) } },
                                    onLongClick = { options = app },
                                )
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            AppText(app.label, Modifier.weight(1f), size = 24.sp, maxLines = 1, letterSpacing = (-0.72).sp, weight = FontWeight.Medium, color = AppColors.text)
                        }
                    }
                }
            }
        }
    }

    val target = options
    if (target != null) {
        val onHome = target.packageName in state.homeApps
        AlertDialog(
            onDismissRequest = { options = null },
            containerColor = AppColors.card,
            title = { AppText(target.label, size = 20.sp, weight = FontWeight.SemiBold) },
            text = {
                Column {
                    OptionRow(if (onHome) "Remove from Home" else "Add to Home") {
                        if (onHome) state.updateHomeApps(state.homeApps - target.packageName)
                        else state.updateHomeApps((state.homeApps + target.packageName).takeLast(6))
                        options = null
                    }
                    OptionRow("App info") { openAppInfo(context, target.packageName); options = null }
                }
            },
            confirmButton = { TextButton(onClick = { options = null }) { AppText("Close", weight = FontWeight.SemiBold) } },
        )
    }
}

@Composable
private fun OptionRow(text: String, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().heightIn(min = 48.dp).combinedClickableSimple(onClick),
        contentAlignment = Alignment.CenterStart,
    ) { AppText(text, size = 16.sp, weight = FontWeight.Medium) }
}

@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.combinedClickableSimple(onClick: () -> Unit): Modifier =
    this.then(Modifier.combinedClickable(role = Role.Button, onClick = onClick))
