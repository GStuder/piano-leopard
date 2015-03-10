package net.kreatious.pianoleopard.keyboardselect;

import java.awt.Container;
import java.util.prefs.Preferences;
import java.util.stream.IntStream;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * Provides a GUI element for selecting the options of a lighted keyboard
 *
 * @author Jay-R Studer
 */
public class LightedKeyboardSelector {
    /**
     * The preference storage location for the navigation channel.
     * <p>
     * The channel is stored in the range 0..15.
     */
    public static final String NAV_CHANNEL_PREFERENCE = "navigationChannel";

    private final JComboBox<Integer> channels = new JComboBox<>(IntStream.rangeClosed(1, 16).boxed()
            .toArray(Integer[]::new));

    /**
     * Constructs a new {@link LightedKeyboardSelector} for selecting the
     * options of a lighted keyboard
     */
    LightedKeyboardSelector() {
    }

    /**
     * Adds this selector to the specified container.
     * <p>
     * The container must have a JGoodies FormLayout with 1 row and 5 columns.
     *
     * @param container
     *            the container to add to
     * @param x
     *            the column to add to
     * @param y
     *            the row to add to
     * @param width
     *            the number of columns to take up
     */
    void addToContainer(Container container, int x, int y, int width) {
        final JPanel panel = new JPanel(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default:grow"),
                FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, },
                new RowSpec[] { FormFactory.DEFAULT_ROWSPEC }));
        final JLabel lblInfo = new JLabel(new ImageIcon(LightedKeyboardSelector.class.getResource("/help.png")));
        lblInfo.setToolTipText("<html>Navigation channels are used by MIDI keyboards with lighted keys.<br>"
                + "They determine which MIDI channel is used to light up the keys.</html>");
        panel.add(channels, "1, 1");
        panel.add(lblInfo, "3, 1");

        container.add(new JLabel("Navigation Channel:"), x + ", " + y + ", right, default");
        container.add(panel, x + 2 + ", " + y + ", " + (width - 2) + ", 1, fill, default");
    }

    /**
     * Loads the current settings from the user's preferences.
     *
     * @param preferences
     *            the preferences to load from
     */
    void load(Preferences preferences) {
        channels.setSelectedItem(preferences.getInt(NAV_CHANNEL_PREFERENCE, 3) + 1);
    }

    /**
     * Saves the current setting to the user's preferences.
     * <p>
     * Any registered preference listeners will be called.
     *
     * @param preferences
     *            the preferences to save to
     */
    void save(Preferences preferences) {
        preferences.put(NAV_CHANNEL_PREFERENCE, Integer.toString((Integer) channels.getSelectedItem() - 1));
    }
}
