package com.tcli.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tcli.parser.TikaParser.ParseResult;

public class JsonFormatter {

    private final boolean includeMetadata;
    private final ObjectMapper mapper;

    public JsonFormatter(boolean includeMetadata) {
        this.includeMetadata = includeMetadata;
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public String format(ParseResult result) throws Exception {
        ObjectNode node = toJsonNode(result);
        return mapper.writeValueAsString(node);
    }

    private ObjectNode toJsonNode(ParseResult result) {
        ObjectNode node = mapper.createObjectNode();
        node.put("file", result.getName());

        if (result.getMimeType() != null) {
            node.put("mimeType", result.getMimeType());
        }

        if (includeMetadata && !result.getMetadata().isEmpty()) {
            ObjectNode metaNode = mapper.createObjectNode();
            result.getMetadata().forEach(metaNode::put);
            node.set("metadata", metaNode);
        }

        node.put("text", result.getText());

        if (!result.getChildren().isEmpty()) {
            ArrayNode childrenNode = mapper.createArrayNode();
            for (ParseResult child : result.getChildren()) {
                childrenNode.add(toJsonNode(child));
            }
            node.set("children", childrenNode);
        }

        return node;
    }
}
