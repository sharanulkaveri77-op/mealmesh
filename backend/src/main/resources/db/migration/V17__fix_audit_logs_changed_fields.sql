-- Alter audit_logs.changed_fields to VARCHAR(500) if it was created as array/text
ALTER TABLE audit_logs ALTER COLUMN changed_fields TYPE VARCHAR(500) USING changed_fields::text;
