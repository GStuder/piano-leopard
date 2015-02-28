package net.kreatious.pianoleopard.painter;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferStrategy;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;

import net.kreatious.pianoleopard.midi.ParsedSequence;

import com.google.common.annotations.VisibleForTesting;

/**
 * Renders the currently playing sequence into a panel using double buffering.
 *
 * @author Jay-R Studer
 */
public class PainterPanel {
    private final JPanel panel = new JPanel();
    private final Canvas canvas;
    private final Thread painterThread;

    private volatile long currentTime;
    private volatile ParsedSequence sequence = ParsedSequence.createEmpty();

    /**
     * Constructs a new {@link PainterPanel}
     */
    public PainterPanel() {
        this(new Canvas());
    }

    @VisibleForTesting
    PainterPanel(Canvas canvas) {
        this.canvas = canvas;

        panel.setLayout(new BorderLayout());
        panel.add(canvas);
        panel.setIgnoreRepaint(true);

        final Painter painter = new Painter(canvas.getSize());
        canvas.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                painter.setComponentDimensions(e.getComponent().getSize());
            }
        });

        painterThread = new Thread("painter thread") {
            @Override
            public synchronized void run() {
                try {
                    final BufferStrategy buffer = canvas.getBufferStrategy();

                    while (true) {
                        final Graphics2D graphics = (Graphics2D) buffer.getDrawGraphics();
                        painter.paint(graphics, currentTime, sequence);
                        graphics.dispose();
                        buffer.show();

                        wait(TimeUnit.SECONDS.toMillis(1) / 60);
                    }
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };
    }

    /**
     * Starts drawing the current parsed sequence into this component.
     *
     * @throws IllegalThreadStateException
     *             if the painter panel has already been started
     */
    public void start() {
        canvas.createBufferStrategy(2);
        painterThread.start();
    }

    /**
     * Gets the panel associated with this component
     *
     * @return the panel owned by this component
     */
    public JPanel getPanel() {
        return panel;
    }

    /**
     * @param currentTime
     *            the current song time in microseconds
     */
    public void setCurrentTime(long currentTime) {
        synchronized (painterThread) {
            this.currentTime = currentTime;
            painterThread.notify();
        }
    }

    /**
     * Sets the current sequence displayed by this panel
     *
     * @param sequence
     *            the parsed sequence to set
     */
    public void setCurrentSequence(ParsedSequence sequence) {
        synchronized (painterThread) {
            this.sequence = sequence;
            painterThread.notify();
        }
    }

    /**
     * Stops drawing the current parsed sequence into this component.
     * <p>
     * This can only be called once, and must be called when the application is
     * closed.
     * <p>
     * This method blocks until the painter thread has exited.
     *
     * @throws InterruptedException
     *             if the current thread is interrupted
     */
    public void stop() throws InterruptedException {
        painterThread.interrupt();
        painterThread.join();
    }
}
