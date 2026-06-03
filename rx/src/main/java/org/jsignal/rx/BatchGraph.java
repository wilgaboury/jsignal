package org.jsignal.rx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import java.util.*;

/**
 * Graph analyzing batch implementation that searches the dependency graph and executes effects in topological order
 */
public class BatchGraph implements Batch {
  private static final Logger logger = LoggerFactory.getLogger(BatchGraph.class);

  private final SortedSet<GraphNode> batch;
  private final Map<Effect, GraphNode> graph;

  public BatchGraph() {
    this.batch = new TreeSet<>(Comparator.<GraphNode>comparingInt(n -> n.inbound.size())
            .thenComparingInt(n -> n.effect.getId()));
    this.graph = new HashMap<>();
  }

  @Override
  public void add(EffectRef ref) {
    ref.getEffect().ifPresent(effect -> {
      var node = graph.computeIfAbsent(effect, GraphNode::new);
      batch.add(node);
      var stack = new HashSet<GraphNode>();
      stack.add(node);
      dfs(stack, node);
    });
  }

  private void dfs(Set<GraphNode> stack, GraphNode node) {
    if (stack.contains(node)) {
      // cycle detected, no-op
      return;
    }

    for (Signal<?> signal : node.effect.getOutbound()) {
      for (EffectRef neighbor : signal.effects()) {
        neighbor.getEffect().ifPresent(effect -> {
          if (graph.containsKey(effect)) {
            var next = graph.get(effect);
            node.outbound.add(next);
            next.update(batch, () -> next.inbound.add(node));
          } else {
            var next = new GraphNode(effect);
            graph.put(effect, next);
            node.outbound.add(next);
            next.inbound.add(node);

            stack.add(next);
            dfs(stack, next);
            stack.remove(next);
          }
        });
      }
    }
  }

  @Override
  public void commit() {
    while (!batch.isEmpty()) {
      var node = batch.removeFirst(); // remove node with fewest path dependencies
      graph.remove(node.effect);
      for (var outbound : node.outbound) {
        outbound.update(batch, () -> outbound.inbound.remove(node));
      }
      node.outbound.clear();

      try {
        node.effect.run();
      } catch (Exception e) {
        logger.error("uncaught exception in effect", e);
      }
    }
  }

  private static class GraphNode {
    public final Effect effect;
    public final Set<GraphNode> inbound;
    public final Set<GraphNode> outbound;

    public GraphNode(Effect effect) {
      this.effect = effect;
      this.inbound = new LinkedHashSet<>();
      this.outbound = new LinkedHashSet<>();
    }

    public void update(Set<GraphNode> batch, Runnable runnable) {
      if (batch.remove(this)) {
        runnable.run();
        batch.add(this);
      }
    }
  }
}
