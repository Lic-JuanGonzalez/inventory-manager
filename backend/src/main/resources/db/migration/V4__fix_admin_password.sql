-- Set correct BCrypt(12) hash for Admin@1234 on the initial admin user
UPDATE users
SET password = '$2a$12$dMIg/wlW4HeBGVKsz2z1mu8FIHaEUHqtOTeFX5Wvw/2w//9FiQ6wu'
WHERE email = 'admin@inventory.com';
