package com.pianopath.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    private lateinit var midiController: MidiController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        midiController = MidiController(this)
        midiController.start()
        setContent {
            PianoPathTheme {
                PianoPathRoot(midiController)
            }
        }
    }

    override fun onDestroy() {
        midiController.close()
        super.onDestroy()
    }
}

class ProgressState(context: android.content.Context) {
    private val prefs = context.getSharedPreferences("pianopath_progress_v3", android.content.Context.MODE_PRIVATE)

    var xp by mutableIntStateOf(prefs.getInt("xp", 0))
        private set
    var calibrated by mutableStateOf(prefs.getBoolean("calibrated", false))
        private set
    var completedLessonCount by mutableIntStateOf(prefs.getInt("completed", 0))
        private set
    var activeLessonId by mutableIntStateOf(prefs.getInt("activeLesson", 0))
        private set
    var bestStars by mutableStateOf(readStars())
        private set

    fun completeLesson(lesson: Lesson, stars: Int) {
        if (lesson.id >= completedLessonCount) {
            completedLessonCount = lesson.id + 1
            xp += lesson.xp
        }
        activeLessonId = (lesson.id + 1).coerceAtMost(CourseData.lessons.lastIndex)
        bestStars = bestStars.toMutableMap().also { map ->
            map[lesson.id] = maxOf(map[lesson.id] ?: 0, stars)
        }
        persist()
    }

    fun markCalibrated() {
        calibrated = true
        persist()
    }

    fun reset() {
        xp = 0
        calibrated = false
        completedLessonCount = 0
        activeLessonId = 0
        bestStars = emptyMap()
        prefs.edit().clear().apply()
    }

    private fun readStars(): Map<Int, Int> {
        return prefs.getString("stars", "")
            .orEmpty()
            .split(',')
            .mapNotNull { token ->
                val parts = token.split(':')
                if (parts.size == 2) {
                    val id = parts[0].toIntOrNull()
                    val stars = parts[1].toIntOrNull()
                    if (id != null && stars != null) id to stars else null
                } else null
            }
            .toMap()
    }

    private fun persist() {
        prefs.edit()
            .putInt("xp", xp)
            .putBoolean("calibrated", calibrated)
            .putInt("completed", completedLessonCount)
            .putInt("activeLesson", activeLessonId)
            .putString("stars", bestStars.entries.joinToString(",") { "${it.key}:${it.value}" })
            .apply()
    }
}

@Composable
fun PianoPathRoot(midiController: MidiController) {
    val context = LocalContext.current
    val progress = remember { ProgressState(context) }
    var activeLesson by remember { mutableStateOf<Lesson?>(null) }

    when {
        !midiController.isConnected -> MidiRequiredScreen(midiController)
        !progress.calibrated -> CalibrationScreen(midiController, progress::markCalibrated)
        activeLesson != null -> LessonPlayer(
            lesson = activeLesson!!,
            midiController = midiController,
            onExit = { activeLesson = null },
            onFinished = { stars ->
                progress.completeLesson(activeLesson!!, stars)
                activeLesson = null
            }
        )
        else -> CourseHome(
            progress = progress,
            midiController = midiController,
            onLesson = { activeLesson = it },
            onReset = progress::reset
        )
    }
}
