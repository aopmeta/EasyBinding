package com.aopmeta.sample.model;

public class GroupProfile {
    public final String id;
    public final String name;
    public final String parentId;

    public GroupProfile(String id, String name, String parentId) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
    }
}
