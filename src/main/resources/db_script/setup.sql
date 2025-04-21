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
(1, 'EMail');
SELECT * FROM user_action_lookup;

INSERT INTO user_action VALUES
(1, 1);
SELECT * FROM user_action;

INSERT INTO user_action_permission VALUES
(1, 1),
(1, 2);
SELECT * FROM user_action_permission;
