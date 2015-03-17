package net.kreatious.pianoleopard;

import java.util.function.Consumer;

import net.kreatious.pianoleopard.midi.InputModel;
import net.kreatious.pianoleopard.midi.event.Event;
import net.kreatious.pianoleopard.midi.event.NoteEvent;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.win32.StdCallLibrary;

/**
 * Keeps the system from going into an idle state
 *
 * @author JStuder
 */
class AntiIdle {
    private static final int ES_DISPLAY_REQUIRED = 0x2;
    private static final int ES_SYSTEM_REQUIRED = 0x1;

    private interface Kernel32 extends StdCallLibrary {
        int SetThreadExecutionState(int esFlags);
    }

    static void create(InputModel inputModel) {
        try {
            if (!Platform.isWindows()) {
                return;
            }

            // Try calling the function to see if it can be linked
            final Kernel32 kernel32 = (Kernel32) Native.loadLibrary("kernel32", Kernel32.class);
            final Consumer<Event> listener = event -> kernel32.SetThreadExecutionState(ES_SYSTEM_REQUIRED
                    | ES_DISPLAY_REQUIRED);
            listener.accept(new NoteEvent(0, true, 0));

            inputModel.addInputListener(listener);
        } catch (final UnsatisfiedLinkError e) {
            // Windows API is not supported on the current platform.
            e.printStackTrace();
        }
    }
}
