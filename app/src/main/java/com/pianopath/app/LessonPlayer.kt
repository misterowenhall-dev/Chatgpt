package com.pianopath.app

import android.graphics.Paint
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun LessonPlayer(
    lesson: Lesson,
    midiController: MidiController,
    onExit: () -> Unit,
    onFinished: (Int) -> Unit
) {
    BackHandler(onBack = onExit)
    val queue = remember(lesson.id) { mutableStateListOf<Challenge>().apply { addAll(lesson.challenges) } }
    val reviewedIds = remember(lesson.id) { mutableSetOf<String>() }
    var queueIndex by remember(lesson.id) { mutableIntStateOf(0) }
    var totalMistakes by remember(lesson.id) { mutableIntStateOf(0) }
    var challengeMistakes by remember(lesson.id) { mutableIntStateOf(0) }
    var finished by remember(lesson.id) { mutableStateOf(false) }
    var stars by remember(lesson.id) { mutableIntStateOf(3) }

    val challenge = queue.getOrNull(queueIndex)
    val progress = if (queue.isEmpty()) 0f else queueIndex.toFloat() / queue.size.toFloat()

    fun completeChallenge() {
        if (challenge != null && challengeMistakes >= 2 && challenge.id !in reviewedIds) {
            reviewedIds.add(challenge.id)
            queue.add(challenge)
        }
        challengeMistakes = 0
        if (queueIndex >= queue.lastIndex) {
            stars = when {
                totalMistakes <= 1 -> 3
                totalMistakes <= 5 -> 2
                else -> 1
            }
            finished = true
        } else {
            queueIndex++
        }
    }

    fun mistake() {
        challengeMistakes++
        totalMistakes++
    }

    Box(Modifier.fillMaxSize().background(Paper)) {
        if (!finished && challenge != null) {
            Column(Modifier.fillMaxSize()) {
                LessonHeader(
                    title = lesson.title,
                    progress = progress,
                    isReview = queueIndex >= lesson.challenges.size,
                    onExit = onExit
                )
                Box(Modifier.weight(1f)) {
                    ChallengeContent(
                        key = "${challenge.id}-$queueIndex",
                        challenge = challenge,
                        midiController = midiController,
                        mistakeCount = challengeMistakes,
                        onMistake = ::mistake,
                        onDone = ::completeChallenge
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = finished,
            enter = fadeIn() + scaleIn(initialScale = .92f),
            exit = fadeOut()
        ) {
            LessonCompleteScreen(
                lesson = lesson,
                stars = stars,
                mistakes = totalMistakes,
                reviews = reviewedIds.size,
                onFinish = { onFinished(stars) }
            )
        }
    }
}

@Composable
private fun LessonHeader(title: String, progress: Float, isReview: Boolean, onExit: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(CardWhite)
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onExit) { Icon(Icons.Rounded.Close, null, tint = Muted) }
            Column(Modifier.weight(1f)) {
                Text(title, color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                if (isReview) Text("Personalised review", color = Purple, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
            Text("${(progress * 100).roundToInt()}%", color = Purple, fontWeight = FontWeight.Black, fontSize = 13.sp)
        }
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(7.dp),
            color = Purple,
            trackColor = Line
        )
    }
}

@Composable
private fun ChallengeContent(
    key: String,
    challenge: Challenge,
    midiController: MidiController,
    mistakeCount: Int,
    onMistake: () -> Unit,
    onDone: () -> Unit
) {
    when (challenge.type) {
        ChallengeType.COACH -> CoachChallenge(challenge, onDone)
        ChallengeType.FIND_NOTE,
        ChallengeType.NOTE_SEQUENCE,
        ChallengeType.SIGHT_READ -> GuidedNoteChallenge(key, challenge, midiController, mistakeCount, onMistake, onDone)
        ChallengeType.RHYTHM,
        ChallengeType.PERFORMANCE -> TimedChallenge(key, challenge, midiController, mistakeCount, onMistake, onDone)
    }
}

@Composable
private fun CoachChallenge(challenge: Challenge, onDone: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(10.dp))
        Surface(shape = CircleShape, color = PurpleSoft) {
            Icon(Icons.Rounded.Lightbulb, null, tint = Purple, modifier = Modifier.padding(17.dp).size(34.dp))
        }
        Text(challenge.title, color = Ink, fontWeight = FontWeight.Black, fontSize = 29.sp, textAlign = TextAlign.Center, lineHeight = 35.sp, modifier = Modifier.padding(top = 20.dp))
        Text(challenge.instruction, color = Muted, fontSize = 17.sp, lineHeight = 25.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 14.dp))
        Spacer(Modifier.height(28.dp))
        Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = PurpleSoft), modifier = Modifier.fillMaxWidth().height(245.dp)) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.MusicNote, null, tint = Purple, modifier = Modifier.size(85.dp))
                    Text("Learn a little, then play", color = PurpleDark, fontWeight = FontWeight.Black, fontSize = 20.sp, modifier = Modifier.padding(top = 15.dp))
                }
            }
        }
        Spacer(Modifier.weight(1f))
        HintCard(challenge.hint)
        Spacer(Modifier.height(14.dp))
        PrimaryButton("Try it on my piano", onDone)
        Spacer(Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()))
    }
}

