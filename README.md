# Criminal Database Management System

A Java Swing desktop application for criminal records, cases, suspects, victims, officers, investigations, and evidence. It uses MySQL through JDBC, PBKDF2 password hashing, role-aware screens, prepared SQL statements, and PDF report export.

## Requirements

- Windows 10/11
- JDK 17 or newer
- Maven 3.8 or newer
- MySQL 8.0 or newer

## Set up MySQL

Create the application database before starting CDMS:

```sql
CREATE DATABASE cdms CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
```

The application creates its relational tables on first launch. The configured database account needs permission to create and modify tables and records in `cdms`.

Configure connection settings in PowerShell before starting the application. The default JDBC URL is `jdbc:mysql://localhost:3306/cdms?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`. The default username is `root`; set the password in the environment rather than in source code.

```powershell
$env:CDMS_DB_URL = "jdbc:mysql://localhost:3306/cdms?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:CDMS_DB_USER = "root"
$env:CDMS_DB_PASSWORD = "your-mysql-password"
```

Keep the password private and replace the example value with your MySQL password. These environment variables apply to processes started from that PowerShell window.

The same schema is available in [`src/main/resources/schema.sql`](src/main/resources/schema.sql). CDMS applies it automatically when it starts.

## Build and launch

From the project directory:

```powershell
mvn clean package
java -jar target/criminal-database-management-system-1.0.0.jar
```

Launch the packaged JAR with `java -jar`; launching only the compiled classes from `target/classes` does not put Connector/J on the Java runtime classpath. The Maven Shade build includes Connector/J and merges JDBC service registrations into the runnable JAR.

On the first launch, CDMS creates a demo administrator and explicitly labeled test-only sample records. Sign in with:

- Username: `admin`
- Password: `admin123`

Change the initial password after signing in. Additional users can be created by an administrator. Passwords must contain 10-128 characters, including at least one letter and one number.

## Features

- Role-aware sign-in, user administration, password changes, and sign out. Only administrators can delete records or administer accounts.
- Dashboard statistics, recent case updates, and recent investigation activity.
- Searchable, sortable record screens with add, view, edit, and administrator-only delete actions.
- Criminals, cases, suspects, victims, police officers, investigations, and evidence are linked with relational foreign keys.
- Global search across all record modules and PDF export of record lists or search results.
- Required-field, date, integer, status, and email validation. Database errors are shown to the user.

Foreign-key form fields accept the numeric ID shown in the corresponding module. Dates use `YYYY-MM-DD`; evidence timestamps use `YYYY-MM-DD HH:MM:SS`. Optional foreign keys may be left blank.

## Security notes

- Passwords are stored as salted PBKDF2-HMAC-SHA256 hashes; plaintext passwords are not stored.
- Application queries use JDBC prepared statements. Identifiers used for record CRUD are restricted to the application's fixed entity definitions.
- Configure a dedicated, least-privilege MySQL account for deployments and replace the initial administrator password immediately.
- Photograph values are file paths; image files are not copied into the database or rendered by the current desktop client.
