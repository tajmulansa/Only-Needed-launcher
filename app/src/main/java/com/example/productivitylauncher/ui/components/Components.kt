package com.example.productivitylauncher.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.widthIn
import com.example.productivitylauncher.ui.theme.Motion
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productivitylauncher.ui.theme.AppColors
import com.example.productivitylauncher.ui.theme.Display
import com.example.productivitylauncher.ui.theme.Mono
import com.example.productivitylauncher.ui.theme.Sans
import com.example.productivitylauncher.ui.theme.enterStagger

/** Shapes: soft tonal surfaces and fully rounded pills (the Still design). */
val RadiusSm = RoundedCornerShape(10.dp)
val RadiusMd = RoundedCornerShape(18.dp)
val RadiusLg = RoundedCornerShape(26.dp)
val RadiusPill = RoundedCornerShape(percent = 50)

fun Modifier.sizeCompat(s: Dp): Modifier = this.then(Modifier.size(s))

/** The only text composable screens should use, so font and colours stay consistent. */
@Composable
fun AppText(
    text: String,
    modifier: Modifier = Modifier,
    size: TextUnit = 16.sp,
    weight: FontWeight = FontWeight.Normal,
    color: Color = AppColors.text,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    align: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    mono: Boolean = false,
    strike: Boolean = false,
    display: Boolean? = null,
    tnum: Boolean = false,
) {
    // Large text uses the display face with tight tracking, like the prototype. Small text stays in Geist.
    val useDisplay = !mono && (display ?: (size.value >= 19f))
    val tracking = if (letterSpacing != TextUnit.Unspecified || !useDisplay) letterSpacing
        else if (size.value >= 48f) (-0.05).em else if (size.value >= 28f) (-0.04).em else (-0.02).em
    val leading = if (lineHeight != TextUnit.Unspecified || !useDisplay || size.value < 24f) lineHeight else size * 1.1f
    androidx.compose.material3.Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = size,
        fontWeight = weight,
        fontFamily = if (mono) Mono else if (useDisplay) Display else Sans,
        letterSpacing = tracking,
        textAlign = align,
        lineHeight = leading,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        textDecoration = if (strike) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
        style = if (tnum) androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum") else androidx.compose.ui.text.TextStyle.Default,
    )
}

/** Small uppercase caption used above sections. */
@Composable
fun Caption(text: String, modifier: Modifier = Modifier, color: Color = AppColors.muted) {
    AppText(text.uppercase(), modifier, size = 11.sp, weight = FontWeight.Medium, color = color, letterSpacing = 1.sp, mono = true)
}

@Composable
fun Rule(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(1.dp).background(AppColors.line))
}

/** A quiet tonal surface. No shadow: depth comes from the tone, not from elevation. */
@Composable
fun CeramicCard(
    modifier: Modifier = Modifier,
    shape: Shape = RadiusLg,
    color: Color = Color.Unspecified,
    padding: PaddingValues = PaddingValues(22.dp),
    borderColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val filled = color != Color.Unspecified
    val base = modifier
        .then(if (filled) Modifier.background(color, shape) else Modifier)
        .then(
            when {
                borderColor != null -> Modifier.border(2.dp, borderColor, shape)
                !filled -> Modifier.border(1.dp, AppColors.line, shape)
                else -> Modifier
            },
        )
    val clickable = if (onClick != null) base.then(Modifier.clickableRole(onClick)) else base
    Column(clickable.padding(padding), verticalArrangement = Arrangement.spacedBy(14.dp), content = content)
}

/** A tap target that gives a small springy press: it shrinks a little while held. */
fun Modifier.clickableRole(onClick: () -> Unit, role: Role = Role.Button): Modifier = composed {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) Motion.PressScale else 1f, Motion.press(), label = "press")
    Modifier
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(interactionSource = source, indication = null, role = role, onClick = onClick)
}

enum class BtnKind { Primary, Accent, Soft, Gate }

