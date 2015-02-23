package net.kreatious.pianoleopard.keyboardselect;

import java.util.Objects;

import javax.sound.midi.MidiDevice;

/**
 * Represents the MIDI keyboard in use by the current application
 *
 * @author Jay-R Studer
 */
public class Keyboard {
    private MidiDevice input;
    private MidiDevice output;

    /**
     * @param input
     *            the {@link MidiDevice} to set as the input
     */
    void setInput(MidiDevice input) {
        Objects.requireNonNull(input);
        this.input = input;
    }

    /**
     * @param output
     *            the {@link MidiDevice} to set as the output
     */
    void setOutput(MidiDevice output) {
        Objects.requireNonNull(output);
        this.output = output;
    }

    /**
     * Obtains the current input MIDI device.
     *
     * @return the current input {@link MidiDevice}
     */
    public MidiDevice getInput() {
        return input;
    }

    /**
     * Obtains the current output MIDI device.
     *
     * @return the current output {@link MidiDevice}
     */
    public MidiDevice getOutput() {
        return output;
    }
}
