package net.kreatious.pianoleopard.midi.sequencer;

import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;

/**
 * Provides the MIDI sequencer available on the system.
 *
 * @author Jay-R Studer
 */
@FunctionalInterface
interface SequencerFactory {
    /**
     * Obtains a new MIDI sequencer that is not connected to a default device.
     *
     * @return a {@link Sequencer} for playing back MIDI files
     * @throws MidiUnavailableException
     *             if no sequencer is available
     */
    Sequencer getSequencer() throws MidiUnavailableException;
}
