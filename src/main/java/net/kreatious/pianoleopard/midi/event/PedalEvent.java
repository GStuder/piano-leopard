package net.kreatious.pianoleopard.midi.event;

import javax.sound.midi.ShortMessage;

/**
 * Represents an event that is associated with a pedal.
 *
 * @author Jay-R Studer
 */
public class PedalEvent extends Event {
    private final Pedal pedal;
    private final boolean on;
    private final Slot slot;

    PedalEvent(ShortMessage message, long time) {
        super(message, time);

        pedal = Pedal.lookup(message.getData1()).orElseThrow(
                () -> new IllegalArgumentException(message.getData1() + " is not a pedal message"));

        on = message.getData2() >= 64;
        slot = new Slot(message.getChannel(), pedal);
    }

    static boolean canCreate(ShortMessage message) {
        return message.getCommand() == ShortMessage.CONTROL_CHANGE && message.getData1() >= 64
                && message.getData1() <= 67;
    }

    /**
     * @return the pedal associated with this event
     */
    public Pedal getPedal() {
        return pedal;
    }

    @Override
    public boolean isOn() {
        return on;
    }

    @Override
    public Slot getSlot() {
        return slot;
    }
}
