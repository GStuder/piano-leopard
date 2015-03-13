package net.kreatious.pianoleopard.history;

import java.util.Arrays;

enum Message {
    /**
     * Unknown event
     */
    UNKNOWN(0),

    /**
     * User generated a MIDI event by pressing a key
     */
    KEY(1),

    /**
     * User changed which file is played
     */
    FILE(2),

    /**
     * User begins a practice session
     */
    START(3),

    /**
     * User started the program
     */
    HEADER(4),

    /**
     * Resynchronizes the value of the offset variable
     */
    OFFSET_CHANGED(5);

    private static final Message[] VALUES = new Message[256];
    static {
        Arrays.fill(VALUES, UNKNOWN);
        for (final Message message : values()) {
            VALUES[message.headerByte] = message;
        }
    }

    private final byte headerByte;

    private Message(int headerByte) {
        this.headerByte = (byte) headerByte;
    }

    byte getHeaderByte() {
        return headerByte;
    }

    static Message getByValue(byte value) {
        return VALUES[value & 0xFF];
    }
}
