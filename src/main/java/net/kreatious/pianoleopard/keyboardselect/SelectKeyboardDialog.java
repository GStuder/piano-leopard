package net.kreatious.pianoleopard.keyboardselect;

import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * Provides the GUI for selecting the desired MIDI device
 *
 * @author Jay-R Studer
 */
public class SelectKeyboardDialog {
    private final KeyboardSelector input;
    private final KeyboardSelector output;
    private final Keyboard keyboard;
    private final JFrame frame = new JFrame();

    /**
     * Constructs a new SelectKeyboardDialog for selecting the desired MIDI
     * device.
     *
     * @param keyboard
     *            the {@link Keyboard} model to modify
     * @param deviceFactory
     *            the {@link MidiDeviceFactory} for obtaining available MIDI
     *            devices
     */
    public SelectKeyboardDialog(Keyboard keyboard, MidiDeviceFactory deviceFactory) {
        this.keyboard = keyboard;
        frame.setIconImage(Toolkit.getDefaultToolkit().getImage(
                SelectKeyboardDialog.class.getResource("/keyboard_configuration.png")));
        frame.setTitle("MIDI Keyboard Selection");
        frame.getContentPane().setLayout(
                new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
                        FormFactory.RELATED_GAP_COLSPEC, FormFactory.BUTTON_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
                        FormFactory.BUTTON_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, },
                        new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                                FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                                FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                                FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                                FormFactory.RELATED_GAP_ROWSPEC, }));

        frame.getContentPane().add(new JLabel("Select which MIDI keyboard to use"), "2, 2, 5, 1");

        input = new KeyboardSelector("Input:", device -> device.getMaxTransmitters() != 0, deviceFactory);
        input.setSelectedDevice(keyboard.getInput());
        frame.getContentPane().add(input.getPanel(), "2, 4, 5, 1");

        output = new KeyboardSelector("Output:", device -> device.getMaxReceivers() != 0, deviceFactory);
        output.setSelectedDevice(keyboard.getOutput());
        frame.getContentPane().add(output.getPanel(), "2, 6, 5, 1");

        final JButton btnOk = new JButton("OK");
        btnOk.addActionListener(event -> apply());
        btnOk.addActionListener(event -> frame.dispose());
        frame.getContentPane().add(btnOk, "4, 8");

        final JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(event -> frame.dispose());
        frame.getContentPane().add(btnCancel, "6, 8");

        frame.pack();
        frame.setMinimumSize(frame.getSize());
    }

    private void apply() {
        keyboard.setInput(input.getSelectedDevice().get());
        keyboard.setOutput(output.getSelectedDevice().get());
    }

    /**
     * @return the {@link JFrame} associated with this dialog
     */
    public JFrame getFrame() {
        return frame;
    }
}
