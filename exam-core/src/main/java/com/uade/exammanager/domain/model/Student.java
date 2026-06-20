package com.uade.exammanager.domain.model;

import java.util.Objects;

public class Student {

    private Long id;
    private String fullName;
    private String legajo;

    public Student() {}

    public Student(Long id, String fullName, String legajo) {
        this.id = id;
        this.fullName = fullName;
        this.legajo = legajo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getLegajo() { return legajo; }
    public void setLegajo(String legajo) { this.legajo = legajo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student s = (Student) o;
        return id != null && Objects.equals(id, s.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }

    @Override
    public String toString() {
        return "Student{id=" + id + ", fullName='" + fullName + "', legajo='" + legajo + "'}";
    }
}
