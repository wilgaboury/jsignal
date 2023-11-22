package com.github.wilgaboury.sigui;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NodeRegistry {
    private static final Logger logger = Logger.getLogger(NodeRegistry.class.getName());

    private final Map<String, MetaNode> byId;
    private final Map<String, Set<MetaNode>> byTag;

    public NodeRegistry() {
        byId = new HashMap<>();
        byTag = new HashMap<>();
    }

    void addNode(MetaNode node) {
        addNodeId(node);
        addNodeTags(node);
    }

    void addNodeId(MetaNode node) {
        if (node.getId() == null)
            return;

        if (byId.containsKey(node.getId())) {
            logger.log(Level.WARNING, String.format("cannot assign id \"%s\", already exists", node.getId()));
            return;
        }

        byId.put(node.getId(), node);
    }

    void addNodeTags(MetaNode node) {
        for (String tag : node.getTags()) {
            byTag.computeIfAbsent(tag, k -> new HashSet<>()).add(node);
        }
    }

    void removeNode(MetaNode node) {
        removeNodeId(node);
        removeNodeTags(node);
    }

    void removeNodeId(MetaNode node) {
        byId.remove(node.getId());
    }

    void removeNodeTags(MetaNode node) {
        for (String tag : node.getTags()) {
            byTag.get(tag).remove(node);
        }
    }

    public Map<String, MetaNode> getById() {
        return Collections.unmodifiableMap(byId);
    }

    public Map<String, Set<MetaNode>> getByTag() {
        return Collections.unmodifiableMap(byTag);
    }
}
