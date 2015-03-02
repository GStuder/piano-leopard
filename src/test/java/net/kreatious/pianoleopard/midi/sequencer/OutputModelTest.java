package net.kreatious.pianoleopard.midi.sequencer;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Transmitter;

import net.kreatious.pianoleopard.midi.ParsedSequence;

import org.junit.Test;
import org.mockito.InOrder;

/**
 * Tests for {@link OutputModel}
 *
 * @author Jay-R Studer
 */
public class OutputModelTest {
    private final MidiDevice output = mock(MidiDevice.class);
    private final Sequencer sequencer = mock(Sequencer.class);
    private final OutputModel outputModel;

    /**
     * Constructs a new {@link OutputModelTest}
     *
     * @throws MidiUnavailableException
     *             if the mock sequencer is unavailable
     */
    public OutputModelTest() throws MidiUnavailableException {
        given(output.getReceiver()).willReturn(mock(Receiver.class));
        given(sequencer.getTransmitter()).willReturn(mock(Transmitter.class));
        outputModel = new OutputModel(output, () -> sequencer);
    }

    /**
     * Tests that the model correctly opens and closes the output MIDI device.
     *
     * @throws MidiUnavailableException
     *             exception is never thrown from this test
     * @throws InterruptedException
     *             if the current thread is interrupted
     */
    @Test
    public void testClose() throws MidiUnavailableException, InterruptedException {
        outputModel.close();

        final InOrder order = inOrder(output, sequencer);
        order.verify(output).open();
        order.verify(sequencer).open();
        order.verify(sequencer).close();
        order.verify(output).close();
        order.verifyNoMoreInteractions();
    }

    /**
     * Tests that the model correctly opens and closes the output MIDI device
     * multiple times.
     *
     * @throws MidiUnavailableException
     *             exception is never thrown from this test
     * @throws InterruptedException
     *             if the current thread is interrupted
     */
    @Test
    public void testSetKeyboard() throws MidiUnavailableException, InterruptedException {
        outputModel.setOutputDevice(output);
        outputModel.close();

        final InOrder order = inOrder(output, sequencer);
        order.verify(output).open();
        order.verify(sequencer).open();
        order.verify(sequencer).close();
        order.verify(output).close();
        order.verify(output).open();
        order.verify(sequencer).open();
        order.verify(sequencer).close();
        order.verify(output).close();
        order.verifyNoMoreInteractions();
    }

    /**
     * Tests {@link OutputModel#start()}
     *
     * @throws IOException
     *             if an I/O error occurs
     * @throws InvalidMidiDataException
     *             exception is never thrown from this test
     * @throws InterruptedException
     *             if the current thread is interrupted
     */
    @Test
    public void testStart() throws IOException, InvalidMidiDataException, InterruptedException {
        final Consumer<ParsedSequence> openListener = mock(Consumer.class);
        outputModel.addOpenListener(openListener);
        outputModel.openMidiFile(ClassLoader.getSystemResourceAsStream("grieg_hallofking.mid"));
        outputModel.start();
        outputModel.close();

        final InOrder order = inOrder(openListener, sequencer);
        order.verify(sequencer).setSequence(any());
        order.verify(openListener).accept(any());
        order.verify(sequencer).setMicrosecondPosition(0);
        order.verify(sequencer).start();
        order.verify(sequencer).close();
    }

    /**
     * Tests {@link OutputModel#start()} twice
     *
     * @throws IOException
     *             if an I/O error occurs
     * @throws InvalidMidiDataException
     *             exception is never thrown from this test
     * @throws InterruptedException
     *             if the current thread is interrupted
     */
    @Test
    public void testStartTwice() throws IOException, InvalidMidiDataException, InterruptedException {
        outputModel.openMidiFile(ClassLoader.getSystemResourceAsStream("grieg_hallofking.mid"));
        outputModel.start();
        outputModel.start();
        outputModel.close();

        final InOrder order = inOrder(sequencer);
        order.verify(sequencer).setSequence(any());
        order.verify(sequencer).setMicrosecondPosition(0);
        order.verify(sequencer).start();
        order.verify(sequencer).setMicrosecondPosition(0);
        order.verify(sequencer).start();
        order.verify(sequencer).close();
    }

    /**
     * Tests {@link OutputModel#addCurrentTimeListener(LongConsumer)}
     *
     * @throws IOException
     *             if an I/O error occurs
     * @throws InvalidMidiDataException
     *             exception is never thrown from this test
     * @throws InterruptedException
     *             if the current thread is interrupted
     */
    @Test
    public void testCurrentTimeListener() throws IOException, InvalidMidiDataException, InterruptedException {
        final LongConsumer currentTimeListener = mock(LongConsumer.class);
        outputModel.addCurrentTimeListener(currentTimeListener);
        outputModel.openMidiFile(ClassLoader.getSystemResourceAsStream("grieg_hallofking.mid"));
        outputModel.start();

        verify(currentTimeListener, timeout(120).atLeast(2)).accept(anyLong());

        outputModel.close();
    }
}