@Composable
private fun GuidedNoteChallenge(
    key: String,
    challenge: Challenge,
    midiController: MidiController,
    mistakeCount: Int,
    onMistake: () -> Unit,
    onDone: () -> Unit
) {
    var noteIndex by remember(key) { mutableIntStateOf(0) }
    var feedback by remember(key) { mutableStateOf("Play ${targetLabel(challenge, 0)}") }
    var correctFlash by remember(key) { mutableStateOf(false) }
    val target = challenge.notes.getOrNull(noteIndex)
    val haptic = LocalHapticFeedback.current

    DisposableEffect(key, midiController, noteIndex) {
        val listener: (MidiNoteEvent) -> Unit = listener@{ event ->
            val expected = challenge.notes.getOrNull(noteIndex) ?: return@listener
            if (event.note == expected) {
                correctFlash = true
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                if (noteIndex >= challenge.notes.lastIndex) {
                    feedback = challenge.successText
                    onDone()
                } else {
                    noteIndex++
                    feedback = "Good — now ${targetLabel(challenge, noteIndex)}"
                }
            } else {
                correctFlash = false
                onMistake()
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                feedback = "That was ${midiNoteName(event.note)}. Try ${targetLabel(challenge, noteIndex)}"
            }
        }
        midiController.addNoteListener(listener)
        onDispose { midiController.removeNoteListener(listener) }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(challenge.title, color = Ink, fontWeight = FontWeight.Black, fontSize = 27.sp, textAlign = TextAlign.Center)
        Text(challenge.instruction, color = Muted, fontSize = 15.sp, lineHeight = 21.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 9.dp))
        Spacer(Modifier.height(18.dp))

        if (challenge.type == ChallengeType.FIND_NOTE) {
            TargetNoteCard(target ?: 60, challenge.showNames)
        } else {
            StaffNotation(
                notes = challenge.notes,
                currentIndex = noteIndex,
                showNames = challenge.showNames,
                modifier = Modifier.fillMaxWidth().height(210.dp)
            )
        }

        Spacer(Modifier.height(16.dp))
        ExerciseProgress(challenge.notes.size, noteIndex)
        Spacer(Modifier.weight(1f))
        FeedbackCard(feedback, correctFlash)
        if (mistakeCount >= 2) {
            Spacer(Modifier.height(10.dp))
            HintCard(challenge.hint)
        }
        Text(
            "Listening to ${midiController.deviceName}",
            color = Green,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 13.dp)
        )
        Spacer(Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 12.dp))
    }
}

private fun targetLabel(challenge: Challenge, index: Int): String {
    val note = challenge.notes.getOrNull(index) ?: 60
    return if (challenge.showNames) midiNoteName(note) else "the highlighted note"
}

