package net.kreatious.pianoleopard.keyboardselect;

import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Toolkit;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import com.google.common.annotations.VisibleForTesting;
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
    private final JDialog dialog;
    private Optional<Keyboard> keyboard;

    /**
     * Constructs a new {@link SelectKeyboardDialog} for selecting the desired
     * MIDI device.
     *
     * @param keyboard
     *            the original {@link Keyboard} model to display, if applicable
     * @param deviceFactory
     *            the {@link MidiDeviceFactory} for obtaining available MIDI
     *            devices
     */
    public SelectKeyboardDialog(Optional<Keyboard> keyboard, MidiDeviceFactory deviceFactory) {
        this.keyboard = keyboard;
        dialog = new JDialog();
        dialog.setModalityType(ModalityType.TOOLKIT_MODAL);
        dialog.setIconImage(Toolkit.getDefaultToolkit().getImage(
                SelectKeyboardDialog.class.getResource("/keyboard_configuration.png")));
        dialog.setTitle("MIDI Keyboard Selection");
        dialog.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.BUTTON_COLSPEC,
                FormFactory.RELATED_GAP_COLSPEC, FormFactory.BUTTON_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, },
                new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                        FormFactory.RELATED_GAP_ROWSPEC, }));

        dialog.add(new JLabel("Select which MIDI keyboard to use"), "2, 2, 5, 1");

        input = new KeyboardSelector("Input:", device -> device.getMaxTransmitters() != 0, deviceFactory);
        keyboard.map(Keyboard::getInput).ifPresent(input::setSelectedDevice);
        dialog.add(input.getPanel(), "2, 4, 5, 1");

        output = new KeyboardSelector("Output:", device -> device.getMaxReceivers() != 0, deviceFactory);
        keyboard.map(Keyboard::getOutput).ifPresent(output::setSelectedDevice);
        dialog.add(output.getPanel(), "2, 6, 5, 1");

        final JButton btnOk = new JButton("OK");
        btnOk.addActionListener(event -> apply());
        btnOk.addActionListener(event -> dialog.dispose());
        dialog.add(btnOk, "4, 8");

        final JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(event -> dialog.dispose());
        dialog.add(btnCancel, "6, 8");

        dialog.pack();
        dialog.setMinimumSize(dialog.getSize());
    }

    private void apply() {
        keyboard = Optional.of(new Keyboard(input.getSelectedDevice().get(), output.getSelectedDevice().get()));
    }

    /**
     * @return the {@link JDialog} associated with this dialog
     */
    @VisibleForTesting
    JDialog getDialog() {
        return dialog;
    }

    /**
     * Shows the select keyboard dialog. This method blocks until a keyboard has
     * been selected.
     *
     * @param parent
     *            the parent component to layout this dialog relative to
     * @return the selected keyboard, unless the dialog was cancelled
     */
    public Optional<Keyboard> showDialog(Optional<Component> parent) {
        parent.ifPresent(dialog::setLocationRelativeTo);

        final Optional<Keyboard> oldKeyboard = keyboard;
        dialog.setVisible(true);
        return oldKeyboard.equals(keyboard) ? Optional.empty() : keyboard;
    }

    /**
     * @return the selected keyboard
     */
    @VisibleForTesting
    Optional<Keyboard> getKeyboard() {
        return keyboard;
    }
}
