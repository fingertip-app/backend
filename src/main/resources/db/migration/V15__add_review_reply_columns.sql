-- 리뷰 테이블에 장인 답글 컬럼 추가
ALTER TABLE reviews
  ADD COLUMN IF NOT EXISTS reply_content TEXT,
  ADD COLUMN IF NOT EXISTS replied_at TIMESTAMP;
