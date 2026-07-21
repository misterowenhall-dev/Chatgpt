package com.pianopath.app

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Piano
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Usb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MidiRequiredScreen(midiController: MidiController) {
    val infinite = rememberInfiniteTransition(label = "midiPulse")
    val pulse by infinite.animateFloat(
        initialValue = .94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "midiScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF17172D), Color(0xFF30226F))))
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 30.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp,
                start = 24.dp,
                end = 24.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(.35f))
        Box(
            modifier = Modifier
                .size(142.dp)
                .graphicsLayer { scaleX = pulse; scaleY = pulse }
                .clip(CircleShape)
                .background(Color.White.copy(alpha = .12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Piano, null, tint = Color.White, modifier = Modifier.size(74.dp))
        }
        Spacer(Modifier.height(28.dp))
        Text(
            "Connect your electric piano",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 30.sp,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp
        )
        Text(
            "PianoPath is MIDI-only. Lessons remain locked until a real piano is connected, so every exercise can verify exactly what you play.",
            color = Color.White.copy(alpha = .76f),
            fontSize = 16.sp,
            lineHeight = 23.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 14.dp)
        )
        Spacer(Modifier.height(30.dp))
        Surface(
            color = Color.White.copy(alpha = .09f),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
                SetupStep("1", "Use the piano’s USB TO HOST or USB MIDI port")
                SetupStep("2", "Connect it to your phone with a USB-C data cable or OTG adapter")
                SetupStep("3", "Turn the piano on, then tap Refresh connection")
            }
        }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = midiController::refresh,
            modifier = Modifier.fillMaxWidth().height(58.dp),
            shape = RoundedCornerShape(17.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = PurpleDark)
        ) {
            Icon(Icons.Rounded.Refresh, null)
            Spacer(Modifier.width(8.dp))
            Text("Refresh connection", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
        }
        Text(
            "No microphone mode · no on-screen piano",
            color = Color.White.copy(alpha = .52f),
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 13.dp)
        )
    }
}

@Composable
private fun SetupStep(number: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.White.copy(alpha = .14f)),
            contentAlignment = Alignment.Center
        ) {
            Text(number, color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
        }
        Spacer(Modifier.width(12.dp))
        Text(text, color = Color.White.copy(alpha = .88f), fontSize = 14.sp, lineHeight = 20.sp)
    }
}

@Composable
fun CalibrationScreen(midiController: MidiController, onComplete: () -> Unit) {
    val played = remember { mutableStateListOf<Int>() }
    DisposableEffect(midiController) {
        val listener: (MidiNoteEvent) -> Unit = { event ->
            if (event.note !in played) played.add(event.note)
            if (played.size >= 3) onComplete()
        }
        midiController.addNoteListener(listener)
        onDispose { midiController.removeNoteListener(listener) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 24.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp,
                start = 22.dp,
                end = 22.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(shape = CircleShape, color = GreenSoft) {
            Icon(Icons.Rounded.Usb, null, tint = Green, modifier = Modifier.padding(16.dp).size(35.dp))
        }
        Spacer(Modifier.height(18.dp))
        Text("Piano connected", color = Green, fontWeight = FontWeight.Black, fontSize = 14.sp)
        Text(
            midiController.deviceName,
            color = Ink,
            fontWeight = FontWeight.Black,
            fontSize = 27.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp)
        )
        Text(
            "Play any three different keys. This confirms that note and velocity data are reaching the app correctly.",
            color = Muted,
            fontSize = 16.sp,
            lineHeight = 23.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp)
        )
        Spacer(Modifier.weight(.6f))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(if (index < played.size) Green else Line),
                    contentAlignment = Alignment.Center
                ) {
                    if (index < played.size) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.Check, null, tint = Color.White)
                            Text(midiNoteName(played[index]), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    } else {
                        Text("${index + 1}", color = Muted, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    }
                }
            }
        }
        Spacer(Modifier.height(22.dp))
        LinearProgressIndicator(
            progress = { played.size / 3f },
            modifier = Modifier.fillMaxWidth().height(12.dp).clip(CircleShape),
            color = Green,
            trackColor = Line
        )
        Text(
            if (played.isEmpty()) "Waiting for your piano…" else "${played.size} of 3 different keys detected",
            color = Muted,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 12.dp)
        )
        Spacer(Modifier.weight(1f))
        Surface(shape = RoundedCornerShape(18.dp), color = CardWhite, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.AutoAwesome, null, tint = Purple)
                Spacer(Modifier.width(12.dp))
                Text("Calibration finishes automatically after the third different key.", color = Ink, fontSize = 14.sp, lineHeight = 20.sp)
            }
        }
    }
}

