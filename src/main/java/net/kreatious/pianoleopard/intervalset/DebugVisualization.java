package net.kreatious.pianoleopard.intervalset;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * Debugging application for visualizing the tree
 *
 * @author Jay-R Studer
 */
class DebugVisualization extends JFrame {
    public static void main(String[] args) {
        new DebugVisualization().setVisible(true);
    }

    private static final long serialVersionUID = -987900044387917878L;

    final IntervalSet<Integer, String> set = new IntervalSet<>();
    final Map<Iterator<String>, Decorator> ranges = new HashMap<>();

    private class Decorator {
        private final String searchInterval;
        private final int number = ranges.size();
        private String action = "";
        private int step;
        private Entry<Integer, String> previousPointer = set.getRoot().get();
        private Entry<Integer, String> pointer = set.getRoot().get();
        private final Color color;
        private final Set<Entry<Integer, String>> expected;
        private final Set<Entry<Integer, String>> visited = new HashSet<>();

        Decorator(int low, int high, Color color) {
            final int l = Math.min(low, high);
            final int h = Math.max(low, high);
            searchInterval = "[" + l + ", " + h + "]";
            this.color = color;
            final List<Entry<Integer, String>> children = new ArrayList<>();
            visitChildren(set.getRoot().get(), children::add);
            expected = children.stream().filter(child -> child.getKey().containsInterval(new Interval<>(l, h)))
                    .collect(toSet());
        }
    }

