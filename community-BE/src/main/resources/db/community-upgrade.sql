-- Apply once to an existing MySQL database before deploying the baseball UI.
ALTER TABLE post MODIFY COLUMN post_content TEXT;
ALTER TABLE comment MODIFY COLUMN comment VARCHAR(1000) NOT NULL;
