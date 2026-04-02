-- =====================================================
-- Update passwords for seed users
-- =====================================================
-- Use this script to set easy-to-remember passwords for testing
--
-- Password: admin123 -> for Lohgarra (SUPER_ADMIN)
-- Password: test123 -> for all users for simplicity (USER)
-- Password: darth123 -> for DarthVader (RESTRICTED_USER)
-- =====================================================

-- Update Lohgarra's password (SUPER_ADMIN)
-- Password: admin123
-- BCrypt hash: $2a$10$RbKW6k6nRg6FVd.AcBclH.aLEfiXsHO4oB9PF93rLlsb/quajY8/S
UPDATE users
SET author_password = '$2a$10$RbKW6k6nRg6FVd.AcBclH.aLEfiXsHO4oB9PF93rLlsb/quajY8/S'
WHERE author_pseudonym = 'Lohgarra';

-- Update TestAuthor's password (USER)
-- Password: test123
-- BCrypt hash: $2a$10$ZutySzg0/QOkcW5Qw4i6Ye4OExeJbiT92Nowdo159pVfGFnA3nLJ2
UPDATE users
SET author_password = '$2a$10$ZutySzg0/QOkcW5Qw4i6Ye4OExeJbiT92Nowdo159pVfGFnA3nLJ2'
WHERE author_pseudonym = 'TestAuthor';

-- Update wookie_writer's password (USER)
-- Password: test123
-- BCrypt hash: $2a$10$ZutySzg0/QOkcW5Qw4i6Ye4OExeJbiT92Nowdo159pVfGFnA3nLJ2
UPDATE users
SET author_password = '$2a$10$ZutySzg0/QOkcW5Qw4i6Ye4OExeJbiT92Nowdo159pVfGFnA3nLJ2'
WHERE author_pseudonym = 'wookie_writer';

-- Update user1's password (USER)
-- Password: test123
-- BCrypt hash: $2a$10$ZutySzg0/QOkcW5Qw4i6Ye4OExeJbiT92Nowdo159pVfGFnA3nLJ2
UPDATE users
SET author_password = '$2a$10$ZutySzg0/QOkcW5Qw4i6Ye4OExeJbiT92Nowdo159pVfGFnA3nLJ2'
WHERE author_pseudonym = 'user1';

-- Update user2's password (USER)
-- Password: test123
-- BCrypt hash: $2a$10$ZutySzg0/QOkcW5Qw4i6Ye4OExeJbiT92Nowdo159pVfGFnA3nLJ2
UPDATE users
SET author_password = '$2a$10$ZutySzg0/QOkcW5Qw4i6Ye4OExeJbiT92Nowdo159pVfGFnA3nLJ2'
WHERE author_pseudonym = 'user2';

-- Update DarthVader's password (RESTRICTED_USER)
-- Password: darth123
-- BCrypt hash: $2a$10$Xw6wer/tUNunFX4wv0/hrejd6tVPMvccju5MjRLxB8hjSCRcZ6WZy
UPDATE users
SET author_password = '$2a$10$Xw6wer/tUNunFX4wv0/hrejd6tVPMvccju5MjRLxB8hjSCRcZ6WZy'
WHERE author_pseudonym = 'DarthVader';

-- =====================================================
-- IMPORTANT: generate actual BCrypt hashes
-- Run the PasswordGenerator class to generate own hashes
-- Then replace the hash strings with generated hashes
-- =====================================================