@Composable
private fun TargetNoteCard(note: Int, showName: Boolean) {
    Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = CardWhite), elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth().height(230.dp)) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            StaffNotation(listOf(note), 0, showName, Modifier.fillMaxWidth().height(145.dp))
            if (showName) Text(midiNoteName(note), color = Purple, fontWeight = FontWeight.Black, fontSize = 24.sp)
        }
    }
}

@Composable
private fun TimedChallenge(
    key: String,
    challenge: Challenge,
    midiController: MidiController,
    mistakeCount: Int,
    onMistake: () -> Unit,
    onDone: () -> Unit
) {
    val metronome = remember(key) { Metronome() }
    var runId by remember(key) { mutableIntStateOf(0) }
    var started by remember(key) { mutableStateOf(false) }
    var countIn by remember(key) { mutableIntStateOf(4) }
    var noteIndex by remember(key) { mutableIntStateOf(0) }
    var startTime by remember(key) { mutableLongStateOf(0L) }
    var feedback by remember(key) { mutableStateOf("Tap Start when your hand is ready") }
    var correctFlash by remember(key) { mutableStateOf(false) }
    var completed by remember(key) { mutableStateOf(false) }
    val interval = remember(challenge.bpm) { (60_000L / challenge.bpm.coerceAtLeast(1)) }
    val tolerance = remember(interval) { (interval * .36f).toLong().coerceIn(220L, 380L) }

    DisposableEffect(key, midiController, started, startTime, noteIndex) {
        val listener: (MidiNoteEvent) -> Unit = listener@{ event ->
            if (!started || startTime == 0L || completed) return@listener
            val expected = challenge.notes.getOrNull(noteIndex) ?: return@listener
            val targetTime = startTime + noteIndex * interval
            val delta = event.timestampMs - targetTime
            if (event.note != expected) {
                correctFlash = false
                feedback = "Wrong note: ${midiNoteName(event.note)}. Expected ${midiNoteName(expected)}"
                onMistake()
                return@listener
            }

            if (abs(delta) > tolerance) {
                onMistake()
                correctFlash = false
                feedback = if (delta < 0) "Correct note, but a little early" else "Correct note, but a little late"
            } else {
                correctFlash = true
                feedback = if (abs(delta) < tolerance / 3) "Right on the beat" else "Good — keep the pulse"
            }

            if (noteIndex >= challenge.notes.lastIndex) {
                completed = true
                feedback = challenge.successText
                onDone()
            } else {
                noteIndex++
            }
        }
        midiController.addNoteListener(listener)
        onDispose { midiController.removeNoteListener(listener) }
    }

    DisposableEffect(metronome) { onDispose { metronome.close() } }

    LaunchedEffect(runId) {
        if (runId == 0) return@LaunchedEffect
        started = false
        completed = false
        noteIndex = 0
        startTime = 0L
        countIn = 4
        feedback = "Listen to the count-in"
        repeat(4) { beat ->
            countIn = 4 - beat
            metronome.click(accent = beat == 0)
            delay(interval)
        }
        countIn = 0
        startTime = SystemClock.elapsedRealtime()
        started = true
        feedback = "Play now"
        challenge.notes.indices.forEach { beat ->
            metronome.click(accent = beat % 4 == 0)
            delay(interval)
        }
    }

    LaunchedEffect(runId, started, noteIndex, startTime) {
        if (!started || startTime == 0L || completed) return@LaunchedEffect
        while (started && !completed && noteIndex < challenge.notes.size) {
            val targetTime = startTime + noteIndex * interval
            if (SystemClock.elapsedRealtime() > targetTime + tolerance) {
                onMistake()
                correctFlash = false
                feedback = "Missed beat — keep listening"
                if (noteIndex >= challenge.notes.lastIndex) {
                    started = false
                    feedback = "Try the phrase again"
                } else {
                    noteIndex++
                }
            }
            delay(30)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(challenge.title, color = Ink, fontWeight = FontWeight.Black, fontSize = 27.sp, textAlign = TextAlign.Center)
        Text(challenge.instruction, color = Muted, fontSize = 15.sp, lineHeight = 21.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 9.dp))
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(shape = RoundedCornerShape(50), color = PurpleSoft) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Timer, null, tint = Purple, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("${challenge.bpm} BPM", color = PurpleDark, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }
            Surface(shape = RoundedCornerShape(50), color = GreenSoft) {
                Text("MIDI timing", color = Green, fontWeight = FontWeight.Black, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        StaffNotation(challenge.notes, noteIndex, challenge.showNames, Modifier.fillMaxWidth().height(210.dp))
        Spacer(Modifier.height(12.dp))
        if (runId > 0 && !started && countIn > 0) {
            Text(countIn.toString(), color = Purple, fontWeight = FontWeight.Black, fontSize = 58.sp)
        } else {
            ExerciseProgress(challenge.notes.size, noteIndex)
        }
        Spacer(Modifier.weight(1f))
        FeedbackCard(feedback, correctFlash)
        if (mistakeCount >= 2) {
            Spacer(Modifier.height(10.dp))
            HintCard(challenge.hint)
        }
        Spacer(Modifier.height(14.dp))
        if (runId == 0 || (!started && countIn == 0 && !completed)) {
            PrimaryButton(if (runId == 0) "Start with count-in" else "Try again") { runId++ }
        } else if (started) {
            Surface(shape = RoundedCornerShape(16.dp), color = PurpleSoft, modifier = Modifier.fillMaxWidth()) {
                Text("Keep playing — the app is measuring every note and beat", color = PurpleDark, fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(14.dp))
            }
        }
        Spacer(Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 10.dp))
    }
}

@Composable
private fun ExerciseProgress(total: Int, current: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
        repeat(total.coerceAtMost(12)) { index ->
            Box(
                Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(if (index < current) Green else if (index == current) Purple else Line)
            )
        }
    }
}

