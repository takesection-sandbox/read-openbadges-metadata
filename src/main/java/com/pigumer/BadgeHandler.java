package com.pigumer;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.pigumer.logic.Logic;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Map;

public class BadgeHandler implements RequestHandler<BadgeHandler.Event, Collection<Map<String, String>>> {

    public record Bucket(String name) {}
    public record BucketObject(String key) {}
    public record S3EventInfo(Bucket bucket, BucketObject object) {}
    public record S3Event(S3EventInfo s3) {}
    public record Event(S3Event[] Records) {}

    private static final S3Client s3Client = S3Client.builder().build();

    @Override
    public Collection<Map<String, String>> handleRequest(Event in, Context context) {
        context.getLogger().log(in.toString());
        S3Event s3 = in.Records[0];
        GetObjectRequest req = GetObjectRequest.builder()
                .bucket(s3.s3.bucket.name)
                .key(s3.s3.object.key)
                .build();
        ResponseInputStream<GetObjectResponse> res = s3Client.getObject(req);

        try (ByteArrayInputStream stream = new ByteArrayInputStream(res.readAllBytes())) {
            Logic logic = new Logic();
            return logic.analyze(stream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
