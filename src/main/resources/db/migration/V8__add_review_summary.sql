-- Add summary and content_en columns to reviews table
ALTER TABLE reviews
ADD COLUMN IF NOT EXISTS summary TEXT,
ADD COLUMN IF NOT EXISTS content_en TEXT;
