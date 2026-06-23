CREATE TABLE experience_tags (
    experience_id BIGINT NOT NULL REFERENCES experiences(id) ON DELETE CASCADE,
    tag VARCHAR(100) NOT NULL,
    PRIMARY KEY (experience_id, tag)
);

CREATE INDEX idx_experience_tags_tag ON experience_tags(tag);
