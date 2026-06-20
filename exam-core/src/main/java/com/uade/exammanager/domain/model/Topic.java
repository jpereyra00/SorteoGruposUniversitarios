package com.uade.exammanager.domain.model;

import java.util.Objects;

public class Topic {

    private Long id;
    private String name;
    private Long unitId;

    public Topic() {}

    public Topic(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Topic(Long id, String name, Long unitId) {
        this.id = id;
        this.name = name;
        this.unitId = unitId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getUnitId() { return unitId; }
    public void setUnitId(Long unitId) { this.unitId = unitId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Topic t = (Topic) o;
        return id != null && Objects.equals(id, t.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }

    @Override
    public String toString() {
        return "Topic{id=" + id + ", name='" + name + "', unitId=" + unitId + "}";
    }
}
