package com.uade.exammanager.domain.model;

import java.util.Objects;

public class Unit {

    private Long id;
    private String name;
    private String description;
    private int order;

    public Unit() {}

    public Unit(Long id, String name, String description, int order) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.order = order;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Unit u = (Unit) o;
        return id != null && Objects.equals(id, u.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }

    @Override
    public String toString() {
        return "Unit{id=" + id + ", name='" + name + "', order=" + order + "}";
    }
}
