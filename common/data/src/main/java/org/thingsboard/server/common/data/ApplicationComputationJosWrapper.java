package org.thingsboard.server.common.data;

import java.util.Collections;
import java.util.Set;

public class ApplicationComputationJosWrapper {
    private String applicationId;
    private Set<String> computationJobs = Collections.emptySet();

    public Set<String> getComputationJobs() {
        return computationJobs;
    }

    public void setComputationJobs(Set<String> computationJobs) {
        this.computationJobs = computationJobs;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }
}
