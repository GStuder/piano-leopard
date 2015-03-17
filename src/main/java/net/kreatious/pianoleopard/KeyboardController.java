package net.kreatious.pianoleopard;

import java.awt.Component;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.prefs.Preferences;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import net.kreatious.pianoleopard.keyboardselect.Keyboard;
import net.kreatious.pianoleopard.keyboardselect.SelectKeyboardDialog;
import net.kreatious.pianoleopard.keyboardselect.SystemMidiDeviceFactory;
import net.kreatious.pianoleopard.midi.InputModel;
import net.kreatious.pianoleopard.midi.OutputModel;

/**
 * Provides the controller for the keyboard action
 *
 * @author Jay-R Studer
 */
class KeyboardController {
    private static final String INPUT_PREFERENCE = "inputDevice";
    private static final String OUTPUT_PREFERENCE = "outputDevice";

    private KeyboardController() {
    }

    /**
     * Finds the keyboard opened by the user in a previous session.
     *
     * @param preferences
     *            the {@link Preferences} storing persisted data
     * @return the keyboard last opened by the user, if any.
     */
    private static Optional<Keyboard> getLastKeyboard(Preferences preferences) {
        final SystemMidiDeviceFactory deviceFactory = new SystemMidiDeviceFactory();
        final Optional<MidiDevice> outputDevice = deviceFactory.getReceivers()
                .filter(device -> device.getDeviceInfo().getName().equals(preferences.get(OUTPUT_PREFERENCE, "")))
                .findFirst();
        final Optional<MidiDevice> inputDevice = deviceFactory.getTransmitters()
                .filter(device -> device.getDeviceInfo().getName().equals(preferences.get(INPUT_PREFERENCE, "")))
                .findFirst();
        if (inputDevice.isPresent() && outputDevice.isPresent()) {
            return Optional.of(new Keyboard(inputDevice.get(), outputDevice.get()));
        }
        return Optional.empty();
    }

    /**
     * Constructs a view and associates it with a controller
     */
    static Component create(Component parent, Preferences preferences, OutputModel outputModel, InputModel inputModel) {
        outputModel.addOutputDeviceListener(info -> preferences.put(OUTPUT_PREFERENCE, info.getName()));
        inputModel.addInputDeviceListener(info -> preferences.put(INPUT_PREFERENCE, info.getName()));

        final Consumer<Keyboard> switchKeyboard = keyboard -> {
            try {
                inputModel.setInputDevice(keyboard.getInput());
                outputModel.setOutputDevice(keyboard.getOutput());
            } catch (final MidiUnavailableException ex) {
                ex.printStackTrace();
                JOptionPane
                        .showMessageDialog(
                                parent,
                                "Unable to select the MIDI keyboard. Check that the keyboard is not in use by another application.",
                                "Error switching MIDI keyboard", JOptionPane.ERROR_MESSAGE);
            }
        };

        final Optional<Keyboard> lastKeyboard = getLastKeyboard(preferences);
        lastKeyboard.ifPresent(switchKeyboard);

        final SelectKeyboardDialog selectKeyboard = new SelectKeyboardDialog(lastKeyboard,
                new SystemMidiDeviceFactory(), preferences);
        final JButton button = new JButton("Keyboard...");
        button.addActionListener(event -> selectKeyboard.showDialog(Optional.of(parent)).ifPresent(switchKeyboard));
        return button;
    }
}
