package com.example.productivitylauncher.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * One motion vocabulary for the whole app. Strong curves (the built-in ones are too weak),
 * short durations (UI stays under 300 ms), and springs for anything a finger can interrupt.
 * Compose follows the system "animator duration scale", and Settings > Animations turns every spec here into a snap.
 */
object Motion {
    /**
     * The one switch behind Settings > Animations. When it is off every spec below snaps straight to its end value,
     * infinite loops stand still and staggers do not wait. Read in composition, so flipping it updates the screen at once.
     */
    var enabled by mutableStateOf(true)

    /** The prototype's pace: everything takes a little longer and lands softly. */
    private fun slowed(ms: Int): Int = (ms * 1.9f).toInt()

    /** Strong ease-out for things that enter, exit or respond to a tap. */
    val EaseOut = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)

    /** Strong ease-in-out for things that move or morph on screen. */
    val EaseInOut = CubicBezierEasing(0.77f, 0f, 0.175f, 1f)

    /** Soft, continuous curve for surfaces that move or morph: starts quickly, then settles very gently. */
    val Soft = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

    /** Even softer landing for things that arrive: a long, quiet tail. */
    val SoftOut = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

    /** Press feedback: quick and settled. Interruptible, so a second tap never restarts from zero. */
    fun <T> press(): FiniteAnimationSpec<T> = if (enabled) spring(dampingRatio = 0.85f, stiffness = 700f) else snap()

    /** A colour or small state change. */
    fun <T> state(ms: Int = 180): FiniteAnimationSpec<T> = if (enabled) tween(slowed(ms), easing = SoftOut) else snap()

    /** Something appearing on a screen the person sees now and then. */
    fun <T> enter(ms: Int = 240): FiniteAnimationSpec<T> = if (enabled) tween(slowed(ms), easing = SoftOut) else snap()

    /** Something leaving: a little quicker than it arrived, so the next thing is not kept waiting. */
    fun <T> exit(ms: Int = 200): FiniteAnimationSpec<T> = if (enabled) tween(slowed(ms), easing = Soft) else snap()

    /** One surface turning into another (a step becoming the next step). */
    fun <T> morph(ms: Int = 320): FiniteAnimationSpec<T> = if (enabled) tween(slowed(ms), easing = Soft) else snap()

    /** A bar or ring filling to its value. */
    fun <T> fill(ms: Int = 360): FiniteAnimationSpec<T> = if (enabled) tween(slowed(ms), easing = SoftOut) else snap()

    /** A small, gentle spring for drag lift and similar. Bounce stays low. */
    fun <T> lift(): FiniteAnimationSpec<T> = if (enabled) spring(dampingRatio = 0.8f, stiffness = 500f) else snap()

    /** Pressed scale: subtle and tactile. */
    const val PressScale = 0.975f

    /** A long, slow move such as a breathing circle or a rolling digit. With animations off it snaps. */
    fun <T> slow(ms: Int): FiniteAnimationSpec<T> = if (enabled) tween(ms, easing = Soft) else snap()
}

/** True once the launch intro has played in this run, so Home only introduces itself once, not on every return. */
object Intro {
    var played = false
}

/**
 * Fades and lifts an element in, one after another by [index] (35 ms apart, capped).
 * With [once] it only plays during the very first screen of a run, because Home is opened hundreds of times a day.
 */
fun Modifier.enterStagger(index: Int, once: Boolean = false, rise: Dp = 18.dp): Modifier = composed {
    val skip = (once && Intro.played) || !Motion.enabled
    var shown by remember { mutableStateOf(skip) }
    LaunchedEffect(Unit) {
        if (!skip) { delay(60L + 70L * index.coerceIn(0, 10)); shown = true }
    }
    val a by animateFloatAsState(if (shown) 1f else 0f, Motion.enter(420), label = "enter")
    Modifier.graphicsLayer { alpha = a; translationY = (1f - a) * rise.toPx() }
}
