
-- Seed a demo conversation with a fixed UUID so the frontend stub can show history.
INSERT INTO conversations (id, is_direct, created_at)
VALUES ('00000000-0000-0000-0000-000000000001', false, NOW())
ON CONFLICT DO NOTHING;
