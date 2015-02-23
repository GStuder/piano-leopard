package net.kreatious.pianoleopard.keyboardselect;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;

/**
 * Provides a list of mock MIDI devices for testing
 *
 * @author Jay-R Studer
 */
class Devices implements MidiDeviceFactory {
    private final List<MidiDevice> devices = new ArrayList<>();

    @Override
    public MidiDevice[] getMidiDevices() {
        return devices.toArray(new MidiDevice[devices.size()]);
    }

    private MidiDevice newAddedDevice(String name) {
        // MidiDevice.Info getters are final & the constructor is protected
        class MockInfo extends Info {
            protected MockInfo() {
                super(name, "Vendor", "Description", "1.0");
            }
        }

        final MidiDevice device = mock(MidiDevice.class);
        given(device.getDeviceInfo()).willReturn(new MockInfo());
        given(device.toString()).willReturn(name);
        devices.add(device);
        return device;
    }

    void clear() {
        devices.clear();
    }

    MidiDevice addTransmitter(String name) {
        return given(newAddedDevice(name).getMaxTransmitters()).willReturn(1).getMock();
    }

    MidiDevice addReceiver(String name) {
        return given(newAddedDevice(name).getMaxReceivers()).willReturn(1).getMock();
    }

    MidiDevice addUnlimitedTransmitter(String name) {
        return given(newAddedDevice(name).getMaxTransmitters()).willReturn(-1).getMock();
    }

    MidiDevice addUnlimitedReceiver(String name) {
        return given(newAddedDevice(name).getMaxReceivers()).willReturn(-1).getMock();
    }
}
