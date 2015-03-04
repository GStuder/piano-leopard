package net.kreatious.pianoleopard.keyboardselect;

import java.util.stream.Stream;

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

    /**
     * @return a new stream of MIDI devices with receiver (output) capability.
     */
    default Stream<MidiDevice> getReceivers() {
        return Stream.of(getMidiDevices()).filter(device -> device.getMaxReceivers() != 0);
    }

    /**
     * @return a new stream of MIDI devices with transmitter (input) capability.
     */
    default Stream<MidiDevice> getTransmitters() {
        return Stream.of(getMidiDevices()).filter(device -> device.getMaxTransmitters() != 0);
    }
}
