package net.kreatious.pianoleopard;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.function.Consumer;

/**
 * Provides a {@link MouseListener} which executes a lambda expression when the
 * mouse enters or leaves.
 *
 * @author Jay-R Studer
 */
class ToggleListener extends MouseAdapter {
    private final Consumer<Boolean> action;

    /**
     * Constructs a new {@link ToggleListener} that calls the specified
     * action when the mouse enters or leaves.
     * <p>
     * The argument supplied to the action can be exclusive-ored with the state
     * of the button in order to implement a preview effect.
     *
     * @param action
     *            the action to take when the mouse enters or leaves. The
     *            supplied argument is true if the mouse has entered, otherwise
     *            it is false.
     */
    ToggleListener(Consumer<Boolean> action) {
        this.action = action;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        action.accept(true);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        action.accept(false);
    }
}