@Composable
private fun FeedbackCard(text: String, correct: Boolean) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (correct) GreenSoft else CardWhite,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (correct) Icons.Rounded.Check else Icons.Rounded.MusicNote, null, tint = if (correct) Green else Purple)
            Spacer(Modifier.width(10.dp))
            Text(text, color = Ink, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 19.sp)
        }
    }
}

@Composable
private fun HintCard(text: String) {
    if (text.isBlank()) return
    Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFFFFF5D9), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Lightbulb, null, tint = Gold, modifier = Modifier.size(21.dp))
            Spacer(Modifier.width(10.dp))
            Text(text, color = Ink, fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(58.dp),
        shape = RoundedCornerShape(17.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Purple, contentColor = Color.White)
    ) {
        Text(text, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
        Spacer(Modifier.width(7.dp))
        Icon(Icons.Rounded.ArrowForward, null)
    }
}

@Composable
private fun LessonCompleteScreen(
    lesson: Lesson,
    stars: Int,
    mistakes: Int,
    reviews: Int,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 30.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 22.dp,
                start = 22.dp,
                end = 22.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(.4f))
        Box(Modifier.size(132.dp).clip(CircleShape).background(GreenSoft), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.EmojiEvents, null, tint = Green, modifier = Modifier.size(70.dp))
        }
        Text("Lesson mastered", color = Ink, fontWeight = FontWeight.Black, fontSize = 31.sp, modifier = Modifier.padding(top = 22.dp))
        Text(lesson.title, color = Muted, fontSize = 16.sp, modifier = Modifier.padding(top = 7.dp))
        Row(Modifier.padding(top = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) { index ->
                Icon(Icons.Rounded.Star, null, tint = if (index < stars) Gold else Line, modifier = Modifier.size(42.dp))
            }
        }
        Spacer(Modifier.height(26.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            ResultStat("+${lesson.xp}", "XP", Purple, Modifier.weight(1f))
            ResultStat(mistakes.toString(), "Mistakes", if (mistakes <= 2) Green else Red, Modifier.weight(1f))
            ResultStat(reviews.toString(), "Reviews", Blue, Modifier.weight(1f))
        }
        if (reviews > 0) {
            Surface(shape = RoundedCornerShape(18.dp), color = PurpleSoft, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                Text(
                    "PianoPath repeated $reviews weak ${if (reviews == 1) "exercise" else "exercises"} before granting mastery.",
                    color = PurpleDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(15.dp)
                )
            }
        }
        Spacer(Modifier.weight(1f))
        PrimaryButton("Return to learning path", onFinish)
    }
}

