package com.pianopath.app

import android.app.Activity
import android.media.AudioManager
import android.media.ToneGenerator
import android.media.midi.MidiDevice
import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiManager
import android.media.midi.MidiOutputPort
import android.media.midi.MidiReceiver
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.Closeable
import java.io.IOException
import java.util.concurrent.CopyOnWriteArraySet

data class MidiNoteEvent(
    val note: Int,
    val velocity: Int,
    val timestampMs: Long
)

class MidiController(private val activity: Activity) : Closeable {
    private val manager = activity.getSystemService(MidiManager::class.java)
    private val handler = Handler(Looper.getMainLooper())
    private val devices = mutableListOf<MidiDevice>()
    private val ports = mutableListOf<MidiOutputPort>()
    private val listeners = CopyOnWriteArraySet<(MidiNoteEvent) -> Unit>()
    private var callbackRegistered = false

    var isConnected by mutableStateOf(false)
        private set
    var deviceName by mutableStateOf("No MIDI piano connected")
        private set
    var lastNote by mutableIntStateOf(-1)
        private set
    var lastVelocity by mutableIntStateOf(0)
        private set
    var eventCounter by mutableLongStateOf(0L)
        private set

    private val deviceCallback = object : MidiManager.DeviceCallback() {
        override fun onDeviceAdded(device: MidiDeviceInfo) = refresh()
        override fun onDeviceRemoved(device: MidiDeviceInfo) = refresh()
        override fun onDeviceStatusChanged(status: android.media.midi.MidiDeviceStatus) {
            refreshConnectionState()
        }
    }

    fun start() {
        if (!callbackRegistered) {
            manager.registerDeviceCallback(deviceCallback, handler)
            callbackRegistered = true
        }
        refresh()
    }

    fun addNoteListener(listener: (MidiNoteEvent) -> Unit) {
        listeners.add(listener)
    }

    fun removeNoteListener(listener: (MidiNoteEvent) -> Unit) {
        listeners.remove(listener)
    }

    fun refresh() {
        closeOpenDevices()
        val infos = manager.devices.filter { it.outputPortCount > 0 }
        if (infos.isEmpty()) {
            updateDisconnected()
            return
        }

        var pending = infos.size
        infos.forEach { info ->
            manager.openDevice(info, { device ->
                pending--
                if (device != null) {
                    devices.add(device)
                    for (portIndex in 0 until info.outputPortCount) {
                        val port = device.openOutputPort(portIndex) ?: continue
                        port.connect(object : MidiReceiver() {
                            override fun onSend(data: ByteArray, offset: Int, count: Int, timestamp: Long) {
                                parseMessages(data, offset, count)
                            }
                        })
                        ports.add(port)
                    }
                }
                if (pending == 0) refreshConnectionState(info)
            }, handler)
        }
    }

    private fun parseMessages(data: ByteArray, offset: Int, count: Int) {
        var index = offset
        val end = offset + count
        while (index < end) {
            val status = data[index].toInt() and 0xFF
            val command = status and 0xF0
            if ((command == 0x90 || command == 0x80) && index + 2 < end) {
                val note = data[index + 1].toInt() and 0x7F
                val velocity = data[index + 2].toInt() and 0x7F
                if (command == 0x90 && velocity > 0) {
                    val event = MidiNoteEvent(note, velocity, android.os.SystemClock.elapsedRealtime())
                    activity.runOnUiThread {
                        lastNote = note
                        lastVelocity = velocity
                        eventCounter++
                        listeners.forEach { it(event) }
                    }
                }
                index += 3
            } else {
                index++
            }
        }
    }

    private fun refreshConnectionState(fallbackInfo: MidiDeviceInfo? = null) {
        isConnected = ports.isNotEmpty()
        if (isConnected) {
            val info = fallbackInfo ?: manager.devices.firstOrNull { it.outputPortCount > 0 }
            val name = info?.properties?.getString(MidiDeviceInfo.PROPERTY_NAME)
                ?: info?.properties?.getString(MidiDeviceInfo.PROPERTY_PRODUCT)
                ?: "MIDI piano"
            deviceName = name
        } else {
            updateDisconnected()
        }
    }

    private fun updateDisconnected() {
        isConnected = false
        deviceName = "No MIDI piano connected"
        lastNote = -1
        lastVelocity = 0
    }

    private fun closeOpenDevices() {
        ports.forEach {
            try { it.close() } catch (_: IOException) { }
        }
        ports.clear()
        devices.forEach {
            try { it.close() } catch (_: IOException) { }
        }
        devices.clear()
    }

    override fun close() {
        listeners.clear()
        closeOpenDevices()
        if (callbackRegistered) {
            manager.unregisterDeviceCallback(deviceCallback)
            callbackRegistered = false
        }
    }
}

class Metronome : Closeable {
    private val tone = ToneGenerator(AudioManager.STREAM_MUSIC, 75)

    fun click(accent: Boolean = false) {
        tone.startTone(
            if (accent) ToneGenerator.TONE_PROP_ACK else ToneGenerator.TONE_PROP_BEEP,
            if (accent) 85 else 55
        )
    }

    override fun close() {
        tone.release()
    }
}
