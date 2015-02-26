package net.kreatious.pianoleopard.keyboardselect;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.sound.midi.MidiDevice;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.google.common.annotations.VisibleForTesting;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * Provides a GUI element for selecting a MIDI Keyboard from a list of connected
 * keyboards.
 *
 * @author Jay-R Studer
 */
class KeyboardSelector {
    /**
     * Represents a row to be displayed within the combobox control
     */
    @VisibleForTesting
    static class DeviceRow {
        private final MidiDevice device;

        DeviceRow(MidiDevice device) {
            this.device = device;
        }

        MidiDevice getDevice() {
            return device;
        }

        @Override
        public int hashCode() {
            return device.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (!(obj instanceof DeviceRow)) {
                return false;
            }
            return Objects.equals(((DeviceRow) obj).device, device);
        }

        @Override
        public String toString() {
            return device.getDeviceInfo().getName();
        }
    }

    private final JComboBox<DeviceRow> keyboards = new JComboBox<>();
    private final JPanel panel = new JPanel();

    /**
     * Constructs a new {@link KeyboardSelector} with the specified label and
     * filter.
     * <p>
     * The provided filter is used to determine if a keyboard should be
     * included.
     *
     * @param label
     *            the label text to display
     * @param filter
     *            the predicate determining if a keyboard should be included
     * @param deviceFactory
     *            the {@link MidiDeviceFactory} for obtaining available MIDI
     *            devices
     */
    KeyboardSelector(String label, Predicate<? super MidiDevice> filter, MidiDeviceFactory deviceFactory) {
        panel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.BUTTON_COLSPEC,
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("default:grow"), },
                new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, }));
        panel.add(new JLabel(label), "1, 1, right, default");
        panel.add(keyboards, "3, 1, fill, default");

        Stream.of(deviceFactory.getMidiDevices()).filter(filter).map(DeviceRow::new).forEach(keyboards::addItem);
    }

    /**
     * @return the {@link JPanel} associated with this control
     */
    JPanel getPanel() {
        return panel;
    }

    /**
     * @return the current {@link MidiDevice} selected by this control.
     */
    Optional<MidiDevice> getSelectedDevice() {
        return Optional.ofNullable((DeviceRow) keyboards.getSelectedItem()).map(DeviceRow::getDevice);
    }

    /**
     * @param device
     *            the {@link MidiDevice} to set on this control
     */
    void setSelectedDevice(MidiDevice device) {
        keyboards.setSelectedItem(new DeviceRow(device));
    }

    /**
     * @return a new array containing the devices displayed by this control.
     */
    @VisibleForTesting
    MidiDevice[] getDisplayedDevices() {
        return IntStream.range(0, keyboards.getItemCount()).mapToObj(keyboards::getItemAt).map(DeviceRow::getDevice)
                .toArray(MidiDevice[]::new);
    }
}
