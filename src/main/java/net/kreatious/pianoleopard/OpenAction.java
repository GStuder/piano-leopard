package net.kreatious.pianoleopard;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import net.kreatious.pianoleopard.midi.sequencer.OutputModel;

/**
 * Provides the action for the open button
 *
 * @author Jay-R Studer
 */
class OpenAction extends AbstractAction {
    private static final long serialVersionUID = 5269422128181087632L;
    private static final String DIRECTORY_PREFERENCE = "defaultOpenDirectory";
    private final JFileChooser chooser;

    private final Component parent;
    private final Preferences preferences;
    private final OutputModel outputModel;

    private OpenAction(Component parent, Preferences preferences, OutputModel outputModel) {
        this.parent = parent;
        this.preferences = preferences;
        this.outputModel = outputModel;

        chooser = new JFileChooser();
        Optional.ofNullable(preferences.get(DIRECTORY_PREFERENCE, null)).map(File::new)
                .ifPresent(chooser::setCurrentDirectory);
        chooser.setPreferredSize(new Dimension(600, 400));
    }

    static Component create(Component parent, Preferences preferences, OutputModel outputModel) {
        final Action action = new OpenAction(parent, preferences, outputModel);
        action.putValue(NAME, "Open...");
        action.setEnabled(false);
        outputModel.addOutputDeviceListener(info -> action.setEnabled(true));
        return new JButton(action);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
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
    }
}