    private DebugVisualization() {
        final List<Interval<Integer>> intervals = new ArrayList<>();
        for (int i = 0; i != 5; i++) {
            intervals.add(new Interval<>(i, i));
            intervals.add(new Interval<>(10 + i * 2, 10 + i * 2 + 1));
            intervals.add(new Interval<>(20 + i * 2, 20 + i * 2 + 2));
            intervals.add(new Interval<>(40 + i * 2, 40 + i * 2 + 3));
            intervals.add(new Interval<>(60 + i * 2, 60 + i * 2 + 4));
        }
        intervals.forEach(interval -> set.put(interval.getLow(), interval.getHigh(), interval.toString()));

        final Map<String, Entry<Integer, String>> entries = new HashMap<>();
        visitChildren(set.getRoot().get(), entry -> entries.put(entry.getValue(), entry));
        show(45, 50, Color.RED);
        show(65, 70, Color.BLUE);
        show(42, 60, Color.GREEN);
        show(12, 16, Color.ORANGE);
        show(3, 3, Color.CYAN);
        show(4, 4, Color.MAGENTA);
        show(10, 20, Color.PINK);

        setExtendedState(MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.BUTTON_COLSPEC,
                FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, },
                new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                        RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC }));

        final JButton step = new JButton("Step");
        step.addActionListener(event -> {
            ranges.forEach((iterator, decorator) -> {
                if (iterator == null) {
                    return;
                } else if (iterator.hasNext()) {
                    decorator.previousPointer = decorator.pointer;
                    decorator.pointer = entries.get(iterator.next());
                    decorator.step++;
                    decorator.action = decorator.searchInterval + " #" + decorator.step
                            + (decorator.expected.contains(decorator.pointer) ? "" : " (WRONG)");
                    decorator.visited.add(decorator.pointer);
                } else {
                    decorator.action = decorator.searchInterval + " #" + decorator.step + " (done)";
                }
            });
            this.repaint();
        });
        getContentPane().add(step, "2, 2, 1, 1");

        final JSlider minRange = new JSlider(0, 80, 0);
        final JSlider maxRange = new JSlider(0, 80, 1);
        final ChangeListener listener = event -> {
            final Decorator decorator = new Decorator(minRange.getValue(), maxRange.getValue(), Color.DARK_GRAY);
            decorator.visited.addAll(decorator.expected);
            ranges.put(null, decorator);
            this.repaint();
        };
        minRange.addChangeListener(listener);
        maxRange.addChangeListener(listener);
        getContentPane().add(minRange, "4, 2, 1, 1");
        getContentPane().add(maxRange, "4, 4, 1, 1");
    }

    private void show(int low, int high, Color color) {
        ranges.put(set.subSet(low, high).iterator(), new Decorator(low, high, color));
    }

    private int maxLevel(Optional<Entry<Integer, String>> node) {
        if (!node.isPresent()) {
            return 0;
        }

        return Math.max(maxLevel(node.get().getLeft()), maxLevel(node.get().getRight())) + 1;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        final Map<Entry<Integer, String>, Rectangle> positions = new HashMap<>();
        calculateNodePositions(positions, Collections.singletonList(set.getRoot()), 1, maxLevel(set.getRoot()), 100);
        positions.forEach((entry, position) -> {
            g.setColor(Color.BLACK);
            drawCenteredString(g, entry.toString(), position);

            entry.getParent().ifPresent(
                    parent -> {
                        final Rectangle parentPosition = positions.get(parent);
                        g.drawLine((int) parentPosition.getCenterX(), (int) parentPosition.getMaxY(),
                                (int) position.getCenterX(), (int) position.getMinY());
                    });

            g.drawRect(position.x, position.y, position.width, position.height);
        });

        final Map<Entry<Integer, String>, Integer> numberTimesPainted = new HashMap<>();
        ranges.values().forEach(
                decorator -> {
                    final int timesPainted = numberTimesPainted.compute(decorator.pointer,
                            (key, value) -> value == null ? 0 : value + 1);

                    final Rectangle position = (Rectangle) positions.get(decorator.pointer).clone();
                    g.setColor(decorator.color);
                    position.x += position.width + 2;
                    position.y += timesPainted * stringHeight(g, decorator.action);
                    g.drawString(decorator.action, position.x, position.y);
                    drawTraversal(g, decorator.previousPointer, decorator.pointer, decorator.number, positions);

                    decorator.expected.stream().map(positions::get).forEach(p -> {
                        g.drawRect(p.x, p.y, p.width, p.height - decorator.number);
                    });

                    decorator.visited.stream().map(positions::get).forEach(p -> {
                        g.drawRect(p.x + 1, p.y + 1, p.width - 2, p.height - 2 - decorator.number);
                    });
                });
    }

    private static int stringHeight(Graphics g, String s) {
        return (int) g.getFontMetrics().getStringBounds(s, g).getHeight();
    }

    private static void drawCenteredString(Graphics g, String s, Rectangle position) {
        final Rectangle2D bounds = g.getFontMetrics().getStringBounds(s, g);
        g.drawString(s, (int) (position.getCenterX() - bounds.getCenterX()),
                (int) (position.getCenterY() - bounds.getCenterY()));
    }

    private static <K extends Comparable<K>, V> void drawTraversal(Graphics g, Entry<K, V> start, Entry<K, V> end,
            int offsetY, Map<Entry<K, V>, Rectangle> positions) {
        final List<Entry<K, V>> successors = shortestPath(start, end);

        for (int i = 0; i < successors.size() - 1; i++) {
            final Rectangle startPosition = positions.get(successors.get(i));
            final Rectangle endPosition = positions.get(successors.get(i + 1));
            g.drawLine(startPosition.x, (int) startPosition.getCenterY() + offsetY, endPosition.x,
                    (int) endPosition.getCenterY() + offsetY);
        }
    }

    private static <K extends Comparable<K>, V> List<Entry<K, V>> shortestPath(Entry<K, V> start, Entry<K, V> end) {
        final List<Entry<K, V>> result = new ArrayList<>();
        for (int i = 0;; i++) {
            if (shortestPath(start, end, i, result)) {
                Collections.reverse(result);
                return result;
            }
        }
    }

    private static <K extends Comparable<K>, V> boolean shortestPath(Entry<K, V> start, Entry<K, V> end, int depth,
            List<Entry<K, V>> result) {
        if (depth < 0) {
            return false;
        }
        if (start.equals(end)) {
            result.add(end);
            return true;
        }
        for (final Entry<K, V> next : Stream.of(start.getLeft(), start.getParent(), start.getRight())
                .filter(Optional::isPresent).map(Optional::get).collect(toList())) {
            if (shortestPath(next, end, depth - 1, result)) {
                result.add(start);
                return true;
            }
        }
        return false;
    }

    private static <K extends Comparable<K>, V> void visitChildren(Entry<K, V> node, Consumer<Entry<K, V>> consumer) {
        node.getLeft().ifPresent(next -> visitChildren(next, consumer));
        consumer.accept(node);
        node.getRight().ifPresent(next -> visitChildren(next, consumer));
    }

    private static <K extends Comparable<K>, V> void calculateNodePositions(Map<Entry<K, V>, Rectangle> result,
            List<Optional<Entry<K, V>>> nodes, int level, int maxLevel, int y) {
        if (!nodes.stream().anyMatch(Optional::isPresent)) {
            return;
        }

        final int NODE_WIDTH = 70;
        final int NODE_HEIGHT = 30;
        final int SPACE_MULTIPLIER = 20;

        final List<Optional<Entry<K, V>>> newNodes = nodes
                .stream()
                .flatMap(
                        node -> Stream.<Optional<Entry<K, V>>> of(node.flatMap(Entry::getLeft),
                                node.flatMap(Entry::getRight))).collect(toList());
        int x = (int) Math.pow(2, maxLevel - level) * SPACE_MULTIPLIER;
        final int betweenSpace = (int) Math.pow(2, maxLevel - level + 1) * SPACE_MULTIPLIER;

        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).isPresent()) {
                result.put(nodes.get(i).get(), new Rectangle(x, y, NODE_WIDTH, NODE_HEIGHT));
            }
            x += betweenSpace;
        }

        final int distanceToNextRow = (int) Math.pow(2, Math.max(maxLevel - level - 2, 0)) * SPACE_MULTIPLIER;
        calculateNodePositions(result, newNodes, level + 1, maxLevel, y + distanceToNextRow + NODE_HEIGHT);
    }
}
