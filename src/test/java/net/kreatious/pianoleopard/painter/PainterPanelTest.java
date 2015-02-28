package net.kreatious.pianoleopard.painter;

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;

import org.junit.After;
import org.junit.Test;

/**
 * Tests for {@link PainterPanel}
 *
 * @author Jay-R Studer
 */
public class PainterPanelTest {
    private final Canvas canvas = mock(Canvas.class);
    private final BufferStrategy bufferStrategy = mock(BufferStrategy.class);
    private final Graphics2D graphics = mock(Graphics2D.class);
    private final PainterPanel panel;

    /**
     * Constructs a new {@link PainterPanelTest}
     */
    public PainterPanelTest() {
        given(canvas.getBufferStrategy()).willReturn(bufferStrategy);
        given(canvas.getSize()).willReturn(new Dimension(100, 100));
        given(bufferStrategy.getDrawGraphics()).willReturn(graphics);

        panel = new PainterPanel(canvas);
        panel.start();
    }

    /**
     * Tests that double buffering is initialized
     */
    @Test
    public void testDoubleBuffering() {
        verify(canvas).createBufferStrategy(2);
        verify(bufferStrategy, timeout(100).atLeastOnce()).show();
        assertThat(panel.getPanel().getIgnoreRepaint(), is(true));
    }

    /**
     * Tests that something is drawn into the panel
     */
    @Test
    public void testDrawing() {
        verify(bufferStrategy, timeout(100).atLeastOnce()).show();
        verify(graphics, atLeastOnce()).setColor(any());
        verify(graphics, atLeastOnce()).fillRect(anyInt(), anyInt(), anyInt(), anyInt());

        synchronized (panel.getPanel().getTreeLock()) {
            assertThat(panel.getPanel().getComponents(), is(arrayContaining(canvas)));
        }
    }

    /**
     * Stops the painter thread
     *
     * @throws InterruptedException
     *             if the current thread is interrupted
     */
    @After
    public void stopPainting() throws InterruptedException {
        panel.stop();
    }
}
