package net.kreatious.pianoleopard.intervalset;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * Dirty debugging application for visualizing the tree when a test fails
 *
 * @author Jay-R Studer
 */
class DebugVisualization<K extends Comparable<K>, V> extends JDialog {
    private static final long serialVersionUID = -987900044387917878L;

    final IntervalSet<K, V> set;
    final Map<Iterator<V>, Decorations> ranges = new HashMap<>();

    static class OnTestFailureDebugClassRule<K extends Comparable<K>, V> implements TestRule {
        final IntervalSet<K, V> set;
        int failures;
        private final DebugVisualization<K, V> visualization;

        OnTestFailureDebugClassRule(IntervalSet<K, V> set) {
            this.set = set;
            visualization = new DebugVisualization<>(set);
        }

        @Override
        public Statement apply(Statement base, Description description) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    base.evaluate();
                    if (failures > 0) {
                        visualization.setVisible(true);
                    }
                }
            };
        }
    }

    static class OnTestFailureDebugRule<K extends Comparable<K>, V> implements TestRule {
        private static final Color[] TEST_FAILURE_COLORS = new Color[] { Color.RED, Color.GREEN, Color.BLUE,
                Color.ORANGE, Color.PINK, Color.CYAN, Color.MAGENTA, Color.YELLOW };
        private final OnTestFailureDebugClassRule<K, V> classRule;
        private final Interval<K> testRange;

        OnTestFailureDebugRule(OnTestFailureDebugClassRule<K, V> classRule, Interval<K> testRange) {
            this.classRule = classRule;
            this.testRange = testRange;
        }

        @Override
        public Statement apply(Statement base, Description description) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    try {
                        base.evaluate();
                    } catch (final AssertionError e) {
                        if (classRule.failures < TEST_FAILURE_COLORS.length && classRule.failures >= 0) {
                            classRule.visualization.show(testRange.getLow(), testRange.getHigh(),
                                    TEST_FAILURE_COLORS[classRule.failures++]);
                        }
                        throw e;
                    }
                }
            };
        }
    }

    private class Decorations {
        private final String searchInterval;
        private final int number = ranges.size();
        private String action = "";
        private int step;
        private Entry<K, V> previousPointer = set.getRoot().get();
        private Entry<K, V> pointer = set.getRoot().get();
        private final Color color;
        private final Set<Entry<K, V>> expected;
        private final Set<Entry<K, V>> visited = new HashSet<>();

        Decorations(K low, K high, Color color) {
            searchInterval = "[" + low + ", " + high + "]";
            this.color = color;
            final List<Entry<K, V>> children = new ArrayList<>();
            visitChildren(set.getRoot().get(), children::add);
            expected = children.stream().filter(child -> child.getKey().containsInterval(new Interval<>(low, high)))
                    .collect(toSet());
        }
    }

    private class TreeDrawingPanel extends JPanel {
        private static final long serialVersionUID = -8045457048224352208L;

        private TreeDrawingPanel() {
            final Map<Entry<K, V>, Rectangle> positions = new HashMap<>();
            calculateNodePositions(positions, Collections.singletonList(set.getRoot()), 1, maxLevel(set.getRoot()), 100);
            setPreferredSize(new Dimension(positions.values().stream().map(rect -> rect.x + rect.width)
                    .max(Integer::compare).get(), positions.values().stream().map(rect -> rect.y + rect.height)
                    .max(Integer::compare).get()));

        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);

            final Map<Entry<K, V>, Rectangle> positions = new HashMap<>();
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

            final Map<Entry<K, V>, Integer> numberTimesPainted = new HashMap<>();
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
    }

    DebugVisualization(IntervalSet<K, V> set) {
        this.set = set;

        setModalityType(ModalityType.TOOLKIT_MODAL);
        setResizable(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.BUTTON_COLSPEC,
                FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow") }, new RowSpec[] {
                FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                RowSpec.decode("default:grow") }));

        final TreeDrawingPanel canvasTree = new TreeDrawingPanel();
        final JButton step = new JButton("Step");
        step.addActionListener(event -> {
            ranges.forEach(new BiConsumer<Iterator<V>, Decorations>() {
                final Map<Set<V>, Entry<K, V>> entries = new IdentityHashMap<Set<V>, Entry<K, V>>() {
                    private static final long serialVersionUID = 7676204394757535429L;
                    {
                        visitChildren(set.getRoot().get(), entry -> put(entry.getValues(), entry));
                    }
                };

                @Override
                public void accept(Iterator<V> iterator, DebugVisualization<K, V>.Decorations decorator) {
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
                }
            });
            canvasTree.repaint();
        });
        add(step, "2, 2, 1, 1");
        add(new JScrollPane(canvasTree), "1, 4, 4, 1");
        pack();
    }

    /**
     * Adds the range that failed
     */
    void show(K low, K high, Color color) {
        ranges.put(set.subSet(low, high).iterator(), new Decorations(low, high, color));
    }

    private int maxLevel(Optional<Entry<K, V>> node) {
        if (!node.isPresent()) {
            return 0;
        }

        return Math.max(maxLevel(node.get().getLeft()), maxLevel(node.get().getRight())) + 1;
    }

    private static int stringHeight(Graphics g, String s) {
        return (int) g.getFontMetrics().getStringBounds(s, g).getHeight();
    }

    private static void drawCenteredString(Graphics g, String s, Rectangle position) {
        final Rectangle2D bounds = g.getFontMetrics().getStringBounds(s, g);
        g.drawString(s, (int) (position.getCenterX() - bounds.getCenterX()),
                (int) (position.getCenterY() - bounds.getCenterY()));
    }

    private void drawTraversal(Graphics g, Entry<K, V> start, Entry<K, V> end, int offsetY,
            Map<Entry<K, V>, Rectangle> positions) {
        final List<Entry<K, V>> successors = shortestPath(start, end);

        for (int i = 0; i < successors.size() - 1; i++) {
            final Rectangle startPosition = positions.get(successors.get(i));
            final Rectangle endPosition = positions.get(successors.get(i + 1));
            g.drawLine(startPosition.x, (int) startPosition.getCenterY() + offsetY, endPosition.x,
                    (int) endPosition.getCenterY() + offsetY);
        }
    }

    private List<Entry<K, V>> shortestPath(Entry<K, V> start, Entry<K, V> end) {
        final List<Entry<K, V>> result = new ArrayList<>();
        for (int i = 0;; i++) {
            if (shortestPath(start, end, i, result)) {
                Collections.reverse(result);
                return result;
            }
        }
    }

    private boolean shortestPath(Entry<K, V> start, Entry<K, V> end, int depth, List<Entry<K, V>> result) {
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

    private void visitChildren(Entry<K, V> node, Consumer<Entry<K, V>> consumer) {
        node.getLeft().ifPresent(next -> visitChildren(next, consumer));
        consumer.accept(node);
        node.getRight().ifPresent(next -> visitChildren(next, consumer));
    }

    private void calculateNodePositions(Map<Entry<K, V>, Rectangle> result, List<Optional<Entry<K, V>>> nodes,
            int level, int maxLevel, int y) {
        if (!nodes.stream().anyMatch(Optional::isPresent)) {
            return;
        }

        final int NODE_WIDTH = 76;
        final int NODE_HEIGHT = 30;
        final int SPACE_MULTIPLIER = NODE_WIDTH / 2;

        final List<Optional<Entry<K, V>>> newNodes = nodes
                .stream()
                .flatMap(
                        node -> Stream.<Optional<Entry<K, V>>> of(node.flatMap(Entry::getLeft),
                                node.flatMap(Entry::getRight))).collect(toList());
        final int betweenSpace = (int) Math.pow(2, maxLevel - level) * SPACE_MULTIPLIER;
        int x = betweenSpace / 2;

        boolean overlapPossible = false;
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).isPresent()) {
                if (maxLevel == level && overlapPossible) {
                    // prevent overlapping nodes at last level
                    result.put(nodes.get(i).get(), new Rectangle(x, y + NODE_HEIGHT, NODE_WIDTH, NODE_HEIGHT));
                    overlapPossible = false;
                } else {
                    result.put(nodes.get(i).get(), new Rectangle(x, y, NODE_WIDTH, NODE_HEIGHT));
                    overlapPossible = true;
                }
            } else {
                overlapPossible = false;
            }
            x += betweenSpace;
        }

        final int distanceToNextRow = betweenSpace / 16;
        calculateNodePositions(result, newNodes, level + 1, maxLevel, y + distanceToNextRow + NODE_HEIGHT);
    }
}
