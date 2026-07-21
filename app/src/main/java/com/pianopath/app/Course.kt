package com.pianopath.app

enum class ChallengeType {
    COACH,
    FIND_NOTE,
    NOTE_SEQUENCE,
    SIGHT_READ,
    RHYTHM,
    PERFORMANCE
}

data class Challenge(
    val id: String,
    val type: ChallengeType,
    val title: String,
    val instruction: String,
    val notes: List<Int> = emptyList(),
    val bpm: Int = 0,
    val showNames: Boolean = true,
    val hint: String = "",
    val successText: String = "Nice!"
)

data class Lesson(
    val id: Int,
    val title: String,
    val subtitle: String,
    val xp: Int,
    val challenges: List<Challenge>
)

object CourseData {
    val lessons = listOf(
        Lesson(
            id = 0,
            title = "Meet middle C",
            subtitle = "Keyboard pattern, hand position, and your first note",
            xp = 40,
            challenges = listOf(
                Challenge(
                    "0-coach",
                    ChallengeType.COACH,
                    "Set up at the piano",
                    "Sit tall but relaxed. Let your arms hang naturally and curve your fingers as if you were holding a small orange.",
                    hint = "Keep your shoulders loose and your wrists level."
                ),
                Challenge(
                    "0-find-c-1",
                    ChallengeType.FIND_NOTE,
                    "Find middle C",
                    "Find the group of two black keys nearest the middle of your piano. Play the white key immediately to their left.",
                    notes = listOf(60),
                    hint = "C is always directly to the left of a group of two black keys.",
                    successText = "That is middle C"
                ),
                Challenge(
                    "0-control",
                    ChallengeType.NOTE_SEQUENCE,
                    "Play with control",
                    "Play middle C four times. Release the key fully between each note.",
                    notes = listOf(60, 60, 60, 60),
                    hint = "Use your right-hand thumb and keep the other fingers relaxed.",
                    successText = "Four clean notes"
                ),
                Challenge(
                    "0-find-c-2",
                    ChallengeType.FIND_NOTE,
                    "Find C again",
                    "Move your hand away, then find and play middle C without using a highlighted keyboard.",
                    notes = listOf(60),
                    showNames = false,
                    hint = "Look for the two black keys first.",
                    successText = "You found it independently"
                ),
                Challenge(
                    "0-performance",
                    ChallengeType.PERFORMANCE,
                    "First performance",
                    "Play C four times with the metronome. Listen to the count-in, then play one note on each beat.",
                    notes = listOf(60, 60, 60, 60),
                    bpm = 64,
                    hint = "Wait for the four count-in clicks before playing.",
                    successText = "Your first timed performance"
                )
            )
        ),
        Lesson(
            id = 1,
            title = "C, D, and E",
            subtitle = "Read and play your first three notes",
            xp = 55,
            challenges = listOf(
                Challenge(
                    "1-coach",
                    ChallengeType.COACH,
                    "Three neighbouring notes",
                    "Place your right-hand thumb on C, finger 2 on D, and finger 3 on E. Keep every finger resting close to its key.",
                    hint = "Avoid lifting unused fingers high into the air."
                ),
                Challenge(
                    "1-cde",
                    ChallengeType.NOTE_SEQUENCE,
                    "Walk upward",
                    "Play C, D, E slowly. The app waits for the correct note, so focus on accuracy.",
                    notes = listOf(60, 62, 64),
                    hint = "Thumb, index, middle: fingers 1, 2, 3.",
                    successText = "C–D–E"
                ),
                Challenge(
                    "1-edc",
                    ChallengeType.NOTE_SEQUENCE,
                    "Walk downward",
                    "Now play E, D, C without moving your hand position.",
                    notes = listOf(64, 62, 60),
                    hint = "Finger 3, finger 2, thumb.",
                    successText = "E–D–C"
                ),
                Challenge(
                    "1-read-1",
                    ChallengeType.SIGHT_READ,
                    "Read the staff",
                    "Read each note from the staff and play it. The names are shown for now.",
                    notes = listOf(60, 62, 64, 62, 60),
                    hint = "Higher notes appear higher on the staff.",
                    successText = "You read five notes"
                ),
                Challenge(
                    "1-read-2",
                    ChallengeType.SIGHT_READ,
                    "Read without names",
                    "Play the notes shown on the staff. This time, the note names are hidden.",
                    notes = listOf(60, 64, 62, 64, 60),
                    showNames = false,
                    hint = "Use the note positions, not the letters.",
                    successText = "You read them without labels"
                ),
                Challenge(
                    "1-performance",
                    ChallengeType.PERFORMANCE,
                    "Three-note performance",
                    "After the count-in, play one note on each beat.",
                    notes = listOf(60, 62, 64, 62, 60, 60, 62, 64),
                    bpm = 68,
                    showNames = false,
                    hint = "Keep your hand still and let each finger do its own job.",
                    successText = "A complete eight-note phrase"
                )
            )
        ),
        Lesson(
            id = 2,
            title = "Feel the beat",
            subtitle = "Turn correct notes into steady music",
            xp = 60,
            challenges = listOf(
                Challenge(
                    "2-coach",
                    ChallengeType.COACH,
                    "Pulse before speed",
                    "Music moves through time. A steady beat matters more than playing fast. You will hear four count-in clicks before each exercise.",
                    hint = "Count 1–2–3–4 quietly while you listen."
                ),
                Challenge(
                    "2-c-beats",
                    ChallengeType.RHYTHM,
                    "Four steady Cs",
                    "Play middle C once on every beat.",
                    notes = listOf(60, 60, 60, 60),
                    bpm = 60,
                    hint = "Do not chase the click. Let your note land with it.",
                    successText = "Four steady beats"
                ),
                Challenge(
                    "2-cde-beats",
                    ChallengeType.RHYTHM,
                    "C–D–E in time",
                    "Play the notes in order, one per beat.",
                    notes = listOf(60, 62, 64, 62),
                    bpm = 64,
                    hint = "Prepare the next finger while the current note sounds.",
                    successText = "Notes and rhythm together"
                ),
                Challenge(
                    "2-read-rhythm",
                    ChallengeType.SIGHT_READ,
                    "Read a new pattern",
                    "Read and play this new pattern without note names.",
                    notes = listOf(64, 62, 60, 62, 64, 64),
                    showNames = false,
                    hint = "Look one note ahead whenever you can.",
                    successText = "New music, read on sight"
                ),
                Challenge(
                    "2-performance",
                    ChallengeType.PERFORMANCE,
                    "Rhythm checkpoint",
                    "Play the whole phrase at a steady tempo.",
                    notes = listOf(60, 60, 62, 64, 64, 62, 60, 62),
                    bpm = 70,
                    showNames = false,
                    hint = "Keep going even if one note is imperfect.",
                    successText = "Checkpoint passed"
                )
            )
        ),
        Lesson(
            id = 3,
            title = "Your first melody",
            subtitle = "Learn, practise, and perform a complete phrase",
            xp = 75,
            challenges = listOf(
                Challenge(
                    "3-coach",
                    ChallengeType.COACH,
                    "Learn in small pieces",
                    "Musicians rarely learn a whole piece at once. First learn short chunks, then connect them, then add a steady beat.",
                    hint = "Small accurate repetitions beat one long messy attempt."
                ),
                Challenge(
                    "3-chunk-a",
                    ChallengeType.NOTE_SEQUENCE,
                    "Phrase A",
                    "Learn the first half slowly.",
                    notes = listOf(60, 62, 64, 62),
                    showNames = false,
                    hint = "C–D–E–D.",
                    successText = "First half learned"
                ),
                Challenge(
                    "3-chunk-b",
                    ChallengeType.NOTE_SEQUENCE,
                    "Phrase B",
                    "Learn the second half slowly.",
                    notes = listOf(60, 60, 62, 60),
                    showNames = false,
                    hint = "Return home to C.",
                    successText = "Second half learned"
                ),
                Challenge(
                    "3-connect",
                    ChallengeType.SIGHT_READ,
                    "Connect the phrase",
                    "Play both halves without a pause.",
                    notes = listOf(60, 62, 64, 62, 60, 60, 62, 60),
                    showNames = false,
                    hint = "Look ahead as the phrase crosses the middle.",
                    successText = "The melody is connected"
                ),
                Challenge(
                    "3-performance",
                    ChallengeType.PERFORMANCE,
                    "Perform your melody",
                    "Listen to the count-in and perform the whole melody in time.",
                    notes = listOf(60, 62, 64, 62, 60, 60, 62, 60),
                    bpm = 72,
                    showNames = false,
                    hint = "Relax, breathe, and keep the pulse moving.",
                    successText = "First melody mastered"
                )
            )
        ),
        Lesson(
            id = 4,
            title = "Unit 1 mastery",
            subtitle = "Prove the skills without hints",
            xp = 100,
            challenges = listOf(
                Challenge(
                    "4-find",
                    ChallengeType.FIND_NOTE,
                    "Find middle C",
                    "Play middle C without labels or hints.",
                    notes = listOf(60),
                    showNames = false,
                    hint = "Use the group of two black keys.",
                    successText = "Keyboard geography mastered"
                ),
                Challenge(
                    "4-sight",
                    ChallengeType.SIGHT_READ,
                    "Sight-reading test",
                    "Read and play this unseen pattern.",
                    notes = listOf(64, 60, 62, 64, 62, 60, 62, 60),
                    showNames = false,
                    hint = "Find the first note before beginning.",
                    successText = "Sight-reading passed"
                ),
                Challenge(
                    "4-performance",
                    ChallengeType.PERFORMANCE,
                    "Final performance",
                    "Play the final phrase in time. The lesson will repeat any weak exercise before awarding mastery.",
                    notes = listOf(60, 62, 64, 64, 62, 60, 62, 64),
                    bpm = 74,
                    showNames = false,
                    hint = "Listen, prepare, then play with confidence.",
                    successText = "Unit 1 mastered"
                )
            )
        )
    )
}

fun midiNoteName(note: Int): String {
    val names = listOf("C", "C♯", "D", "D♯", "E", "F", "F♯", "G", "G♯", "A", "A♯", "B")
    return "${names[note % 12]}${note / 12 - 1}"
}
