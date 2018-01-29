package org.thingsboard.server.common.data;

import org.thingsboard.server.common.data.id.ComputationsId;

public class Computations extends SearchTextBased<ComputationsId> implements HasName {

    private String name;
    private String jarPath;

    private String actions;

    public Computations() {
        super();
    }

    public Computations(ComputationsId id) {
        super(id);
    }

    public Computations(Computations computations) {
        super(computations);
        this.name = computations.name;
        this.jarPath = computations.jarPath;
        this.actions = computations.actions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Computations that = (Computations) o;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (jarPath != null ? !jarPath.equals(that.jarPath) : that.jarPath != null) return false;
        if (actions != null ? !actions.equals(that.actions) : that.actions != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (jarPath != null ? jarPath.hashCode() : 0);
        result = 31 * result + (actions != null ? actions.hashCode() : 0);
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

    public String getActions() {
        return actions;
    }

    public void setActions(String actions) {
        this.actions = actions;
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
