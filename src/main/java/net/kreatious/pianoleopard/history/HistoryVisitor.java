package net.kreatious.pianoleopard.history;

import javax.sound.midi.InvalidMidiDataException;

/**
 * Visits the events performed historically by the user.
 * <p>
 * Methods are called from various threads, but are guaranteed to not be called
 * concurrently.
 *
 * @author Jay-R Studer
 */
public abstract class HistoryVisitor {
    /**
     * Called when the user begins a practice session. The file being practiced
     * will be given by a previous call to {@link #onFile(byte[], long)}.
     *
     * @param time
     *            the epoch time in milliseconds the sequence was started at
     */
    public void onStart(long time) {
    }

    /**
     * Called when the user has opened a file.
     *
     * @param hash
     *            the SHA-256 hash code of the file that was opened
     * @param time
     *            the epoch time in milliseconds the event was played at
     */
    public void onFile(byte[] hash, long time) {
    }

    /**
     * Called when the user has pressed a key in the past.
     *
     * @param status
     *            the status byte of the message the user played
     * @param data1
     *            the first data byte of the message
     * @param data2
     *            the second data byte of the message
     * @param currentTime
     *            the current song time in microseconds the message was played
     *            at
     * @param time
     *            the epoch time in milliseconds the event was played at
     * @throws InvalidMidiDataException
     *             if the message was invalid
     */
    public void onKey(byte status, byte data1, byte data2, long currentTime, long time) throws InvalidMidiDataException {
    }

    /**
     * Called after parsing is complete.
     * <p>
     * This method may be called multiple times, and is guaranteed to be called
     * at least once if the log file is accessible. If notifications are no
     * longer desired, then the visitor needs to be unregistered.
     * <p>
     * If the {@link History} class was unable to initialize logging, this
     * method is never called.
     */
    public abstract void onParsingComplete();
}
