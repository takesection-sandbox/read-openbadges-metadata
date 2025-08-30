package com.pigumer.database;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;

import java.util.HashMap;
import java.util.Map;

public class OpenBadgesTable {

    private DynamoDbClient client;

    private String tableName;

    public OpenBadgesTable(String tableName) {
        client = DynamoDbClient.builder().build();
        this.tableName = tableName;
    }

    public PutItemResponse put(String key, Map<String, Object> map) {
        String badge = (String) map.get("badge");
        String id = (String) map.get("id");
        String identity = (String) ((Map<String, String>) map.get("recipient")).get("identity");
        String issuedOn =  (String) map.get("issuedOn");
        String row = map.toString();
        HashMap<String, AttributeValue> itemMap = new HashMap<>();
        itemMap.put("badge", AttributeValue.builder().s(badge).build());
        itemMap.put("id", AttributeValue.builder().s(id).build());
        itemMap.put("issuedOn", AttributeValue.builder().s(issuedOn).build());
        itemMap.put("identity", AttributeValue.builder().s(identity).build());
        itemMap.put("row", AttributeValue.builder().s(row).build());
        itemMap.put("fileName", AttributeValue.builder().s(key).build());
        String expires = (String) map.get("expires");
        if (expires != null) {
            itemMap.put("expires", AttributeValue.builder().s(expires).build());
        }
        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(tableName)
                .item(itemMap)
                .build();
        PutItemResponse res = client.putItem(putItemRequest);
        System.out.println(res.toString());
        return res;
    }
}
