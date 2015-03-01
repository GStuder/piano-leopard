package net.kreatious.pianoleopard.keyboardselect;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.sound.midi.MidiDevice;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import net.kreatious.pianoleopard.keyboardselect.KeyboardSelector.DeviceRow;

import org.junit.Test;

/**
 * Tests the {@link SelectKeyboardDialog} class.
 *
 * @author Jay-R Studer
 */
public class SelectKeyboardDialogTest {
    private final Devices devices = new Devices();
    private final MidiDevice transmitter = devices.addUnlimitedTransmitter("transmitter");
    private final MidiDevice receiver = devices.addUnlimitedReceiver("receiver");
    private final MidiDevice defaultTransmitter = devices.addTransmitter("defaultTransmitter");
    private final MidiDevice defaultReceiver = devices.addReceiver("defaultReceiver");
    private final SelectKeyboardDialog dialog;

    /**
     * Constructs a new SelectKeyboardDialogTest with the default keyboard
     * devices set.
     */
    public SelectKeyboardDialogTest() {
        dialog = new SelectKeyboardDialog(new Keyboard(defaultTransmitter, defaultReceiver), devices);
    }

    /**
     * Tests the OK button
     */
    @Test
    public void testOK() {
        getComboBoxWithDevice(transmitter).setSelectedItem(new DeviceRow(transmitter));
        getComboBoxWithDevice(receiver).setSelectedItem(new DeviceRow(receiver));
        getButtonWithText("OK").doClick();

        final Keyboard keyboard = dialog.getKeyboard();
        assertThat(keyboard.getInput(), is(transmitter));
        assertThat(keyboard.getOutput(), is(receiver));
        assertThat(dialog.getFrame().isValid(), is(false));
    }

    /**
     * Tests that the dialog reads the current state of the keyboard model
     */
    @Test
    public void testOKNoSelection() {
        getButtonWithText("OK").doClick();

        final Keyboard keyboard = dialog.getKeyboard();
        assertThat(keyboard.getInput(), is(defaultTransmitter));
        assertThat(keyboard.getOutput(), is(defaultReceiver));
        assertThat(dialog.getFrame().isValid(), is(false));
    }

    /**
     * Tests the cancel button
     */
    @Test
    public void testCancel() {
        getComboBoxWithDevice(transmitter).setSelectedItem(new DeviceRow(transmitter));
        getComboBoxWithDevice(receiver).setSelectedItem(new DeviceRow(receiver));
        getButtonWithText("Cancel").doClick();

        final Keyboard keyboard = dialog.getKeyboard();
        assertThat(keyboard.getInput(), is(defaultTransmitter));
        assertThat(keyboard.getOutput(), is(defaultReceiver));
        assertThat(dialog.getFrame().isValid(), is(false));
    }

    private JButton getButtonWithText(String text) {
        return Stream.of(dialog.getFrame().getContentPane().getComponents())
                .filter(component -> component instanceof JButton).map(component -> (JButton) component)
                .filter(button -> button.getText().equals(text)).findFirst().get();
    }

    private JComboBox<?> getComboBoxWithDevice(MidiDevice device) {
        return Stream.of(dialog.getFrame().getContentPane().getComponents())
                .filter(component -> component instanceof JPanel).map(component -> (JPanel) component)
                .flatMap(panel -> Stream.of(panel.getComponents()))
                .filter(component -> component instanceof JComboBox<?>).map(component -> (JComboBox<?>) component)
                .filter(comboBox -> comboBoxContainsDevice(comboBox, device)).findFirst().get();
    }

    private static boolean comboBoxContainsDevice(JComboBox<?> keyboards, MidiDevice device) {
        return IntStream.range(0, keyboards.getItemCount()).mapToObj(keyboards::getItemAt)
                .anyMatch(deviceRow -> ((DeviceRow) deviceRow).getDevice().equals(device));
    }
}
