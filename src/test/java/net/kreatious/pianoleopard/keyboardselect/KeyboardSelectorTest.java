package net.kreatious.pianoleopard.keyboardselect;

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import javax.sound.midi.MidiDevice;

import org.junit.Test;

/**
 * Tests the {@link KeyboardSelector} class.
 *
 * @author Jay-R Studer
 */
public class KeyboardSelectorTest {

    private final Devices devices = new Devices();
    private final MidiDevice receiver = devices.addReceiver("receiver");
    private final MidiDevice unlimitedReceiver = devices.addUnlimitedReceiver("unlimitedReceiver");
    private final MidiDevice transmitter = devices.addTransmitter("transmitter");
    private final MidiDevice unlimitedTransmitter = devices.addUnlimitedTransmitter("unlimitedTransmitter");

    /**
     * Test for displaying a list of input devices
     */
    @Test
    public void testTransmitter() {
        final KeyboardSelector selector = new KeyboardSelector("Input:", x -> x.getMaxTransmitters() != 0, devices);
        assertThat(selector.getDisplayedDevices(), is(arrayContaining(transmitter, unlimitedTransmitter)));
    }

    /**
     * Test for displaying a list of output devices
     */
    @Test
    public void testReceiver() {
        final KeyboardSelector selector = new KeyboardSelector("Output:", x -> x.getMaxReceivers() != 0, devices);
        assertThat(selector.getDisplayedDevices(), is(arrayContaining(receiver, unlimitedReceiver)));
    }

    /**
     * Test for no devices being available
     */
    @Test
    public void testNoDevices() {
        devices.clear();

        final KeyboardSelector selector = new KeyboardSelector("Output:", x -> true, devices);
        assertThat(selector.getDisplayedDevices(), is(emptyArray()));
        assertThat(selector.getSelectedDevice().isPresent(), is(false));
    }

    /**
     * Test the default selected device
     */
    @Test
    public void testGetSelectedDevice() {
        final KeyboardSelector selector = new KeyboardSelector("Output:", x -> x.getMaxTransmitters() != 0, devices);
        assertThat(selector.getSelectedDevice().get(), is(transmitter));
    }

    /**
     * Tests that calling set selected device changes the selection
     */
    @Test
    public void testSetSelectedDevice() {
        final KeyboardSelector selector = new KeyboardSelector("All:", x -> true, devices);
        selector.setSelectedDevice(unlimitedTransmitter);
        assertThat(selector.getSelectedDevice().get(), is(unlimitedTransmitter));
    }

    /**
     * Tests that setting a null selected device does not change the selection
     */
    @Test
    public void testSetNullSelectedDevice() {
        final KeyboardSelector selector = new KeyboardSelector("All:", x -> true, devices);
        selector.setSelectedDevice(null);
        assertThat(selector.getSelectedDevice().get(), is(receiver));
    }

    /**
     * Tests that setting an unlisted device does not change the selection
     */
    @Test
    public void testSetUnselectedDevice() {
        final KeyboardSelector selector = new KeyboardSelector("Input:", x -> x.getMaxReceivers() != 0, devices);
        selector.setSelectedDevice(unlimitedTransmitter);
        assertThat(selector.getSelectedDevice().get(), is(receiver));
    }
}
