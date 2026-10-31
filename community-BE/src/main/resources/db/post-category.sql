-- Existing posts stay in the free board. Apply once before deployment.
ALTER TABLE post ADD COLUMN category VARCHAR(20) NOT NULL DEFAULT 'FREE';
