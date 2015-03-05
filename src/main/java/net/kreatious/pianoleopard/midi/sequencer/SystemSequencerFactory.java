package net.kreatious.pianoleopard.midi.sequencer;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;

/**
 * Provides the MIDI sequencer for the current system.
 *
 * @author Jay-R Studer
 */
public class SystemSequencerFactory implements SequencerFactory {
    @Override
    public Sequencer getSequencer() throws MidiUnavailableException {
        return MidiSystem.getSequencer(false);
    }
}
