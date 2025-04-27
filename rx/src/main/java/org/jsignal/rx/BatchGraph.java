package org.jsignal.rx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Graph analyzing batch implementation that searches the dependency graph and executes effects in topological order
 */
public class BatchGraph implements Batch {
  private static final Logger logger = LoggerFactory.getLogger(BatchGraph.class);

  private final SortedSet<GraphNode> batch;
  private final Map<Effect, GraphNode> graph;

  public BatchGraph() {
    this.batch = new TreeSet<>(Comparator.comparingInt(n -> n.inbound));
    this.graph = new HashMap<>();
  }

  @Override
  public void add(EffectRef ref) {
    dfs(new HashSet<>(), ref).ifPresent(batch::add);
  }

  private Optional<GraphNode> dfs(Set<Effect> stack, EffectRef ref) {
    return ref.getEffect().flatMap(effect -> {
      if (stack.contains(effect)) {
        // cycle detected
        // TODO: add detailed logging
        return Optional.empty();
      } else if (graph.containsKey(effect)) {
        var node = graph.get(effect);
        node.inbound++;
        node.updateSort(batch);
        return Optional.of(node);
      } else {
        var node = new GraphNode(effect);
        graph.put(effect, node);
        stack.add(effect);
        for (Signal<?> signal : effect.getOutbound()) {
          for (EffectRef neighbor : signal.effects()) {
            dfs(stack, neighbor).ifPresent(node.neighbors::add);
          }
        }
        stack.remove(effect);
        return Optional.of(node);
      }
    });
  }

  @Override
  public void commit() {
    while (!batch.isEmpty()) {
      var node = batch.removeFirst(); // remove the node with the smallest number of inbound dependencies
      graph.remove(node.effect);
      for (var neighbor : node.neighbors) {
        neighbor.inbound--;
        neighbor.updateSort(batch);
      }

      try {
        node.effect.run();
      } catch (Exception e) {
        logger.error("uncaught exception in effect", e);
      }
    }
  }

  private static class GraphNode {
    public final Effect effect;
    public int inbound;
    public Set<GraphNode> neighbors;

    public GraphNode(Effect effect) {
      this.effect = effect;
      this.inbound = 0;
      this.neighbors = new HashSet<>();
    }

    public void updateSort(SortedSet<GraphNode> batch) {
      if (batch.contains(this)) {
        batch.remove(this);
        batch.add(this);
      }
    }
  }
}
