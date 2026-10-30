package com.cdms;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public final class Database {
    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/cdms?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private Database() {}

    public static Connection connect() throws Exception {
        String url = System.getenv().getOrDefault("CDMS_DB_URL", DEFAULT_URL);
        String user = System.getenv().getOrDefault("CDMS_DB_USER", "root");
        String password = System.getenv().getOrDefault("CDMS_DB_PASSWORD", "");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException exception) {
            throw new SQLException("MySQL Connector/J is missing from the runtime classpath. "
                    + "Build with 'mvn clean package' and launch the packaged CDMS JAR.", exception);
        }
        return DriverManager.getConnection(url, user, password);
    }

    public static void initialize() throws Exception {
        String schema;
        try (var stream = Database.class.getResourceAsStream("/schema.sql")) {
            if (stream == null) throw new IllegalStateException("Database schema resource /schema.sql is missing.");
            try (var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                schema = reader.lines().collect(Collectors.joining("\n"));
            }
        }
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            for (String sql : schema.split(";")) {
                if (!sql.isBlank()) statement.execute(sql.trim());
            }
        }
        seedInitialData();
    }

    private static void seedInitialData() throws Exception {
        try (Connection connection = connect();
             PreparedStatement query = connection.prepareStatement("SELECT COUNT(*) FROM users");
             ResultSet result = query.executeQuery()) {
            result.next();
            if (result.getInt(1) > 0) return;
        }
        try (Connection connection = connect()) {
            connection.setAutoCommit(false);
            try {
                int officerId;
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO police_officers (badge_number,full_name,rank_title,department,phone,email) VALUES (?,?,?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    insert.setString(1, "CDMS-001");
                    insert.setString(2, "System Administrator");
                    insert.setString(3, "Inspector");
                    insert.setString(4, "Administration");
                    insert.setString(5, "");
                    insert.setString(6, "");
                    insert.executeUpdate();
                    try (ResultSet keys = insert.getGeneratedKeys()) {
                        keys.next();
                        officerId = keys.getInt(1);
                    }
                }
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO users (username,password_hash,role,officer_id) VALUES (?,?,?,?)")) {
                    insert.setString(1, "admin");
                    insert.setString(2, PasswordUtil.hash("admin123"));
                    insert.setString(3, "ADMIN");
                    insert.setInt(4, officerId);
                    insert.executeUpdate();
                }
                int criminalId;
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO criminals (full_name,date_of_birth,gender,address,status,identification_marks,previous_records) "
                                + "VALUES ('Sample Record - Test Only','2000-01-01','Unspecified','Training data only','UNDER_SURVEILLANCE','', '')",
                        Statement.RETURN_GENERATED_KEYS)) {
                    insert.executeUpdate();
                    try (ResultSet keys = insert.getGeneratedKeys()) {
                        keys.next();
                        criminalId = keys.getInt(1);
                    }
                }
                int caseId;
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO cases (case_number,title,crime_type,description,location,incident_date,assigned_officer_id,status) "
                                + "VALUES ('DEMO-2026-001','Sample case - test only','Training','Demonstration data; not a real case.','Training environment','2026-01-01',?,'OPEN')",
                        Statement.RETURN_GENERATED_KEYS)) {
                    insert.setInt(1, officerId);
                    insert.executeUpdate();
                    try (ResultSet keys = insert.getGeneratedKeys()) {
                        keys.next();
                        caseId = keys.getInt(1);
                    }
                }
                int investigationId;
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO investigations (case_id,officer_id,progress,notes,status) VALUES (?,?,'Initial training entry','Demonstration data; not a real investigation.','IN_PROGRESS')",
                        Statement.RETURN_GENERATED_KEYS)) {
                    insert.setInt(1, caseId);
                    insert.setInt(2, officerId);
                    insert.executeUpdate();
                    try (ResultSet keys = insert.getGeneratedKeys()) {
                        keys.next();
                        investigationId = keys.getInt(1);
                    }
                }
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO suspects (case_id,criminal_id,full_name,status) VALUES (?,?,'Sample Record - Test Only','IDENTIFIED')")) {
                    insert.setInt(1, caseId);
                    insert.setInt(2, criminalId);
                    insert.executeUpdate();
                }
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO victims (case_id,full_name,identification_info) VALUES (?,'Sample Record - Test Only','Training data only')")) {
                    insert.setInt(1, caseId);
                    insert.executeUpdate();
                }
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO evidence (investigation_id,description,evidence_type,storage_location) "
                                + "VALUES (?,'Demonstration entry; no real evidence.','TRAINING','Training environment')")) {
                    insert.setInt(1, investigationId);
                    insert.executeUpdate();
                }
                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }
}
