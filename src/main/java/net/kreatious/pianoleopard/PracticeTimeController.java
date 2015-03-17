package net.kreatious.pianoleopard;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import net.kreatious.pianoleopard.history.History;
import net.kreatious.pianoleopard.history.HistoryVisitor;
import net.kreatious.pianoleopard.midi.OutputModel;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;

class PracticeTimeController {
    private static Map<History, HistoryVisitor> activeVisitors = new ConcurrentHashMap<>();

    static Component create(History history, OutputModel outputModel) {
        final JLabel label = new JLabel("Practiced: 0 times");
        label.setVisible(false);
        outputModel.addOpenListener(sequence -> sequence.getFile().ifPresent(midi -> onOpen(label, history, midi)));
        return label;
    }

    private static void onOpen(JLabel label, History history, File midi) {
        label.setVisible(true);

        final HistoryVisitor visitor = activeVisitors.compute(history, (key, oldVisitor) -> {
            key.stopReading(oldVisitor);
            return new PracticeTimeVisitor(midi) {
                @Override
                void afterParsing(List<Instant> times) {
                    SwingUtilities.invokeLater(() -> {
                        label.setText("Practiced: " + times.size() + " times");
                    });
                }
            };
        });

        ForkJoinPool.commonPool().submit(() -> history.startReading(visitor));
    }

    /**
     * Calculates the times a given MIDI file has been practiced.
     * <p>
     * A MIDI file is considered practiced if the user played at least one note.
     */
    private static abstract class PracticeTimeVisitor extends HistoryVisitor {
        private final byte[] fileHash;

        private Optional<Instant> openTime = Optional.empty();
        private boolean fileOpen;
        private boolean dirty;

        private final List<Instant> practiceTimes = new ArrayList<>();

        private PracticeTimeVisitor(File midi) {
            try {
                fileHash = Hashing.sha256().hashBytes(Files.toByteArray(midi)).asBytes();
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public void onFile(byte[] hash, long time) {
            dirty = true;
            fileOpen = Arrays.equals(hash, fileHash);
        }

        @Override
        public void onStart(long time) {
            if (fileOpen) {
                openTime = Optional.of(Instant.ofEpochMilli(time));
            }
        }

        @Override
        public void onKey(byte status, byte data1, byte data2, long currentTime, long time) {
            if (openTime.isPresent()) {
                practiceTimes.add(openTime.get());
                openTime = Optional.empty();
            }
        }

        @Override
        public final void onParsingComplete() {
            if (dirty) {
                dirty = false;
                afterParsing(practiceTimes);
            }
        }

        /**
         * @param times
         *            the unsorted times the file has been practiced.
         */
        abstract void afterParsing(List<Instant> times);
    }
}