@Composable
fun CButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    kind: BtnKind = BtnKind.Primary,
    enabled: Boolean = true,
    small: Boolean = false,
    leading: Ic? = null,
) {
    val bg = when (kind) {
        BtnKind.Primary -> AppColors.primary
        BtnKind.Accent -> AppColors.focus
        BtnKind.Soft -> AppColors.card
        BtnKind.Gate -> AppColors.gate
    }
    val fg = when (kind) {
        BtnKind.Primary -> AppColors.onPrimary
        BtnKind.Accent -> AppColors.onFocus
        BtnKind.Soft -> AppColors.text
        BtnKind.Gate -> AppColors.onGate
    }
    val shape = RadiusPill
    Row(
        modifier
            .alpha(if (enabled) 1f else 0.4f)
            .background(bg, shape)
            .then(if (enabled) Modifier.clickableRole(onClick) else Modifier)
            .heightIn(min = if (small) 44.dp else 58.dp)
            .padding(horizontal = if (small) 20.dp else 24.dp, vertical = if (small) 10.dp else 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            AppIcon(leading, fg, size = 20.dp)
            Spacer(Modifier.width(8.dp))
        }
        AppText(text, size = if (small) 14.sp else 16.sp, weight = FontWeight.Medium, color = fg, align = TextAlign.Center, display = false)
    }
}

/** Plain underlined-style text button, for quiet actions like Continue. */
@Composable
fun TextLink(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Box(
        modifier
            .alpha(if (enabled) 1f else 0.5f)
            .heightIn(min = 44.dp)
            .then(if (enabled) Modifier.clickableRole(onClick) else Modifier)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        AppText(text, size = 15.sp, weight = FontWeight.Medium, color = AppColors.muted)
    }
}

@Composable
fun IconButtonLarge(ic: Ic, description: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(40.dp)
            .border(1.dp, AppColors.line, CircleShape)
            .semantics { contentDescription = description }
            .clickableRole(onClick),
        contentAlignment = Alignment.Center,
    ) {
        AppIcon(ic, AppColors.text, size = 18.dp)
    }
}

/**
 * Page header as in the prototype: a slim top row (back circle, small label, actions on the right)
 * and a big display title under it.
 */
@Composable
fun PageHeader(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    label: String? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    Column(modifier.fillMaxWidth().padding(start = 28.dp, end = 28.dp, top = 12.dp, bottom = 14.dp)) {
        Row(Modifier.fillMaxWidth().height(40.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (onBack != null) IconButtonLarge(Ic.Back, "Back", onBack)
            if (label != null) Caption(label)
            Spacer(Modifier.weight(1f))
            if (trailing != null) trailing()
        }
        AppText(title, Modifier.padding(top = 12.dp), size = 44.sp, weight = FontWeight.Medium, letterSpacing = (-2).sp, maxLines = 1)
    }
}

@Composable
fun Chip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val chipBg by animateColorAsState(if (selected) AppColors.primary else Color.Transparent, Motion.state(160), label = "chipBg")
    val chipFg by animateColorAsState(if (selected) AppColors.onPrimary else AppColors.muted, Motion.state(160), label = "chipFg")
    Box(
        modifier
            .heightIn(min = 44.dp)
            .background(chipBg, RadiusPill)
            .border(1.dp, if (selected) Color.Transparent else AppColors.line, RadiusPill)
            .clickableRole(onClick, Role.Tab)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        AppText(text, size = 15.sp, weight = FontWeight.SemiBold, color = chipFg)
    }
}

/** Segmented control, e.g. Auto / Light / Dark. The selected pill slides to the new option. */
@Composable
fun Segmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val haptic = LocalHapticFeedback.current
    BoxWithConstraints(modifier.fillMaxWidth().background(AppColors.card, RadiusPill).padding(3.dp)) {
        val segment = maxWidth / options.size
        val x by animateDpAsState(segment * selected, Motion.press(), label = "segment")
        Box(Modifier.matchParentSize()) {
            Box(Modifier.offset(x = x).width(segment).fillMaxHeight().shadow(1.dp, RadiusPill, ambientColor = Color(0x26000000), spotColor = Color(0x26000000)).background(AppColors.phone, RadiusPill))
        }
        Row(Modifier.fillMaxWidth()) {
            options.forEachIndexed { i, label ->
                val on = i == selected
                val textColor by animateColorAsState(if (on) AppColors.text else AppColors.muted, Motion.state(160), label = "segmentText")
                Box(
                    Modifier
                        .weight(1f)
                        .heightIn(min = 40.dp)
                        .clickableRole({
                            if (!on) haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            onSelect(i)
                        }, Role.Tab),
                    contentAlignment = Alignment.Center,
                ) {
                    AppText(label, size = 13.sp, weight = FontWeight.SemiBold, color = textColor)
                }
            }
        }
    }
}

