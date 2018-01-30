package org.thingsboard.server.common.data.computation;

import com.fasterxml.jackson.databind.JsonNode;
import org.thingsboard.server.common.data.HasName;
import org.thingsboard.server.common.data.SearchTextBased;
import org.thingsboard.server.common.data.id.ComputationId;
import org.thingsboard.server.common.data.id.TenantId;

public class Computations extends SearchTextBased<ComputationId> implements HasName {

    private String name;
    private String jarPath;
    private TenantId tenantId;
    private String computationName;
    private String mainClass;
    private JsonNode jsonDescriptor;
    private String argsformat;

    public Computations() {
        super();
    }

    public Computations(ComputationId id) {
        super(id);
    }

    public Computations(Computations computations) {
        super(computations);
        this.name = computations.name;
        this.jarPath = computations.jarPath;
        this.computationName = computations.computationName;
        this.tenantId = computations.tenantId;
        this.argsformat = computations.argsformat;
        this.jsonDescriptor = computations.jsonDescriptor;
        this.mainClass = computations.mainClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Computations that = (Computations) o;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (jarPath != null ? !jarPath.equals(that.jarPath) : that.jarPath != null) return false;
        if (computationName != null ? !computationName.equals(that.computationName) : that.computationName != null) return false;
        if (tenantId != null ? !tenantId.equals(that.tenantId) : that.tenantId != null) return false;
        if (argsformat != null ? !argsformat.equals(that.argsformat) : that.argsformat != null) return false;
        if (jsonDescriptor != null ? !jsonDescriptor.equals(that.jsonDescriptor) : that.jsonDescriptor != null) return false;
        if (mainClass != null ? !mainClass.equals(that.mainClass) : that.mainClass != null) return false;

        return true;
    }

    @Override

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (jarPath != null ? jarPath.hashCode() : 0);
        result = 31 * result + (computationName != null ? computationName.hashCode() : 0);
        result = 31 * result + (tenantId != null ? tenantId.hashCode() : 0);
        result = 31 * result + (argsformat != null ? argsformat.hashCode() : 0);
        result = 31 * result + (jsonDescriptor != null ? jsonDescriptor.hashCode() : 0);
        result = 31 * result + (mainClass != null ? mainClass.hashCode() : 0);
        return result;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJarPath() {
        return jarPath;
    }

    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }

    public String getComputationName() {
        return computationName;
    }

    public void setComputationName(String computationName) {
        this.computationName = computationName;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public void setTenantId(TenantId tenantId) {
        this.tenantId = tenantId;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public JsonNode getJsonDescriptor() {
        return jsonDescriptor;
    }

    public void setJsonDescriptor(JsonNode jsonDescriptor) {
        this.jsonDescriptor = jsonDescriptor;
    }

    public String getArgsformat() {
        return argsformat;
    }

    public void setArgsformat(String argsformat) {
        this.argsformat = argsformat;
    }

    @Override
    public String getName() {
        return name;
    }
    @Override
    public String getSearchText() {
        return name;
    }
}
