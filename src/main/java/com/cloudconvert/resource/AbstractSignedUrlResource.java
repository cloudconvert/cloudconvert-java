package com.cloudconvert.resource;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.TaskRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.jetbrains.annotations.NotNull;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;


public abstract class AbstractSignedUrlResource extends AbstractResource {

    public static final String HMAC_SHA256 = "HmacSHA256";


    public AbstractSignedUrlResource(
            final SettingsProvider settingsProvider, final ObjectMapperProvider objectMapperProvider
    ) {
        super(settingsProvider, objectMapperProvider);


    }


    public String sign(
            @NotNull final String base,
            @NotNull final String signingSecret,
            @NotNull final Map<String, TaskRequest> tasks,
            String cacheKey
    ) throws InvalidKeyException, NoSuchAlgorithmException, JsonProcessingException {

        String url = base;

        String jobJson = getJson(ImmutableMap.of("tasks", tasks));

        String base64Job = Base64.encodeBase64URLSafeString(jobJson.getBytes(StandardCharsets.UTF_8));

        url = url.concat("?job=").concat(base64Job);

        if (cacheKey != null) {
            url = url.concat("&cache_key=").concat(cacheKey);
        }

        final Mac mac = Mac.getInstance(HMAC_SHA256);
        final SecretKeySpec secretKeySpec = new SecretKeySpec(signingSecret.getBytes(), HMAC_SHA256);
        mac.init(secretKeySpec);

        url = url.concat("&s=").concat(Hex.encodeHexString(mac.doFinal(url.getBytes())));

        return url;
    }


    public String sign(
            @NotNull final String base,
            @NotNull final String signingSecret,
            @NotNull final Map<String, TaskRequest> tasks
    ) throws InvalidKeyException, NoSuchAlgorithmException, JsonProcessingException {
        return this.sign(base, signingSecret, tasks, null);
    }

}