@Composable
fun Toggle(on: Boolean, onChange: (Boolean) -> Unit, description: String) {
    // The thumb slides and the track changes colour, so the switch never just teleports.
    val track by animateColorAsState(if (on) AppColors.text else AppColors.card, Motion.state(160), label = "toggleTrack")
    val thumb by animateColorAsState(if (on) AppColors.phone else AppColors.text, Motion.state(160), label = "toggleThumb")
    val slide by animateFloatAsState(if (on) 1f else 0f, Motion.press(), label = "toggleSlide")
    val haptic = LocalHapticFeedback.current
    // The tap area is 48 dp tall even though the track is 32 dp, and it shrinks a little when pressed.
    Box(
        Modifier
            .size(width = 52.dp, height = 48.dp)
            .semantics { contentDescription = description }
            .clickableRole({
                haptic.performHapticFeedback(if (on) HapticFeedbackType.ToggleOff else HapticFeedbackType.ToggleOn)
                onChange(!on)
            }, Role.Switch),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(width = 52.dp, height = 32.dp).background(track, RoundedCornerShape(16.dp)).border(1.dp, AppColors.line, RoundedCornerShape(16.dp)).padding(3.dp)) {
            Box(Modifier.offset(x = 20.dp * slide).size(26.dp).background(thumb, CircleShape))
        }
    }
}

/** Round check used by the frog and habits. */
@Composable
fun CheckCircle(checked: Boolean, size: Dp = 34.dp) {
    val fill by animateFloatAsState(if (checked) 1f else 0f, Motion.state(160), label = "fill")
    // A little pop when it becomes checked (not when the screen first shows it already checked).
    val pop = remember { Animatable(1f) }
    var first by remember { mutableStateOf(true) }
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(checked) {
        if (first) first = false
        else if (checked) { haptic.performHapticFeedback(HapticFeedbackType.Confirm); pop.snapTo(0.88f); pop.animateTo(1f, spring(dampingRatio = 0.65f, stiffness = 600f)) }
    }
    Box(
        Modifier
            .size(size)
            .graphicsLayer { scaleX = pop.value; scaleY = pop.value }
            .background(AppColors.text.copy(alpha = fill), CircleShape)
            .border(BorderStroke(1.5.dp, AppColors.text), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (fill > 0.4f) AppIcon(Ic.Check, AppColors.phone, size = size * 0.52f)
    }
}

/** Round "- 5 +" control. */
@Composable
fun Stepper(value: String, onMinus: () -> Unit, onPlus: () -> Unit, modifier: Modifier = Modifier, valueWidth: Dp = 72.dp) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StepBtn(Ic.Minus, "Less", onMinus)
        AppText(value, Modifier.width(valueWidth), size = 13.sp, color = AppColors.text, align = TextAlign.Center, mono = true)
        StepBtn(Ic.Plus, "More", onPlus)
    }
}

@Composable
private fun StepBtn(ic: Ic, description: String, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Box(
        Modifier
            .size(44.dp)
            .semantics { contentDescription = description }
            .clickableRole({ haptic.performHapticFeedback(HapticFeedbackType.SegmentTick); onClick() }),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(34.dp).border(1.dp, AppColors.line, CircleShape), contentAlignment = Alignment.Center) { AppIcon(ic, AppColors.text, size = 16.dp) }
    }
}

/** The round-square letter icon used for apps. Gated apps use the terracotta colour. */
@Composable
fun AppBadge(letter: String, gated: Boolean, size: Dp = 44.dp, modifier: Modifier = Modifier) {
    val fontSize = with(LocalDensity.current) { (size.toPx() * 0.4f).toSp() }
    Box(
        modifier
            .size(size)
            .background(if (gated) AppColors.gate else AppColors.card, RoundedCornerShape(size * 0.3f)),
        contentAlignment = Alignment.Center,
    ) {
        AppText(letter, size = fontSize, weight = FontWeight.SemiBold, color = if (gated) AppColors.onGate else AppColors.text)
    }
}

