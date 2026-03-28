package com.expense.split.service;

import com.expense.split.model.Group;
import com.expense.split.model.User;
import com.expense.split.repository.GroupRepository;
import java.util.List;

public class GroupService {
    private final GroupRepository groupRepository;

    public GroupService() {
        this.groupRepository = new GroupRepository();
    }

    public synchronized Group createGroup(String name) {
        Group group = new Group(name);
        return groupRepository.save(group);
    }

    public synchronized boolean addMemberToGroup(long groupId, User user) {
        Group group = groupRepository.getGroupById(groupId);
        if (group != null) {
            group.addMember(user);
            return true;
        }
        return false;
    }

    public synchronized List<Group> listGroupsForUser(User user) {
        return groupRepository.getAllGroups().stream()
                .filter(g -> g.getMembers().contains(user))
                .toList();
    }

    public synchronized Group getGroupById(long id) {
        return groupRepository.getGroupById(id);
    }

    public List<Group> getAllGroups() {
        return groupRepository.getAllGroups();
    }
}
