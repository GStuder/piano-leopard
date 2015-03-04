package net.kreatious.pianoleopard;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import net.kreatious.pianoleopard.midi.sequencer.OutputModel;

/**
 * Provides the controller for the open action
 *
 * @author Jay-R Studer
 */
class OpenController {
    private static final String DIRECTORY_PREFERENCE = "defaultOpenDirectory";

    private OpenController() {
    }

    /**
     * Constructs a view and associates it with its controller
     */
    static Component create(Component parent, Preferences preferences, OutputModel outputModel) {
        final JFileChooser chooser = new JFileChooser();
        chooser.setPreferredSize(new Dimension(600, 400));
        Optional.ofNullable(preferences.get(DIRECTORY_PREFERENCE, null)).map(File::new)
                .ifPresent(chooser::setCurrentDirectory);

        final JButton button = new JButton("Open...");
        button.setEnabled(false);
        outputModel.addOutputDeviceListener(info -> button.setEnabled(true));
        button.addActionListener(e -> {
            try {
                if (chooser.showOpenDialog(parent) == JFileChooser.CANCEL_OPTION) {
                    return;
                }

                final File selectedFile = chooser.getSelectedFile();
                outputModel.openMidiFile(selectedFile);
                Optional.ofNullable(selectedFile.getParentFile()).ifPresent(
                        directory -> preferences.put(DIRECTORY_PREFERENCE, directory.getAbsolutePath()));
            } catch (final IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(parent, "Unable to open " + chooser.getSelectedFile().getName()
                        + ", try a different file.", "Error opening MIDI file", JOptionPane.ERROR_MESSAGE);
            }
        });
        return button;
    }
}
