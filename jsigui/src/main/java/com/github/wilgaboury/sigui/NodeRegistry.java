package com.github.wilgaboury.sigui;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NodeRegistry {
    private static final Logger logger = Logger.getLogger(NodeRegistry.class.getName());

    private final Map<MetaNode, String> nodeToId;
    private final Map<String, MetaNode> idToNode;
    private final Map<MetaNode, Set<String>> nodeToTags;
    private final Map<String, Set<MetaNode>> tagToNodes;

    public NodeRegistry() {
        nodeToId = new HashMap<>();
        idToNode = new HashMap<>();
        nodeToTags = new HashMap<>();
        tagToNodes = new HashMap<>();
    }

    public void setId(MetaNode node, String id) {
        if (nodeToId.containsKey(node)) {
            logger.log(Level.WARNING, String.format("cannot assign id \"%s\" to node, already has id \"%s\"", id, nodeToId.get(node)));
            return;
        }

        if (idToNode.containsKey(id)) {
            logger.log(Level.WARNING, String.format("cannot assign id \"%s\", already exists", id));
            return;
        }

        nodeToId.put(node, id);
        idToNode.put(id, node);
    }

    public void setTags(MetaNode node, Collection<String> tags) {
        if (nodeToId.containsKey(node)) {
            logger.log(Level.WARNING, "cannot assign tags to, already exists");
            return;
        }

        nodeToTags.put(node, new HashSet<>(tags));
        for (String tag : tags) {
            tagToNodes.computeIfAbsent(tag, k -> new HashSet<>()).add(node);
        }
    }

    public void removeNode(MetaNode node) {
        idToNode.remove(nodeToId.remove(node));
        var tags = nodeToTags.remove(node);
        if (tags != null) {
            for (String tag : tags) {
                tagToNodes.get(tag).remove(node);
            }
        }
    }

    public Map<MetaNode, String> getNodeToId() {
        return nodeToId;
    }

    public Map<String, MetaNode> getIdToNode() {
        return idToNode;
    }

    public Map<MetaNode, Set<String>> getNodeToTags() {
        return nodeToTags;
    }

    public Map<String, Set<MetaNode>> getTagToNodes() {
        return tagToNodes;
    }
}
