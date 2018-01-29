package org.thingsboard.server.dao.model.sql;

import com.datastax.driver.core.utils.UUIDs;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.TypeDef;
import org.thingsboard.server.common.data.Computations;
import org.thingsboard.server.common.data.id.ComputationsId;
import org.thingsboard.server.dao.model.BaseSqlEntity;
import org.thingsboard.server.dao.model.ModelConstants;
import org.thingsboard.server.dao.model.SearchTextEntity;
import org.thingsboard.server.dao.util.mapping.JsonStringType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@TypeDef(name = "json", typeClass = JsonStringType.class)
@Table(name = ModelConstants.COMPUTATIONS_TABLE_NAME)
public class ComputationsEntity extends BaseSqlEntity<Computations> implements SearchTextEntity<Computations> {
    @Transient
    private static final long serialVersionUID = -4873737406462009031L;

    @Column(name = ModelConstants.COMPUTATIONS_NAME)
    private String name;

    @Column(name = ModelConstants.COMPUTATIONS_JAR_PATH)
    private String jarPath;

    @Column(name = ModelConstants.COMPUTATIONS_ACTIONS)
    private String actions;

    @Column(name = ModelConstants.SEARCH_TEXT_PROPERTY)
    private String searchText;

    @Override
    public String getSearchTextSource() {
        return name;
    }

    public ComputationsEntity() {
        super();
    }

    public ComputationsEntity(Computations computations){
        if(computations.getId() != null) {
            this.setId(computations.getId().getId());
        }
        if(computations.getName() != null) {
            this.name = computations.getName();
        }
        if(computations.getJarPath() != null) {
            this.jarPath = computations.getJarPath();
        }
        if(computations.getJarPath() != null) {
            this.actions = computations.getActions();
        }
    }

    @Override
    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ComputationsEntity that = (ComputationsEntity) o;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (searchText != null ? !searchText.equals(that.searchText) : that.searchText != null) return false;
        if (jarPath != null ? !jarPath.equals(that.jarPath) : that.jarPath != null) return false;
        if (actions != null ? !actions.equals(that.actions) : that.actions != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (searchText != null ? searchText.hashCode() : 0);
        result = 31 * result + (jarPath != null ? jarPath.hashCode() : 0);
        result = 31 * result + (actions != null ? actions.hashCode() : 0);
        return result;
    }

    @Override
    public Computations toData() {
        Computations computations = new Computations(new ComputationsId(getId()));
        computations.setCreatedTime(UUIDs.unixTimestamp(getId()));
        computations.setName(name);
        computations.setJarPath(jarPath);
        computations.setActions(actions);
        return computations;
    }

}
