package net.kreatious.pianoleopard.keyboardselect;

import javax.sound.midi.MidiDevice;

/**
 * Represents the MIDI keyboard in use by the current application
 *
 * @author Jay-R Studer
 */
public class Keyboard {
    private final MidiDevice input;
    private final MidiDevice output;

    /**
     * Constructs a new {@link Keyboard} with the specified input and output
     * devices
     *
     * @param input
     *            the MIDI input device
     * @param output
     *            the MIDI output device
     */
    public Keyboard(MidiDevice input, MidiDevice output) {
        this.input = input;
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

    @Override
    public String toString() {
        return "Keyboard[input = " + input.getDeviceInfo() + ", output = " + output.getDeviceInfo() + "]";
    }
}
