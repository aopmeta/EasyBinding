package com.aopmeta.sample.model;


import java.util.List;

public class UserProfile {
    public final String id;
    public final String name;
    public final int avatar;

    public final List<String> groupIds;

    public UserProfile(String id, String name, int avatar, List<String> groupIds) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.groupIds = groupIds;
    }
}
