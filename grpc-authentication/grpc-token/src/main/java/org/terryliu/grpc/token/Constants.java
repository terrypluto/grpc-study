package org.terryliu.grpc.token;

import io.grpc.Context;
import io.grpc.Metadata;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

import java.security.Key;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

/**
 * @author Nyquist Data Tech Team
 * @version 1.0.0
 * @date 2023/4/13
 * @description todo
 */
public class Constants {
    public static final String BEARER_TYPE = "Bearer";

    public static final Metadata.Key<String> AUTHORIZATION_METADATA_KEY = Metadata.Key.of("Authorization", ASCII_STRING_MARSHALLER);
    public static final Context.Key<String> CLIENT_ID_CONTEXT_KEY = Context.key("clientId");

    private Constants() {
        throw new AssertionError();
    }

    public static Key signKey(){
        String encode = "dC8ziH16pGnQu7Sl6HwgFnQpBtE5YnnBSEBkGIs/uN8AYi87Mphnob2kvlQte5WenkFjzNJGaZMYdisBe74OrQ==";
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(encode));
    }
}
