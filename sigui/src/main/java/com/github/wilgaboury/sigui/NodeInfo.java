package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.Provider;

public class NodeInfo {
    private final Object id;
    private final Object[] tags;
    private final Provider data;

    public NodeInfo(Builder builder) {
        this.id = builder.id;
        this.tags = builder.tags;
        this.data = builder.data;
    }

    public Object getId() {
        return id;
    }

    public Object[] getTags() {
        return tags;
    }

    public Provider getData() {
        return data;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Object id;
        private Object[] tags;
        private Provider data;

        public Builder setId(Object id) {
            this.id = id;
            return this;
        }

        public Builder setTags(Object[] tags) {
            this.tags = tags;
            return this;
        }

        public Builder setData(Provider data) {
            this.data = data;
            return this;
        }

        public NodeInfo build() {
            return new NodeInfo(this);
        }
    }
}
