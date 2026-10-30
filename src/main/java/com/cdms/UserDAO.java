package com.cdms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public final class UserDAO {
    public UserSession authenticate(String username, String password) throws Exception {
        String sql = "SELECT user_id,username,password_hash,role,officer_id FROM users WHERE username=?";
        try (Connection connection = Database.connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username.trim());
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next() || !PasswordUtil.verify(password, result.getString("password_hash"))) return null;
                int officer = result.getInt("officer_id");
                return new UserSession(result.getInt("user_id"), result.getString("username"), result.getString("role"),
                        result.wasNull() ? null : officer);
            }
        }
    }

    public void changePassword(UserSession session, String oldPassword, String newPassword) throws Exception {
        try (Connection connection = Database.connect();
             PreparedStatement query = connection.prepareStatement("SELECT password_hash FROM users WHERE user_id=?")) {
            query.setInt(1, session.userId());
            try (ResultSet result = query.executeQuery()) {
                if (!result.next() || !PasswordUtil.verify(oldPassword, result.getString(1)))
                    throw new IllegalArgumentException("Current password is incorrect.");
            }
        }
        try (Connection connection = Database.connect();
             PreparedStatement update = connection.prepareStatement("UPDATE users SET password_hash=? WHERE user_id=?")) {
            update.setString(1, PasswordUtil.hash(newPassword));
            update.setInt(2, session.userId());
            update.executeUpdate();
        }
    }

    public List<Object[]> listUsers() throws Exception {
        String sql = "SELECT u.user_id,u.username,u.role,u.officer_id,o.full_name FROM users u LEFT JOIN police_officers o ON u.officer_id=o.officer_id ORDER BY u.username";
        try (Connection connection = Database.connect(); PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            List<Object[]> users = new ArrayList<>();
            while (result.next()) users.add(new Object[]{result.getInt(1), result.getString(2), result.getString(3),
                    result.getObject(4), result.getString(5)});
            return users;
        }
    }

    public void createUser(String username, String password, String role, Integer officerId) throws Exception {
        if (username == null || !username.matches("[A-Za-z0-9._-]{3,60}"))
            throw new IllegalArgumentException("Username must be 3-60 characters (letters, numbers, ., _ or -).");
        validatePassword(password);
        if (!"ADMIN".equals(role) && !"OFFICER".equals(role)) throw new IllegalArgumentException("Select a valid role.");
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO users (username,password_hash,role,officer_id) VALUES (?,?,?,?)")) {
            statement.setString(1, username);
            statement.setString(2, PasswordUtil.hash(password));
            statement.setString(3, role);
            if (officerId == null) statement.setNull(4, java.sql.Types.INTEGER); else statement.setInt(4, officerId);
            statement.executeUpdate();
        }
    }

    public void deleteUser(int id, int currentUserId) throws Exception {
        if (id == currentUserId) throw new IllegalArgumentException("You cannot delete your own account.");
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE user_id=?")) {
            statement.setInt(1, id);
            if (statement.executeUpdate() == 0) throw new IllegalStateException("The user no longer exists.");
        }
    }

    public static void validatePassword(String password) {
        if (password == null || password.length() < 10 || password.length() > 128
                || password.chars().noneMatch(Character::isLetter)
                || password.chars().noneMatch(Character::isDigit)) {
            throw new IllegalArgumentException("Password must be 10-128 characters and contain a letter and a number.");
        }
    }
}
