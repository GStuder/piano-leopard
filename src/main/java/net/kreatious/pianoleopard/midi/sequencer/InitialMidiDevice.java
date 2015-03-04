package net.kreatious.pianoleopard.midi.sequencer;

import java.util.Collections;
import java.util.List;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

/**
 * Provides a do-nothing implementation of a MIDI device that is unconnected to
 * any actual devices.
 *
 * @author Jay-R Studer
 */
class InitialMidiDevice implements MidiDevice {
    @Override
    public Info getDeviceInfo() {
        return new Info("Piano Leopard Default Device", "Kreatious LLC",
                "The initial MIDI device used by Piano Leopard, does nothing", "1.0") {
        };
    }

    @Override
    public void open() {
    }

    @Override
    public void close() {
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public long getMicrosecondPosition() {
        return 0;
    }

    @Override
    public int getMaxReceivers() {
        return -1;
    }

    @Override
    public int getMaxTransmitters() {
        return -1;
    }

    @Override
    public Receiver getReceiver() {
        return new Receiver() {
            @Override
            public void send(MidiMessage message, long timeStamp) {
            }

            @Override
            public void close() {
            }
        };
    }

    @Override
    public List<Receiver> getReceivers() {
        return Collections.emptyList();
    }

    @Override
    public Transmitter getTransmitter() {
        return new Transmitter() {
            @Override
            public void setReceiver(Receiver receiver) {
            }

            @Override
            public Receiver getReceiver() {
                return null;
            }

            @Override
            public void close() {
            }
        };
    }

    @Override
    public List<Transmitter> getTransmitters() {
        return Collections.emptyList();
    }
}
