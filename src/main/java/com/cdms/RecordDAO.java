package com.cdms;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class RecordDAO {
    public List<Object[]> list(EntityType type, String search) throws Exception {
        return list(type, search, "", "", "", "");
    }

    public List<Object[]> list(EntityType type, String search, String category, String status,
                               String fromDate, String toDate) throws Exception {
        StringBuilder sql = new StringBuilder("SELECT ").append(type.primaryKey());
        for (EntityType.Column column : type.columns()) sql.append(',').append(column.name());
        sql.append(" FROM ").append(type.table());
        List<Object> parameters = new ArrayList<>();
        List<String> conditions = new ArrayList<>();
        if (search != null && !search.isBlank()) {
            StringBuilder searchCondition = new StringBuilder("(");
            for (int i = 0; i < type.columns().size(); i++) {
                if (i > 0) searchCondition.append(" OR ");
                searchCondition.append(type.columns().get(i).name()).append(" LIKE ?");
                parameters.add("%" + search.trim() + "%");
            }
            conditions.add(searchCondition.append(")").toString());
        }
        if (type == EntityType.CASES) {
            if (category != null && !category.isBlank()) {
                conditions.add("crime_type=?");
                parameters.add(category.trim());
            }
            if (status != null && !status.isBlank()) {
                conditions.add("status=?");
                parameters.add(status.trim());
            }
            if (fromDate != null && !fromDate.isBlank()) {
                conditions.add("incident_date>=?");
                parameters.add(Date.valueOf(LocalDate.parse(fromDate.trim())));
            }
            if (toDate != null && !toDate.isBlank()) {
                conditions.add("incident_date<=?");
                parameters.add(Date.valueOf(LocalDate.parse(toDate.trim())));
            }
        }
        if (!conditions.isEmpty()) sql.append(" WHERE ").append(String.join(" AND ", conditions));
        sql.append(" ORDER BY ").append(type.primaryKey()).append(" DESC");
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < parameters.size(); i++) statement.setObject(i + 1, parameters.get(i));
            try (ResultSet result = statement.executeQuery()) {
                List<Object[]> records = new ArrayList<>();
                while (result.next()) {
                    Object[] row = new Object[type.columns().size() + 1];
                    row[0] = result.getObject(1);
                    for (int i = 0; i < type.columns().size(); i++) row[i + 1] = result.getObject(i + 2);
                    records.add(row);
                }
                return records;
            }
        }
    }

    public void save(EntityType type, Integer id, List<String> values) throws Exception {
        if (values.size() != type.columns().size()) throw new IllegalArgumentException("Invalid form data.");
        List<Object> parsed = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            EntityType.Column column = type.columns().get(i);
            String value = values.get(i) == null ? "" : values.get(i).trim();
            if (column.required() && value.isBlank()) throw new IllegalArgumentException(column.label() + " is required.");
            if (column.name().equals("status") && !value.isBlank()) {
                List<String> allowed = type == EntityType.CRIMINALS
                        ? List.of("WANTED", "ARRESTED", "RELEASED", "UNDER_SURVEILLANCE")
                        : type == EntityType.CASES
                        ? List.of("OPEN", "UNDER_INVESTIGATION", "SOLVED", "CLOSED") : List.of();
                if (!allowed.isEmpty() && !allowed.contains(value.toUpperCase()))
                    throw new IllegalArgumentException("Select a valid " + type.label().toLowerCase() + " status.");
            }
            if (column.name().equals("email") && !value.isBlank() && !value.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
                throw new IllegalArgumentException("Enter a valid email address.");
            parsed.add(parse(column, value));
        }
        String sql;
        if (id == null) {
            String placeholders = String.join(",", java.util.Collections.nCopies(type.columns().size(), "?"));
            sql = "INSERT INTO " + type.table() + " (" + String.join(",", type.columns().stream().map(EntityType.Column::name).toList())
                    + ") VALUES (" + placeholders + ")";
        } else {
            String assignments = String.join(",", type.columns().stream().map(c -> c.name() + "=?").toList());
            sql = "UPDATE " + type.table() + " SET " + assignments + " WHERE " + type.primaryKey() + "=?";
        }
        try (Connection connection = Database.connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < parsed.size(); i++) statement.setObject(i + 1, parsed.get(i));
            if (id != null) statement.setInt(parsed.size() + 1, id);
            statement.executeUpdate();
        }
    }

    private Object parse(EntityType.Column column, String value) {
        if (value.isBlank()) return null;
        try {
            return switch (column.kind()) {
                case INTEGER -> Integer.valueOf(value);
                case DATE -> Date.valueOf(LocalDate.parse(value));
                case DATETIME -> Timestamp.valueOf(LocalDateTime.parse(value.replace(' ', 'T')));
                default -> value;
            };
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Invalid value for " + column.label() + ".", exception);
        }
    }

    public void delete(EntityType type, int id) throws Exception {
        String sql = "DELETE FROM " + type.table() + " WHERE " + type.primaryKey() + "=?";
        try (Connection connection = Database.connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            if (statement.executeUpdate() == 0) throw new IllegalStateException("The selected record no longer exists.");
        }
    }
}
