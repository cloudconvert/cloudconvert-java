package com.cloudconvert.processor.content;

import java.io.IOException;
import java.io.InputStream;

public class DataExtractingContentPreProcessor implements ContentPreProcessor {

    private static final String DATA = "data";

    private final DefaultContentPreProcessor defaultContentPreProcessor;

    public DataExtractingContentPreProcessor() {
        this.defaultContentPreProcessor = new DefaultContentPreProcessor();
    }

    @Override
    public String preProcess(final InputStream inputStream) throws IOException {
        final String rawString = defaultContentPreProcessor.preProcess(inputStream);

        // Get rid of "data" part
        return rawString.substring(8, rawString.length() - 1);
    }
}
