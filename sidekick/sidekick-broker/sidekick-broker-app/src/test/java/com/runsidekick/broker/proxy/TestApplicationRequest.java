package com.runsidekick.broker.proxy;

import java.util.List;
import java.util.Objects;

public class TestApplicationRequest {

    private String client;

    public TestApplicationRequest() {
    }

    public TestApplicationRequest(String client) {
        this.client = client;
    }

    public String getName() {
        return "Test";
    }

    public String getClient() {
        return client;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestApplicationRequest that = (TestApplicationRequest) o;
        return Objects.equals(client, that.client);
    }

    @Override
    public int hashCode() {
        return Objects.hash(client);
    }

    @Override
    public String toString() {
        return "TestApplicationRequest{" +
                "client=" + client +
                '}';
    }

}
