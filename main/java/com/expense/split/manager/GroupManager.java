package com.expense.split.manager;

import com.expense.split.design.Color;
import com.expense.split.model.Group;
import com.expense.split.model.User;
import com.expense.split.service.GroupService;
import java.util.List;

public class GroupManager {
    private final GroupService groupService;

    public GroupManager() {
        this.groupService = new GroupService();
    }

    public synchronized Group createGroup(String name, User creator) {
        try {
            Group group = groupService.createGroup(name);
            if (group != null) {
                group.addMember(creator);
                return group;
            }
        } catch (Exception e) {
            System.err.println(Color.RED + "[ERROR]: " + e.getMessage() + Color.RESET);
        }
        return null;
    }

    public synchronized boolean addMember(long groupId, User user) {
        return groupService.addMemberToGroup(groupId, user);
    }

    public synchronized List<Group> getGroupsForUser(User user) {
        return groupService.listGroupsForUser(user);
    }

    public synchronized Group getGroupById(long id) {
        return groupService.getGroupById(id);
    }
}
