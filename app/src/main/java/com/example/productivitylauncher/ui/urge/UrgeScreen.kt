package com.example.productivitylauncher.ui.urge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.productivitylauncher.data.openMessages
import com.example.productivitylauncher.data.openPhone
import com.example.productivitylauncher.ui.components.Caption
import com.example.productivitylauncher.ui.components.Ic
import com.example.productivitylauncher.ui.steady.FlipCard
import com.example.productivitylauncher.ui.steady.LandscapeAwake
import com.example.productivitylauncher.ui.steady.RoundButton
import com.example.productivitylauncher.ui.theme.AppColors

/**
 * Shown full screen while the red button's timer runs, in the same style as Steady mode: a landscape flip clock, here
 * counting down minutes and seconds, with only Phone and Messages. There is no exit; it ends when the timer does.
 */
@Composable
fun UrgeScreen(remainingMillis: Long) {
    val context = LocalContext.current
    LandscapeAwake()
    val totalSeconds = ((remainingMillis + 999L) / 1000L).coerceAtLeast(0L)
    val mm = "%02d".format(totalSeconds / 60L)
    val ss = "%02d".format(totalSeconds % 60L)
    Box(Modifier.fillMaxSize().background(AppColors.phone).padding(horizontal = 28.dp, vertical = 20.dp)) {
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            BoxWithConstraints(Modifier.weight(1f).fillMaxHeight()) {
                val gap = 16.dp
                val cardH = minOf(maxHeight * 0.66f, (maxWidth - gap) / 2 / 1.1f)
                Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                        FlipCard(mm, cardH)
                        FlipCard(ss, cardH)
                    }
                    Box(Modifier.height(18.dp))
                    Caption("Take a break \u00B7 this feeling passes", color = AppColors.gate)
                }
            }
            Column(Modifier.padding(start = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                RoundButton(Ic.Phone, "Call") { openPhone(context) }
                RoundButton(Ic.Message, "Message") { openMessages(context) }
            }
        }
    }
}
