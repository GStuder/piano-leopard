package net.kreatious.pianoleopard;

import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.prefs.Preferences;

import javax.sound.midi.MidiUnavailableException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import net.kreatious.pianoleopard.keyboardselect.SelectKeyboardDialog;
import net.kreatious.pianoleopard.midi.sequencer.InputModel;
import net.kreatious.pianoleopard.midi.sequencer.LightedKeyboardController;
import net.kreatious.pianoleopard.midi.sequencer.OutputModel;
import net.kreatious.pianoleopard.midi.sequencer.SystemSequencerFactory;
import net.kreatious.pianoleopard.painter.PainterPanel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * Provides the main application interface for learning how to play the piano.
 *
 * @author Jay-R Studer
 */
public class Main {
    /**
     * Application main entry point
     *
     * @param args
     *            unused
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());

            final JFrame applet = create();
            applet.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            applet.setIconImage(Toolkit.getDefaultToolkit().getImage(
                    SelectKeyboardDialog.class.getResource("/application_lightning.png")));
            applet.setTitle("Piano Leopard");
            applet.setVisible(true);
        } catch (final Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Unable to continue: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static JFrame create() throws MidiUnavailableException {
        final OutputModel outputModel = new OutputModel(new SystemSequencerFactory());
        final InputModel inputModel = InputModel.create(outputModel);
        final Preferences preferences = Preferences.userNodeForPackage(Main.class);
        LightedKeyboardController.create(outputModel);

        final JFrame frame = new JFrame();
        frame.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
                FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow") },
                new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                        FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:default:grow") }));
        frame.add(PlayAction.create(outputModel), "6, 2");
        frame.add(OpenAction.create(frame, preferences, outputModel), "4, 2");
        frame.add(KeyboardAction.create(frame, preferences, outputModel, inputModel), "2, 2");
        frame.add(PainterPanel.create(outputModel, inputModel), "1, 4, 8, 1, fill, fill");
        frame.pack();
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    outputModel.close();
                    inputModel.close();
                } catch (final InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        return frame;
    }
}