@Composable
fun CourseHome(
    progress: ProgressState,
    midiController: MidiController,
    onLesson: (Lesson) -> Unit,
    onReset: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Paper),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 18.dp,
            end = 18.dp,
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp,
            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 26.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("PianoPath", color = Ink, fontWeight = FontWeight.Black, fontSize = 30.sp)
                    Text("Unit 1 · First notes", color = Muted, fontSize = 14.sp)
                }
                Surface(shape = RoundedCornerShape(50), color = PurpleSoft) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Star, null, tint = Purple, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("${progress.xp} XP", color = PurpleDark, fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                }
            }
        }
        item {
            Surface(shape = RoundedCornerShape(16.dp), color = GreenSoft) {
                Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Usb, null, tint = Green, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(9.dp))
                    Column(Modifier.weight(1f)) {
                        Text("MIDI ready", color = Green, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                        Text(midiController.deviceName, color = Ink, fontSize = 12.sp)
                    }
                }
            }
        }
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(PurpleDark, Purple, Blue)))
                        .padding(22.dp)
                ) {
                    Column {
                        Text("UNIT 1", color = Color.White.copy(alpha = .66f), fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.2.sp)
                        Text("Your first notes", color = Color.White, fontWeight = FontWeight.Black, fontSize = 28.sp, modifier = Modifier.padding(top = 5.dp))
                        Text(
                            "Learn middle C, read C–D–E, build a steady beat, and perform your first melody.",
                            color = Color.White.copy(alpha = .83f),
                            fontSize = 15.sp,
                            lineHeight = 21.sp,
                            modifier = Modifier.padding(top = 9.dp)
                        )
                        Spacer(Modifier.height(18.dp))
                        val complete = progress.completedLessonCount.coerceAtMost(CourseData.lessons.size)
                        LinearProgressIndicator(
                            progress = { complete / CourseData.lessons.size.toFloat() },
                            modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = .22f)
                        )
                        Text("$complete of ${CourseData.lessons.size} lessons mastered", color = Color.White.copy(alpha = .76f), fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }
        item {
            Text("LEARNING PATH", color = Muted, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 1.sp, modifier = Modifier.padding(top = 5.dp, start = 3.dp))
        }
        CourseData.lessons.forEach { lesson ->
            item {
                val unlocked = lesson.id <= progress.completedLessonCount
                val complete = lesson.id < progress.completedLessonCount
                LessonCard(
                    lesson = lesson,
                    unlocked = unlocked,
                    complete = complete,
                    stars = progress.bestStars[lesson.id] ?: 0,
                    onClick = { if (unlocked) onLesson(lesson) }
                )
            }
        }
        item {
            Surface(shape = RoundedCornerShape(22.dp), color = CardWhite) {
                Column(Modifier.padding(18.dp)) {
                    Text("Why only one unit right now?", color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Text(
                        "This build focuses on making the lesson engine reliable and genuinely useful before adding hundreds of units. Every future unit will use the same MIDI-only, adaptive system.",
                        color = Muted,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        modifier = Modifier.padding(top = 7.dp)
                    )
                }
            }
        }
        item {
            TextButton(onClick = onReset, modifier = Modifier.fillMaxWidth()) {
                Text("Reset calibration and progress", color = Muted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun LessonCard(
    lesson: Lesson,
    unlocked: Boolean,
    complete: Boolean,
    stars: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().then(if (unlocked) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = if (unlocked) CardWhite else Color(0xFFF0F1F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (unlocked) 2.dp else 0.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            complete -> Green
                            unlocked -> Purple
                            else -> Locked
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    when {
                        complete -> Icons.Rounded.Check
                        unlocked -> Icons.Rounded.PlayArrow
                        else -> Icons.Rounded.Lock
                    },
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(29.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(lesson.title, color = if (unlocked) Ink else Muted, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                Text(lesson.subtitle, color = Muted, fontSize = 12.sp, lineHeight = 17.sp, modifier = Modifier.padding(top = 4.dp))
                Text("${lesson.challenges.size} exercises · ${lesson.xp} XP", color = if (unlocked) Purple else Muted, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(top = 7.dp))
            }
            if (complete) {
                Row {
                    repeat(3) { index ->
                        Icon(Icons.Rounded.Star, null, tint = if (index < stars) Gold else Line, modifier = Modifier.size(17.dp))
                    }
                }
            }
        }
    }
}
