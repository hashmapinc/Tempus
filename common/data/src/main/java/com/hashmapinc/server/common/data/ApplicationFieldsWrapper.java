package com.hashmapinc.server.common.data;

import java.util.Set;

public class ApplicationFieldsWrapper {
    private String applicationId;
    private Set<String> fields;

    public Set<String> getFields() {
        return fields;
    }

    public void setFields(Set<String> fields) {
        this.fields = fields;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

}
