package net.kreatious.pianoleopard;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import net.kreatious.pianoleopard.keyboardselect.Keyboard;
import net.kreatious.pianoleopard.keyboardselect.SelectKeyboardDialog;
import net.kreatious.pianoleopard.keyboardselect.SystemMidiDeviceFactory;

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
     * @throws Exception
     *             if an error occurs
     */
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new NimbusLookAndFeel());

        final JFrame applet = new SelectKeyboardDialog(new Keyboard(), new SystemMidiDeviceFactory()).getFrame();
        applet.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        applet.setTitle("Piano Leopard");
        applet.setVisible(true);
    }
}
