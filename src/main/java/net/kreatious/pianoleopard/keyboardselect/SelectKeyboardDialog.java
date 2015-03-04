package net.kreatious.pianoleopard.keyboardselect;

import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.event.KeyEvent;
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
        dialog.setResizable(false);
        dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
        dialog.setTitle("MIDI Keyboard Selection");
        dialog.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.BUTTON_COLSPEC,
                FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.BUTTON_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.BUTTON_COLSPEC,
                FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, }));

        dialog.add(new JLabel("Select which MIDI keyboard to use"), "2, 2, 7, 1");

        input = new KeyboardSelector("Input:", device -> device.getMaxTransmitters() != 0, deviceFactory);
        keyboard.map(Keyboard::getInput).ifPresent(input::setSelectedDevice);
        dialog.add(input.getPanel(), "2, 4, 5, 1");

        output = new KeyboardSelector("Output:", device -> device.getMaxReceivers() != 0, deviceFactory);
        keyboard.map(Keyboard::getOutput).ifPresent(output::setSelectedDevice);
        dialog.add(output.getPanel(), "2, 6, 5, 1");

        final JButton btnOk = new JButton("OK");
        btnOk.addActionListener(event -> apply());
        btnOk.addActionListener(event -> dialog.dispose());
        dialog.add(btnOk, "6, 8");

        final JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setMnemonic(KeyEvent.VK_R);
        btnRefresh.addActionListener(event -> input.reloadDevices());
        btnRefresh.addActionListener(event -> output.reloadDevices());
        dialog.add(btnRefresh, "2, 8");

        final JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(event -> dialog.dispose());
        dialog.add(btnCancel, "8, 8");

        dialog.pack();
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
     * <p>
     * If a parent is not provided, the dialog will not be shown and this method
     * returns immediately.
     *
     * @param parent
     *            the parent component to layout this dialog relative to
     * @return the selected keyboard, unless the dialog was cancelled
     */
    public Optional<Keyboard> showDialog(Optional<Component> parent) {
        parent.ifPresent(dialog::setLocationRelativeTo);

        final Optional<Keyboard> oldKeyboard = keyboard;
        input.reloadDevices();
        output.reloadDevices();
        dialog.setVisible(parent.isPresent());
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
