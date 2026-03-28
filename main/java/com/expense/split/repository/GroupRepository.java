package com.expense.split.repository;

import com.expense.split.model.Group;
import java.util.ArrayList;
import java.util.List;

public class GroupRepository {

    private static final List<Group> groups = new ArrayList<>(); // in-memory storage

    public Group save(Group group) {
        if (!groups.contains(group)) {
            groups.add(group);
        }
        return group;
    }

    public Group getGroupById(long id) {
        return groups.stream()
                .filter(g -> g.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public List<Group> getAllGroups() {
        return new ArrayList<>(groups);
    }

    public boolean delete(Group group) {
        return groups.remove(group);
    }

    public static List<Group> getGroups() {
        return groups;
    }
}
