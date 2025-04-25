package org.jsignal.rx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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

  public Optional<GraphNode> dfs(Set<Effect> stack, EffectRef ref) {
    return ref.getEffect().flatMap(effect -> {
      if (stack.contains(effect)) {
        // cycle detected
        return Optional.empty();
      } else if (graph.containsKey(effect)) {
        var node = graph.get(effect);
        node.inbound++;
        node.updateSort(batch);
      } else {
        var node = new GraphNode(effect);
        graph.put(effect, node);
        for (Signal<?> signal : effect.getOutbound()) {
          for (EffectRef neighbor : signal.effects()) {
            dfs(neighbor);
          }
        }
      }
    });
  }

  @Override
  public void commit() {
    while (!batch.isEmpty()) {
      var node = batch.removeFirst();

      try {
        node.effect.run();
      } catch (Exception e) {
        logger.error("uncaught exception in effect");
      }
    }
  }

  private static class GraphNode {
    public final Effect effect;
    public int inbound;
    public Set<Effect> neighbors;

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
