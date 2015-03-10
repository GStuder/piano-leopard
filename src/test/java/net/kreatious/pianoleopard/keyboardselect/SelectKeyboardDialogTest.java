package net.kreatious.pianoleopard.keyboardselect;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.prefs.Preferences;
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
    private final Preferences preferences = mock(Preferences.class);
    private final Devices devices = new Devices();
    private final MidiDevice transmitter = devices.addUnlimitedTransmitter("transmitter");
    private final MidiDevice receiver = devices.addUnlimitedReceiver("receiver");
    private final MidiDevice defaultTransmitter = devices.addTransmitter("defaultTransmitter");
    private final MidiDevice defaultReceiver = devices.addReceiver("defaultReceiver");
    private final Optional<Keyboard> firstSelectedKeyboard = Optional.of(new Keyboard(defaultTransmitter,
            defaultReceiver));

    /**
     * Tests the OK button for the first time the dialog is shown, without an
     * existing keyboard
     */
    @Test
    public void testOK() {
        final SelectKeyboardDialog dialog = new SelectKeyboardDialog(Optional.empty(), devices, preferences);
        getComboBoxWithDevice(dialog, transmitter).setSelectedItem(new DeviceRow(transmitter));
        getComboBoxWithDevice(dialog, receiver).setSelectedItem(new DeviceRow(receiver));
        getButtonWithText(dialog, "OK").doClick();

        final Keyboard keyboard = dialog.getKeyboard().get();
        assertThat(keyboard.getInput(), is(transmitter));
        assertThat(keyboard.getOutput(), is(receiver));
        assertThat(dialog.getDialog().isValid(), is(false));
    }

    /**
     * Tests that the dialog reads the current state of the keyboard model, with
     * an existing keyboard
     */
    @Test
    public void testOKNoSelection() {
        final SelectKeyboardDialog dialog = new SelectKeyboardDialog(firstSelectedKeyboard, devices, preferences);
        getButtonWithText(dialog, "OK").doClick();

        final Keyboard keyboard = dialog.getKeyboard().get();
        assertThat(keyboard.getInput(), is(defaultTransmitter));
        assertThat(keyboard.getOutput(), is(defaultReceiver));
        assertThat(dialog.getDialog().isValid(), is(false));
    }

    /**
     * Tests the cancel button the first time the dialog is shown, without an
     * existing keyboard
     */
    @Test
    public void testCancelFirst() {
        final SelectKeyboardDialog dialog = new SelectKeyboardDialog(Optional.empty(), devices, preferences);
        getComboBoxWithDevice(dialog, transmitter).setSelectedItem(new DeviceRow(transmitter));
        getComboBoxWithDevice(dialog, receiver).setSelectedItem(new DeviceRow(receiver));
        getButtonWithText(dialog, "Cancel").doClick();

        assertThat(dialog.getKeyboard(), is(Optional.empty()));
        assertThat(dialog.getDialog().isValid(), is(false));
    }

    /**
     * Tests the cancel button if the dialog is shown with an existing keyboard
     */
    @Test
    public void testCancel() {
        final SelectKeyboardDialog dialog = new SelectKeyboardDialog(firstSelectedKeyboard, devices, preferences);
        getComboBoxWithDevice(dialog, transmitter).setSelectedItem(new DeviceRow(transmitter));
        getComboBoxWithDevice(dialog, receiver).setSelectedItem(new DeviceRow(receiver));
        getButtonWithText(dialog, "Cancel").doClick();

        final Keyboard keyboard = dialog.getKeyboard().get();
        assertThat(keyboard.getInput(), is(defaultTransmitter));
        assertThat(keyboard.getOutput(), is(defaultReceiver));
        assertThat(dialog.getDialog().isValid(), is(false));
    }

    /**
     * Tests that the keyboard dialog refreshes its device lists when shown
     */
    @Test
    public void testShowDialogRefreshes() {
        testShowDialogRefreshes(defaultReceiver, Devices::addReceiver);
        testShowDialogRefreshes(defaultTransmitter, Devices::addTransmitter);
    }

    private void testShowDialogRefreshes(MidiDevice device, BiFunction<Devices, String, MidiDevice> addDevice) {
        final SelectKeyboardDialog dialog = new SelectKeyboardDialog(firstSelectedKeyboard, devices, preferences);

        final MidiDevice addedDevice = addDevice.apply(devices, "new device");
        final JComboBox<?> combobox = getComboBoxWithDevice(dialog, device);
        assertThat(getDevices(combobox), not(hasItem(addedDevice)));
        dialog.showDialog(Optional.empty());
        assertThat(getDevices(combobox), hasItem(addedDevice));

        getButtonWithText(dialog, "Cancel").doClick();
        assertThat(dialog.getDialog().isValid(), is(false));
    }

    /**
     * Tests that the keyboard dialog refreshes its device lists when the
     * refresh button is selected
     */
    @Test
    public void testRefresh() {
        testRefresh(defaultReceiver, Devices::addReceiver);
        testRefresh(defaultTransmitter, Devices::addTransmitter);
    }

    private void testRefresh(MidiDevice device, BiFunction<Devices, String, MidiDevice> addDevice) {
        final SelectKeyboardDialog dialog = new SelectKeyboardDialog(firstSelectedKeyboard, devices, preferences);

        final MidiDevice addedDevice = addDevice.apply(devices, "new device");
        final JComboBox<?> combobox = getComboBoxWithDevice(dialog, device);
        assertThat(getDevices(combobox), not(hasItem(addedDevice)));
        getButtonWithText(dialog, "Refresh").doClick();
        assertThat(getDevices(combobox), hasItem(addedDevice));

        getButtonWithText(dialog, "Cancel").doClick();
        assertThat(dialog.getDialog().isValid(), is(false));
    }

    private static JButton getButtonWithText(SelectKeyboardDialog dialog, String text) {
        return Stream.of(dialog.getDialog().getContentPane().getComponents())
                .filter(component -> component instanceof JButton).map(component -> (JButton) component)
                .filter(button -> button.getText().equals(text)).findFirst().get();
    }

    private static JComboBox<?> getComboBoxWithDevice(SelectKeyboardDialog dialog, MidiDevice device) {
        return Stream.of(dialog.getDialog().getContentPane().getComponents())
                .filter(component -> component instanceof JPanel).map(component -> (JPanel) component)
                .flatMap(panel -> Stream.of(panel.getComponents()))
                .filter(component -> component instanceof JComboBox<?>).map(component -> (JComboBox<?>) component)
                .filter(comboBox -> getDevices(comboBox).contains(device)).findFirst().get();
    }

    private static List<MidiDevice> getDevices(JComboBox<?> keyboards) {
        return IntStream.range(0, keyboards.getItemCount()).mapToObj(keyboards::getItemAt)
                .map(deviceRow -> ((DeviceRow) deviceRow).getDevice()).collect(toList());
    }
}
