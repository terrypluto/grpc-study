package org.terryliu.grpc.token.client;

import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Status;
import org.terryliu.grpc.token.Constants;

import java.util.concurrent.Executor;

/**
 * @author Nyquist Data Tech Team
 * @version 1.0.0
 * @date 2023/4/13
 * @description todo
 */
public class BearerToken extends CallCredentials {
    private String value;

    public BearerToken(String value) {
        this.value = value;
    }
    @Override
    public void applyRequestMetadata(RequestInfo requestInfo, Executor executor, MetadataApplier metadataApplier) {
        executor.execute(() -> {
            try {
                Metadata headers = new Metadata();
                headers.put(Constants.AUTHORIZATION_METADATA_KEY, String.format("%s %s", Constants.BEARER_TYPE, value));
                metadataApplier.apply(headers);
            } catch (Throwable e) {
                metadataApplier.fail(Status.UNAUTHENTICATED.withCause(e));
            }
        });
    }

    @Override
    public void thisUsesUnstableApi() {

    }
}
