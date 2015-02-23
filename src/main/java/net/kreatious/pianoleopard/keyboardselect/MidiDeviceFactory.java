package net.kreatious.pianoleopard.keyboardselect;

import javax.sound.midi.MidiDevice;

/**
 * Provides the MIDI devices available on the system.
 *
 * @author Jay-R Studer
 */
@FunctionalInterface
interface MidiDeviceFactory {
    /**
     * Obtains an array of MIDI device objects that represent all MIDI devices
     * available on the system.
     *
     * @return an array of {@link MidiDevice} objects, one for each installed
     *         MIDI device
     */
    MidiDevice[] getMidiDevices();
}