/** "GATED" tag shown next to gated apps. Red is never the only signal. */
@Composable
fun GatedTag(modifier: Modifier = Modifier) {
    Row(
        modifier.border(1.dp, AppColors.gate.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        AppIcon(Ic.Lock, AppColors.gate, size = 11.dp)
        AppText("GATED", size = 10.sp, weight = FontWeight.Medium, color = AppColors.gate, letterSpacing = 1.sp, mono = true)
    }
}

/** A text input in the Ceramic style. */
@Composable
fun AppField(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    onDone: (() -> Unit)? = null,
    fill: Color = Color.Unspecified,
    mono: Boolean = false,
    center: Boolean = false,
) {
    BasicTextField(
        value = value,
        onValueChange = onChange,
        singleLine = singleLine,
        textStyle = TextStyle(
            color = AppColors.text,
            fontSize = 16.sp,
            fontFamily = if (mono) Mono else Sans,
            textAlign = if (center) TextAlign.Center else TextAlign.Start,
        ),
        cursorBrush = SolidColor(AppColors.text),
        keyboardOptions = KeyboardOptions(imeAction = if (onDone != null) ImeAction.Done else ImeAction.Default),
        keyboardActions = KeyboardActions(onDone = { onDone?.invoke() }),
        modifier = modifier.fillMaxWidth(),
        decorationBox = { inner ->
            Box(
                Modifier.fillMaxWidth().heightIn(min = 54.dp).then(if (fill == Color.Unspecified) Modifier.hairlineBelow().padding(vertical = 14.dp) else Modifier.background(fill, RoundedCornerShape(18.dp)).padding(horizontal = 18.dp, vertical = 14.dp)),
                contentAlignment = if (center) Alignment.Center else Alignment.CenterStart,
            ) {
                if (value.isEmpty()) AppText(placeholder, color = AppColors.muted, mono = mono)
                inner()
            }
        },
    )
}

val LocalRowLines = androidx.compose.runtime.compositionLocalOf { true }

/** Settings row: title, optional subtitle and a trailing slot. */
@Composable
fun SettingRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    titleColor: Color = AppColors.text,
    trailing: @Composable () -> Unit = {},
) {
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .then(if (onClick != null) Modifier.clickableRole(onClick) else Modifier)
            .then(if (LocalRowLines.current) Modifier.hairlineBelow() else Modifier)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            AppText(title, size = 17.sp, letterSpacing = (-0.17).sp, color = titleColor)
            if (subtitle != null) AppText(subtitle, size = 13.sp, color = AppColors.muted, lineHeight = 18.sp)
        }
        trailing()
    }
}

/** Value text with a chevron, used as the trailing part of a navigation row. */
@Composable
fun ValueChevron(value: String, color: Color = AppColors.muted) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (value.isNotEmpty()) AppText(value, Modifier.widthIn(max = 150.dp), size = 15.sp, weight = FontWeight.Medium, color = color, maxLines = 1)
        AppIcon(Ic.Chevron, color, size = 18.dp)
    }
}

@Composable
fun SettingsGroup(label: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().enterStagger(0), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Rule()
        Caption(label, Modifier.padding(top = 18.dp, bottom = 4.dp))
        Column(Modifier.fillMaxWidth(), content = content)
    }
}

@Composable
fun Divider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.line))
}


/** A value that rolls up to its new text (a counter going from "1 of 3" to "2 of 3") instead of just changing. */
@Composable
fun Rolling(value: String, modifier: Modifier = Modifier, content: @Composable (String) -> Unit) {
    androidx.compose.animation.AnimatedContent(
        targetState = value,
        modifier = modifier,
        transitionSpec = {
            (androidx.compose.animation.slideInVertically(Motion.enter(220)) { it / 2 } + androidx.compose.animation.fadeIn(Motion.enter(200))) togetherWith
                (androidx.compose.animation.slideOutVertically(Motion.enter(220)) { -it / 2 } + androidx.compose.animation.fadeOut(Motion.state(120)))
        },
        label = "rolling",
    ) { content(it) }
}

/** A one-pixel line along the bottom edge: the Still way of separating list rows. */
fun Modifier.hairlineBelow(): Modifier = composed {
    val c = AppColors.line
    drawBehind { drawLine(c, androidx.compose.ui.geometry.Offset(0f, size.height), androidx.compose.ui.geometry.Offset(size.width, size.height), 1.dp.toPx()) }
}
