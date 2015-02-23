package net.kreatious.pianoleopard.keyboardselect;

import java.util.Optional;
import java.util.stream.Stream;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

/**
 * Provides the MIDI devices connected to the current system.
 *
 * @author Jay-R Studer
 */
public class SystemMidiDeviceFactory implements MidiDeviceFactory {
    @Override
    public MidiDevice[] getMidiDevices() {
        return Stream.of(MidiSystem.getMidiDeviceInfo()).map(SystemMidiDeviceFactory::getMidiDevice)
                .filter(Optional::isPresent).map(Optional::get).toArray(MidiDevice[]::new);
    }

    private static Optional<MidiDevice> getMidiDevice(Info info) {
        try {
            return Optional.of(MidiSystem.getMidiDevice(info));
        } catch (final MidiUnavailableException e) {
            return Optional.empty();
        }
    }
}
