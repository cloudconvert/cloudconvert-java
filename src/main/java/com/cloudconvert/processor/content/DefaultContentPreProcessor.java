package com.cloudconvert.processor.content;

import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;

import java.io.IOException;
import java.io.InputStream;

public class DefaultContentPreProcessor implements ContentPreProcessor {

    @Override
    public String preProcess(final InputStream inputStream) throws IOException {
        final ByteSource byteSource = new ByteSource() {
            @Override
            public InputStream openStream() {
                return inputStream;
            }
        };

        return byteSource.asCharSource(Charsets.UTF_8).read();
    }
}
