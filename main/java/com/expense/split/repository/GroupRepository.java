package com.expense.split.repository;

import com.expense.split.db.DbConnection;
import com.expense.split.model.Group;
import com.expense.split.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GroupRepository {

    private static final List<Group> groups = new ArrayList<>(); // in-memory storage

    // save the group list in the database
    public static void upload() throws SQLException {
        if (groups.isEmpty()) {
            System.out.println("group list is empty. nothing to upload.");
            return;
        }

        String groupQuery = """
                INSERT INTO groups_table (
                    group_id,
                    group_name
                )
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE
                    group_name = VALUES(group_name)
                """;

        String memberQuery = """
                INSERT INTO group_members (
                    group_id,
                    user_id
                )
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE
                    user_id = VALUES(user_id)
                """;

        try (
                Connection connection = DbConnection.getConnection();
                PreparedStatement groupPS = connection.prepareStatement(groupQuery);
                PreparedStatement memberPS = connection.prepareStatement(memberQuery)
        ) {
            for (Group group : groups) {

                groupPS.setLong(1, group.getId());
                groupPS.setString(2, group.getName());
                groupPS.addBatch();

                for (User user : group.getMembers()) {
                    memberPS.setLong(1, group.getId());
                    memberPS.setLong(2, user.getId());
                    memberPS.addBatch();
                }
            }

            groupPS.executeBatch();
            memberPS.executeBatch();

            System.out.println("[STATUS]: all groups uploaded successfully.");

        }
    }

    // fetch all records from the database and load it in the lists.
    public static void download() throws SQLException {
        groups.clear();

        String groupQuery = """
                SELECT * FROM groups_table
                """;

        String memberQuery = """
                SELECT * FROM group_members
                WHERE group_id = ?
                """;

        long maxId = 0;

        try (
                Connection connection = DbConnection.getConnection();
                PreparedStatement groupPS = connection.prepareStatement(groupQuery);
                ResultSet groupRS = groupPS.executeQuery()
        ) {
            while (groupRS.next()) {

                long groupId = groupRS.getLong("group_id");
                String groupName = groupRS.getString("group_name");

                Group group = new Group(groupId, groupName);

                try (
                        PreparedStatement memberPS = connection.prepareStatement(memberQuery)
                ) {
                    memberPS.setLong(1, groupId);

                    try (ResultSet memberRS = memberPS.executeQuery()) {

                        while (memberRS.next()) {
                            long userId = memberRS.getLong("user_id");

                            User user = new UserRepository().getUserById(userId);

                            if (user != null) {
                                group.addMember(user);
                            }
                        }
                    }
                }

                groups.add(group);

                if (groupId > maxId) {
                    maxId = groupId;
                }
            }

        }

        Group.setGroupCount(maxId);

        System.out.println("[STATUS]: all groups downloaded successfully.");
    }

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
