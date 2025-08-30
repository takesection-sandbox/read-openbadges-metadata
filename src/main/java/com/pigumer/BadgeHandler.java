package com.pigumer;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.pigumer.logic.Extract;
import com.pigumer.logic.Logic;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Map;

public class BadgeHandler implements RequestHandler<BadgeHandler.Event, BadgeHandler.MetaText> {

    public record Bucket(String name) {}
    public record BucketObject(String key) {}
    public record S3EventInfo(Bucket bucket, BucketObject object) {}
    public record S3Event(S3EventInfo s3) {}
    public record Event(S3Event[] Records) {}

    public record MetaText(String bucket, String key, Collection<Map<String, String>> text) {}

    private static final S3Client s3Client = S3Client.builder().build();

    @Override
    public MetaText handleRequest(Event in, Context context) {
        S3Event s3 = in.Records[0];
        String bucketName = s3.s3.bucket.name;
        String key = s3.s3.object.key;
        context.getLogger().log(bucketName + "/" + key);
        GetObjectRequest req = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        ResponseInputStream<GetObjectResponse> res = s3Client.getObject(req);

        try (ByteArrayInputStream stream = new ByteArrayInputStream(res.readAllBytes())) {
            Logic logic = new Logic();
            Collection<Map<String, String>> text = logic.analyze(stream);

            Map<String, String> openbadge = new Extract().extract(text);
            String json = openbadge.get("value");
            if (json != null) {
                context.getLogger().log(json);
            }
            return new MetaText(bucketName, key, text);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
