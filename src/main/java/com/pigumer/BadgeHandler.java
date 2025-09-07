package com.pigumer;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.pigumer.database.OpenBadgesTable;
import com.pigumer.logic.Extract;
import com.pigumer.logic.Logic;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.ByteArrayInputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class BadgeHandler implements RequestHandler<BadgeHandler.Event, BadgeHandler.MetaText> {

    public record Bucket(String name) {}
    public record BucketObject(String key) {}
    public record S3EventInfo(Bucket bucket, BucketObject object) {}
    public record S3Event(S3EventInfo s3) {}
    public record Event(S3Event[] Records) {}

    public record MetaText(String bucket, String key, Collection<Map<String, String>> text) {}

    private static final S3Client s3Client = S3Client.builder().build();

    private String tableName = System.getenv("TABLE_NAME");

    private Map<String, Object> parseJson(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            HashMap<String, Object> map = new HashMap<>();
            JsonNode root = mapper.readTree(json);
            for (Iterator<String> it = root.fieldNames(); it.hasNext(); ) {
                String name = it.next();
                JsonNode node = root.get(name);
                if (node.getNodeType() == JsonNodeType.STRING) {
                    map.put(name, node.asText());
                } else if (node.getNodeType() == JsonNodeType.OBJECT) {
                    HashMap<String, String> jm = new HashMap<>();
                    for (Iterator<String> j = node.fieldNames(); j.hasNext(); ) {
                        String jn = j.next();
                        JsonNode n = node.get(jn);
                        jm.put(jn, n.asText());
                    }
                    map.put(name, jm);
                } else if (node.getNodeType() == JsonNodeType.ARRAY) {
                    ArrayList<String> array = new ArrayList<>();
                    // TODO: いずれ実装するかも
                    map.put(name, array);
                }
            }
            return map;
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    @Override
    public MetaText handleRequest(Event in, Context context) {
        S3Event s3 = in.Records[0];
        String bucketName = s3.s3.bucket.name;
        String key = URLDecoder.decode(s3.s3.object.key, StandardCharsets.UTF_8);
        context.getLogger().log(bucketName + "/" + key);
        GetObjectRequest req = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        ResponseInputStream<GetObjectResponse> res = s3Client.getObject(req);

        try (ByteArrayInputStream stream = new ByteArrayInputStream(res.readAllBytes())) {
            Logic logic = new Logic();
            Collection<Map<String, String>> text = logic.analyze(stream);
            System.out.println(text.toString());

            Map<String, String> openbadge = new Extract().extract(text);
            String json = openbadge.get("value");
            if (json != null) {
                Map<String, Object> map = parseJson(json);
                new OpenBadgesTable(tableName).put(key, map, text);
            }
            return new MetaText(bucketName, key, text);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
