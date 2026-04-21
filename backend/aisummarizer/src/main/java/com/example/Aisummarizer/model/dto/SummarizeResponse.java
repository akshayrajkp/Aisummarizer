package com.example.Aisummarizer.model.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record SummarizeResponse(
    PointwiseView     pointwise,
    HierarchicalView  hierarchical,
    VisualView        visual
) {
    public record PointwiseView(List<String> points) {
        @JsonCreator
        public PointwiseView(@JsonProperty("points") List<String> points) {
            this.points = points;
        }
    }

    public record HierarchicalView(List<Section> sections) {
        @JsonCreator
        public HierarchicalView(@JsonProperty("sections") List<Section> sections) {
            this.sections = sections;
        }

        public record Section(String title, List<String> points) {
            @JsonCreator
            public Section(
                @JsonProperty("title")  String title,
                @JsonProperty("points") List<String> points
            ) {
                this.title  = title;
                this.points = points;
            }
        }
    }

    public record VisualView(String center, List<Node> nodes) {
        @JsonCreator
        public VisualView(
            @JsonProperty("center") String center,
            @JsonProperty("nodes")  List<Node> nodes
        ) {
            this.center = center;
            this.nodes  = nodes;
        }

        public record Node(String label, String description) {
            @JsonCreator
            public Node(
                @JsonProperty("label")       String label,
                @JsonProperty("description") String description
            ) {
                this.label       = label;
                this.description = description;
            }
        }
    }
}
