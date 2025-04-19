USE appdb;

INSERT INTO user_status_lookup VALUES
(1, 'Active'),
(2, 'Terminated');
SELECT * FROM user_status_lookup;

INSERT INTO user_role_lookup VALUES
(1, 'User'),
(2, 'Admin');
SELECT * FROM user_role_lookup;

INSERT INTO permission VALUES
(1, 'read'),
(2, 'write'),
(3, 'approve');
SELECT * FROM permission;

INSERT INTO user_action_lookup VALUES
(1, 'Merchant onboarding & maintenance');
SELECT * FROM user_action_lookup;

INSERT INTO user VALUES(1, 'test@gmail.com', 'Pass@123', 'test', 1, 1);
UPDATE user SET password = '$argon2id$v=19$m=65536,t=10,p=1$4+Wa1CCCduXmGP7yCE0NEw$8pmQcZypnbq+Nclyf9oDMJw0rYVfQNlRv+f+BH9ii7I' WHERE username = 'test';
SELECT * FROM user;

INSERT INTO user_action VALUES
(1, 1);
SELECT * FROM user_action;

INSERT INTO user_action_permission VALUES
(1, 1),
(1, 2);
SELECT * FROM user_action_permission;
