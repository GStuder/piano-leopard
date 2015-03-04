package net.kreatious.pianoleopard;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Optional;
import java.util.prefs.Preferences;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import net.kreatious.pianoleopard.keyboardselect.Keyboard;
import net.kreatious.pianoleopard.keyboardselect.SelectKeyboardDialog;
import net.kreatious.pianoleopard.keyboardselect.SystemMidiDeviceFactory;
import net.kreatious.pianoleopard.midi.sequencer.InputModel;
import net.kreatious.pianoleopard.midi.sequencer.OutputModel;

/**
 * Provides the action for the keyboard button
 *
 * @author Jay-R Studer
 */
class KeyboardAction extends AbstractAction {
    private static final long serialVersionUID = 6330824397500171700L;
    private static final String INPUT_PREFERENCE = "inputDevice";
    private static final String OUTPUT_PREFERENCE = "outputDevice";

    private final Component parent;
    private final OutputModel outputModel;
    private final InputModel inputModel;
    private final SelectKeyboardDialog selectKeyboard;

    private KeyboardAction(Component parent, Preferences preferences, OutputModel outputModel, InputModel inputModel) {
        this.parent = parent;
        this.outputModel = outputModel;
        this.inputModel = inputModel;
        selectKeyboard = new SelectKeyboardDialog(getLastKeyboard(preferences), new SystemMidiDeviceFactory());
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

    static JButton create(Component parent, Preferences preferences, OutputModel outputModel, InputModel inputModel) {
        outputModel.addOutputDeviceListener(info -> preferences.put(OUTPUT_PREFERENCE, info.getName()));
        inputModel.addInputDeviceListener(info -> preferences.put(INPUT_PREFERENCE, info.getName()));

        final KeyboardAction action = new KeyboardAction(parent, preferences, outputModel, inputModel);
        getLastKeyboard(preferences).ifPresent(action::setKeyboard);
        action.putValue(NAME, "Keyboard...");
        return new JButton(action);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        selectKeyboard.showDialog(Optional.of(parent)).ifPresent(this::setKeyboard);
    }

    private void setKeyboard(Keyboard keyboard) {
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
    }
}
