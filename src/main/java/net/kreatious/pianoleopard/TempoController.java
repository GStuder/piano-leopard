package net.kreatious.pianoleopard;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import net.kreatious.pianoleopard.midi.OutputModel;

import com.google.common.annotations.VisibleForTesting;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * Provides a view for controlling the current tempo factor.
 *
 * @author Jay-R Studer
 */
class TempoController {
    @VisibleForTesting
    static final int MIDPOINT = 800;

    @VisibleForTesting
    static final double HIGHEST_TEMPO_FACTOR = 5;

    private TempoController() {
    }

    /**
     * Constructs a view and associates it with its controller
     */
    static Component create(OutputModel outputModel) {
        final JSlider slider = new JSlider(0, MIDPOINT * 2, MIDPOINT);
        slider.addChangeListener(e -> outputModel.setTempoFactor((float) linearToFactor(slider.getValue())));
        slider.setMinorTickSpacing(MIDPOINT / 8);
        slider.setMajorTickSpacing(MIDPOINT / 4);
        slider.setSnapToTicks(true);
        slider.setPaintTicks(true);

        final JPanel panel = new JPanel(new FormLayout(new ColumnSpec[] { FormFactory.DEFAULT_COLSPEC,
                FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC }, new RowSpec[] {
                FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC }));
        panel.add(new JLabel("Slower"), "1, 1");
        panel.add(slider, "3, 1");
        panel.add(new JLabel("Faster"), "5, 1");

        final JLabel lblTempo = new JLabel("100% Tempo");
        slider.addChangeListener(e -> lblTempo.setText(linearToPercent(slider.getValue()) + " Tempo"));
        panel.add(lblTempo, "3, 2, 1, 1, center, fill");

        panel.setVisible(false);
        outputModel.addOpenListener(sequence -> panel.setVisible(true));
        return panel;
    }

    /**
     * Converts a slider value to a percentage
     */
    private static String linearToPercent(int i) {
        return (int) (linearToFactor(i) * 100) + "%";
    }

    /**
     * Converts from a value to tempo factor in the range [1/highest, highest]
     */
    @VisibleForTesting
    static double linearToFactor(int i) {
        return Math.pow(HIGHEST_TEMPO_FACTOR, normalize(i));
    }

    /**
     * Converts from tempo factor to a value in the range [0, 2 * midpoint]
     */
    @VisibleForTesting
    static int factorToLinear(double y) {
        return (int) (MIDPOINT * (Math.log(y) / Math.log(HIGHEST_TEMPO_FACTOR)) + MIDPOINT);
    }

    /**
     * Normalizes a value from the slider to the range -1..1
     */
    private static double normalize(int i) {
        return (i - MIDPOINT) / (double) MIDPOINT;
    }
}
