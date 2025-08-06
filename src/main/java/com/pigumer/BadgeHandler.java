package com.pigumer;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.pigumer.logic.Logic;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class BadgeHandler implements RequestHandler<BadgeHandler.BadgeObject, String> {

    public record BadgeObject(String bucketName, String key) {}

    private static final S3Client s3Client = S3Client.builder().build();

    @Override
    public String handleRequest(BadgeObject in, Context context) {
        context.getLogger().log(in.toString());
        GetObjectRequest req = GetObjectRequest.builder()
                .bucket(in.bucketName)
                .key(in.key)
                .build();
        ResponseInputStream<GetObjectResponse> res = s3Client.getObject(req);
        try (ByteArrayInputStream stream = new ByteArrayInputStream(res.readAllBytes())) {
            Logic logic = new Logic();
            return logic.analyze(stream);
        } catch (Exception e) {
            context.getLogger().log(e.getMessage());
            return e.getMessage();
        }
    }
}
