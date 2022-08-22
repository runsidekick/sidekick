package com.runsidekick.broker.proxy;

import java.util.List;
import java.util.Objects;

public class TestRequest {

    private List<String> applications;

    public TestRequest() {
    }

    public TestRequest(List<String> applications) {
        this.applications = applications;
    }

    public String getName() {
        return "Test";
    }

    public List<String> getApplications() {
        return applications;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestRequest that = (TestRequest) o;
        return Objects.equals(applications, that.applications);
    }

    @Override
    public int hashCode() {
        return Objects.hash(applications);
    }

    @Override
    public String toString() {
        return "TestRequest{" +
                "applications=" + applications +
                '}';
    }

}