@Composable
private fun ResultStat(value: String, label: String, color: Color, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
        Column(Modifier.padding(vertical = 15.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = color, fontWeight = FontWeight.Black, fontSize = 21.sp)
            Text(label, color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp))
        }
    }
}

@Composable
fun StaffNotation(
    notes: List<Int>,
    currentIndex: Int,
    showNames: Boolean,
    modifier: Modifier = Modifier
) {
    Card(shape = RoundedCornerShape(25.dp), colors = CardDefaults.cardColors(containerColor = CardWhite), elevation = CardDefaults.cardElevation(2.dp), modifier = modifier) {
        Canvas(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 12.dp)) {
            val left = 26.dp.toPx()
            val right = size.width - 14.dp.toPx()
            val spacing = 15.dp.toPx()
            val middleY = size.height * .48f
            val topLine = middleY - 2 * spacing
            repeat(5) { line ->
                val y = topLine + line * spacing
                drawLine(Line, Offset(left, y), Offset(right, y), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
            }

            val clefPaint = Paint().apply {
                isAntiAlias = true
                color = android.graphics.Color.rgb(24, 32, 51)
                textSize = 54.sp.toPx()
                textAlign = Paint.Align.CENTER
            }
            drawContext.canvas.nativeCanvas.drawText("𝄞", left + 17.dp.toPx(), middleY + 19.dp.toPx(), clefPaint)

            val available = right - left - 62.dp.toPx()
            val count = notes.size.coerceAtLeast(1)
            val stepX = (available / count).coerceAtMost(54.dp.toPx())
            val startX = left + 55.dp.toPx() + (available - stepX * count) / 2f + stepX / 2

            notes.forEachIndexed { index, note ->
                val x = startX + index * stepX
                val y = noteY(note, middleY, spacing)
                val active = index == currentIndex
                val done = index < currentIndex
                val color = when {
                    done -> Green
                    active -> Purple
                    else -> Ink.copy(alpha = .72f)
                }
                if (active) drawCircle(PurpleSoft, 17.dp.toPx(), Offset(x, y))
                drawOval(color, Offset(x - 8.dp.toPx(), y - 5.5.dp.toPx()), Size(16.dp.toPx(), 11.dp.toPx()))
                drawLine(color, Offset(x + 7.dp.toPx(), y), Offset(x + 7.dp.toPx(), y - 34.dp.toPx()), strokeWidth = 2.dp.toPx())

                if (note == 60) {
                    drawLine(Line, Offset(x - 13.dp.toPx(), y), Offset(x + 13.dp.toPx(), y), strokeWidth = 1.5.dp.toPx())
                }

                if (showNames) {
                    val labelPaint = Paint().apply {
                        isAntiAlias = true
                        this.color = android.graphics.Color.rgb(103, 80, 232)
                        textSize = 12.sp.toPx()
                        textAlign = Paint.Align.CENTER
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }
                    drawContext.canvas.nativeCanvas.drawText(midiNoteName(note).dropLast(1), x, size.height - 10.dp.toPx(), labelPaint)
                }
            }
        }
    }
}

private fun noteY(note: Int, middleY: Float, spacing: Float): Float {
    val diatonicIndex = when (note % 12) {
        0 -> 0
        2 -> 1
        4 -> 2
        5 -> 3
        7 -> 4
        9 -> 5
        11 -> 6
        else -> 0
    } + (note / 12 - 5) * 7
    return middleY - diatonicIndex * (spacing / 2f)
}
