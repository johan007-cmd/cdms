package com.cdms;

import java.util.List;

public enum EntityType {
    CRIMINALS("Criminal Records", "criminals", "criminal_id", List.of(
            c("full_name", "Full name", Kind.TEXT, true), c("date_of_birth", "Date of birth (YYYY-MM-DD)", Kind.DATE, false),
            c("gender", "Gender", Kind.TEXT, false), c("address", "Address", Kind.TEXT, false),
            c("contact_number", "Contact number", Kind.TEXT, false), c("photograph_path", "Photograph file path", Kind.TEXT, false),
            c("identification_marks", "Identification marks", Kind.LONG_TEXT, false),
            c("previous_records", "Previous criminal records", Kind.LONG_TEXT, false),
            c("status", "Status (WANTED, ARRESTED, RELEASED, UNDER_SURVEILLANCE)", Kind.TEXT, true))),
    CASES("Crime Cases", "cases", "case_id", List.of(
            c("case_number", "Case number", Kind.TEXT, true), c("title", "Case title", Kind.TEXT, true),
            c("crime_type", "Crime type", Kind.TEXT, true), c("description", "Description", Kind.LONG_TEXT, false),
            c("location", "Location", Kind.TEXT, false), c("incident_date", "Incident date (YYYY-MM-DD)", Kind.DATE, false),
            c("assigned_officer_id", "Assigned officer ID (optional)", Kind.INTEGER, false),
            c("status", "Status (OPEN, UNDER_INVESTIGATION, SOLVED, CLOSED)", Kind.TEXT, true))),
    SUSPECTS("Suspects", "suspects", "suspect_id", List.of(
            c("case_id", "Case ID", Kind.INTEGER, true), c("criminal_id", "Linked criminal ID (optional)", Kind.INTEGER, false),
            c("full_name", "Full name", Kind.TEXT, true), c("date_of_birth", "Date of birth (YYYY-MM-DD)", Kind.DATE, false),
            c("contact_number", "Contact number", Kind.TEXT, false), c("address", "Address", Kind.TEXT, false),
            c("status", "Status", Kind.TEXT, true))),
    VICTIMS("Victims", "victims", "victim_id", List.of(
            c("case_id", "Case ID", Kind.INTEGER, true), c("full_name", "Full name", Kind.TEXT, true),
            c("date_of_birth", "Date of birth (YYYY-MM-DD)", Kind.DATE, false), c("contact_number", "Contact number", Kind.TEXT, false),
            c("address", "Address", Kind.TEXT, false), c("identification_info", "Identification information", Kind.TEXT, false))),
    OFFICERS("Police Officers", "police_officers", "officer_id", List.of(
            c("badge_number", "Badge number", Kind.TEXT, true), c("full_name", "Full name", Kind.TEXT, true),
            c("rank_title", "Rank / designation", Kind.TEXT, true), c("department", "Department", Kind.TEXT, true),
            c("phone", "Phone", Kind.TEXT, false), c("email", "Email", Kind.TEXT, false))),
    INVESTIGATIONS("Investigations", "investigations", "investigation_id", List.of(
            c("case_id", "Case ID", Kind.INTEGER, true), c("officer_id", "Officer ID", Kind.INTEGER, true),
            c("progress", "Progress update", Kind.LONG_TEXT, true), c("notes", "Investigation notes", Kind.LONG_TEXT, false),
            c("status", "Status", Kind.TEXT, true))),
    EVIDENCE("Evidence", "evidence", "evidence_id", List.of(
            c("investigation_id", "Investigation ID", Kind.INTEGER, true), c("description", "Description", Kind.TEXT, true),
            c("evidence_type", "Evidence type", Kind.TEXT, false), c("collected_at", "Collected at (YYYY-MM-DD HH:MM:SS)", Kind.DATETIME, false),
            c("storage_location", "Storage location", Kind.TEXT, false)));

    public enum Kind { TEXT, LONG_TEXT, DATE, INTEGER, DATETIME }
    public record Column(String name, String label, Kind kind, boolean required) {}

    private final String label;
    private final String table;
    private final String primaryKey;
    private final List<Column> columns;

    EntityType(String label, String table, String primaryKey, List<Column> columns) {
        this.label = label;
        this.table = table;
        this.primaryKey = primaryKey;
        this.columns = columns;
    }

    private static Column c(String name, String label, Kind kind, boolean required) {
        return new Column(name, label, kind, required);
    }

    public String label() { return label; }
    public String table() { return table; }
    public String primaryKey() { return primaryKey; }
    public List<Column> columns() { return columns; }
}
