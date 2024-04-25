package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.Context;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public interface MetaNodeRegistry {
  Context<MetaNodeRegistry> context = new Context<>(new Null());

  void addById(Object id, MetaNode meta);
  void addByTag(Object tag, MetaNode meta);
  void removeById(Object id, MetaNode meta);
  void removeByTag(Object tag, MetaNode meta);
  Optional<MetaNode> getById(Object id);
  List<MetaNode> getByTag(Object tag);

  class Null implements MetaNodeRegistry {

    @Override
    public void addById(Object id, MetaNode meta) {}

    @Override
    public void addByTag(Object tag, MetaNode meta) {}

    @Override
    public void removeById(Object id, MetaNode meta) {}

    @Override
    public void removeByTag(Object tag, MetaNode meta) {}

    @Override
    public Optional<MetaNode> getById(Object id) {
      return Optional.empty();
    }

    @Override
    public List<MetaNode> getByTag(Object tag) {
      return List.of();
    }
  }

  class Root implements MetaNodeRegistry {
    private final Map<Object, MetaNode> byId;
    private final Map<Object, TreeSet<MetaNode>> byTag;

    public Root() {
      byId = new HashMap<>();
      byTag = new HashMap<>();
    }

    @Override
    public void addById(Object id, MetaNode meta) {
      byId.put(id, meta);
    }

    @Override
    public void addByTag(Object tag, MetaNode meta) {
      byTag.computeIfAbsent(tag, ignored ->
          new TreeSet<>(MetaNodeRegistry::compareByRenderOrder))
        .add(meta);
    }

    @Override
    public void removeById(Object id, MetaNode meta) {
      byTag.remove(id);
    }

    @Override
    public void removeByTag(Object tag, MetaNode meta) {
      Set<MetaNode> metas = byTag.get(tag);
      if (metas != null) {
        metas.remove(meta);
        if (metas.isEmpty()) {
          byTag.remove(tag);
        }
      }
    }

    @Override
    public Optional<MetaNode> getById(Object id) {
      return Optional.ofNullable(byId.get(id));
    }

    @Override
    public List<MetaNode> getByTag(Object tag) {
      return Optional.ofNullable(byTag.get(tag))
        .map(List::copyOf)
        .orElseGet(Collections::emptyList);
    }
  }

  class Child implements MetaNodeRegistry {
    private final MetaNodeRegistry parent;
    private final Root that;

    public Child(MetaNodeRegistry parent) {
      this.parent = parent;
      this.that = new Root();
    }

    @Override
    public void addById(Object id, MetaNode meta) {
      parent.addById(id, meta);
      that.addById(id, meta);
    }

    @Override
    public void addByTag(Object tag, MetaNode meta) {
      parent.addByTag(tag, meta);
      that.addByTag(tag, meta);
    }

    @Override
    public void removeById(Object id, MetaNode meta) {
      parent.removeById(id, meta);
      that.removeById(id, meta);
    }

    @Override
    public void removeByTag(Object tag, MetaNode meta) {
      parent.removeByTag(tag, meta);
      that.removeByTag(tag, meta);
    }

    @Override
    public Optional<MetaNode> getById(Object id) {
      return that.getById(id);
    }

    @Override
    public List<MetaNode> getByTag(Object tag) {
      return that.getByTag(tag);
    }
  }

  private static int compareByRenderOrder(MetaNode m1, MetaNode m2) {
    MetaNode ancestor = lowestCommonAncestor(m1, m2);
    if (ancestor == null) {
      return 0;
    }

    for (MetaNode child : ancestor.getChildren()) {
      if (m1 == child) {
        return -1;
      }
      if (m2 == child) {
        return 1;
      }
    }

    return 0;
  }

  private static @Nullable MetaNode lowestCommonAncestor(MetaNode m1, MetaNode m2) {
    int parents1 = countAncestors(m1);
    int parents2 = countAncestors(m2);
    MetaNode larger;
    MetaNode smaller;
    int parentsLarger;
    int parentsSmaller;
    if (parents1 > parents2) {
      larger = m1;
      parentsLarger = parents1;
      smaller = m2;
      parentsSmaller = parents2;
    } else {
      larger = m2;
      parentsLarger = parents2;
      smaller = m1;
      parentsSmaller = parents1;
    }

    int diff = parentsLarger - parentsSmaller;
    while (diff > 0) {
      larger = larger.getParent();
      diff--;
    }

    while (larger != smaller) {
      larger = larger.getParent();
      smaller = smaller.getParent();
    }
    return larger;
  }

  private static int countAncestors(MetaNode n) {
    int count = 0;
    while (n != null) {
      n = n.getParent();
    }
    return count;
  }
}
