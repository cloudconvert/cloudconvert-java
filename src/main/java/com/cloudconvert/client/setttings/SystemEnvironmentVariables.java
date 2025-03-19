package com.cloudconvert.client.setttings;

public class SystemEnvironmentVariables implements EnvironmentVariables {
    @Override
    public String getenv(String name) {
        return System.getenv(name);
    }
} 