package net.kreatious.pianoleopard.keyboardselect;

import java.awt.Container;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.sound.midi.MidiDevice;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import com.google.common.annotations.VisibleForTesting;

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
    private final String label;
    private final Predicate<? super MidiDevice> filter;
    private final MidiDeviceFactory deviceFactory;

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
        this.label = label;
        this.filter = filter;
        this.deviceFactory = deviceFactory;

        reloadDevices();
    }

    /**
     * Adds this selector to the specified container.
     * <p>
     * The container must have a JGoodies FormLayout with 1 row and 3 columns.
     *
     * @param container
     *            the container to add to
     * @param x
     *            the column to add to
     * @param y
     *            the row to add to
     * @param width
     *            the number of columns to take up
     */
    void addToContainer(Container container, int x, int y, int width) {
        container.add(new JLabel(label), x + ", " + y + ", right, default");
        container.add(keyboards, x + 2 + ", " + y + ", " + (width - 2) + ", 1, fill, default");
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
     * Reloads the MIDI devices displayed by this control
     */
    void reloadDevices() {
        final Optional<MidiDevice> selectedDevice = getSelectedDevice();
        keyboards.removeAllItems();
        Stream.of(deviceFactory.getMidiDevices()).filter(filter).map(DeviceRow::new).forEach(keyboards::addItem);
        selectedDevice.ifPresent(this::setSelectedDevice);
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
