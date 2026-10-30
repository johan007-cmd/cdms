package com.cdms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public final class DashboardDAO {
    public long[] statistics() throws Exception {
        String sql = "SELECT (SELECT COUNT(*) FROM criminals),(SELECT COUNT(*) FROM cases),"
                + "(SELECT COUNT(*) FROM cases WHERE status='UNDER_INVESTIGATION'),"
                + "(SELECT COUNT(*) FROM cases WHERE status IN ('SOLVED','CLOSED')),"
                + "(SELECT COUNT(*) FROM criminals WHERE status='WANTED'),"
                + "(SELECT COUNT(*) FROM police_officers)";
        try (Connection connection = Database.connect(); PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            if (!result.next()) throw new IllegalStateException("Dashboard statistics could not be loaded.");
            long[] values = new long[6];
            for (int i = 0; i < values.length; i++) values[i] = result.getLong(i + 1);
            return values;
        }
    }

    public List<Object[]> recentCases() throws Exception {
        String sql = "SELECT case_number,title,status,created_at FROM cases ORDER BY created_at DESC LIMIT 8";
        try (Connection connection = Database.connect(); PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            List<Object[]> rows = new ArrayList<>();
            while (result.next()) rows.add(new Object[]{result.getString(1), result.getString(2),
                    result.getString(3), result.getTimestamp(4)});
            return rows;
        }
    }

    public List<Object[]> recentInvestigations() throws Exception {
        String sql = "SELECT c.case_number,o.full_name,i.status,i.updated_at FROM investigations i "
                + "JOIN cases c ON c.case_id=i.case_id JOIN police_officers o ON o.officer_id=i.officer_id "
                + "ORDER BY i.updated_at DESC LIMIT 8";
        try (Connection connection = Database.connect(); PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            List<Object[]> rows = new ArrayList<>();
            while (result.next()) rows.add(new Object[]{result.getString(1), result.getString(2),
                    result.getString(3), result.getTimestamp(4)});
            return rows;
        }
    }
}
