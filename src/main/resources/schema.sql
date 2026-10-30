CREATE TABLE IF NOT EXISTS police_officers (
  officer_id INT AUTO_INCREMENT PRIMARY KEY,
  badge_number VARCHAR(30) NOT NULL UNIQUE,
  full_name VARCHAR(120) NOT NULL,
  rank_title VARCHAR(80) NOT NULL,
  department VARCHAR(100) NOT NULL,
  phone VARCHAR(30),
  email VARCHAR(150),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS users (
  user_id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(60) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role ENUM('ADMIN','OFFICER') NOT NULL,
  officer_id INT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_user_officer FOREIGN KEY (officer_id) REFERENCES police_officers(officer_id) ON DELETE SET NULL
);
CREATE TABLE IF NOT EXISTS criminals (
  criminal_id INT AUTO_INCREMENT PRIMARY KEY,
  full_name VARCHAR(120) NOT NULL,
  date_of_birth DATE,
  gender VARCHAR(30),
  address VARCHAR(500),
  contact_number VARCHAR(30),
  photograph_path VARCHAR(500),
  identification_marks TEXT,
  previous_records TEXT,
  status ENUM('WANTED','ARRESTED','RELEASED','UNDER_SURVEILLANCE') NOT NULL DEFAULT 'UNDER_SURVEILLANCE',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_criminal_name (full_name),
  INDEX idx_criminal_status (status)
);
CREATE TABLE IF NOT EXISTS cases (
  case_id INT AUTO_INCREMENT PRIMARY KEY,
  case_number VARCHAR(40) NOT NULL UNIQUE,
  title VARCHAR(180) NOT NULL,
  crime_type VARCHAR(100) NOT NULL,
  description TEXT,
  location VARCHAR(250),
  incident_date DATE,
  assigned_officer_id INT NULL,
  status ENUM('OPEN','UNDER_INVESTIGATION','SOLVED','CLOSED') NOT NULL DEFAULT 'OPEN',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_case_officer FOREIGN KEY (assigned_officer_id) REFERENCES police_officers(officer_id) ON DELETE SET NULL,
  INDEX idx_case_type (crime_type),
  INDEX idx_case_status (status)
);
CREATE TABLE IF NOT EXISTS suspects (
  suspect_id INT AUTO_INCREMENT PRIMARY KEY,
  case_id INT NOT NULL,
  criminal_id INT NULL,
  full_name VARCHAR(120) NOT NULL,
  date_of_birth DATE,
  contact_number VARCHAR(30),
  address VARCHAR(500),
  status VARCHAR(50) NOT NULL DEFAULT 'IDENTIFIED',
  CONSTRAINT fk_suspect_case FOREIGN KEY (case_id) REFERENCES cases(case_id) ON DELETE CASCADE,
  CONSTRAINT fk_suspect_criminal FOREIGN KEY (criminal_id) REFERENCES criminals(criminal_id) ON DELETE SET NULL
);
CREATE TABLE IF NOT EXISTS victims (
  victim_id INT AUTO_INCREMENT PRIMARY KEY,
  case_id INT NOT NULL,
  full_name VARCHAR(120) NOT NULL,
  date_of_birth DATE,
  contact_number VARCHAR(30),
  address VARCHAR(500),
  identification_info VARCHAR(250),
  CONSTRAINT fk_victim_case FOREIGN KEY (case_id) REFERENCES cases(case_id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS investigations (
  investigation_id INT AUTO_INCREMENT PRIMARY KEY,
  case_id INT NOT NULL,
  officer_id INT NOT NULL,
  progress TEXT NOT NULL,
  notes TEXT,
  status VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_investigation_case FOREIGN KEY (case_id) REFERENCES cases(case_id) ON DELETE CASCADE,
  CONSTRAINT fk_investigation_officer FOREIGN KEY (officer_id) REFERENCES police_officers(officer_id) ON DELETE RESTRICT,
  INDEX idx_investigation_updated (updated_at)
);
CREATE TABLE IF NOT EXISTS evidence (
  evidence_id INT AUTO_INCREMENT PRIMARY KEY,
  investigation_id INT NOT NULL,
  description VARCHAR(500) NOT NULL,
  evidence_type VARCHAR(100),
  collected_at DATETIME,
  storage_location VARCHAR(250),
  CONSTRAINT fk_evidence_investigation FOREIGN KEY (investigation_id) REFERENCES investigations(investigation_id) ON DELETE CASCADE
);
