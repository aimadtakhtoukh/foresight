CREATE DATABASE planner;
USE planner;

CREATE TABLE IF NOT EXISTS user(
  id BIGINT PRIMARY KEY,
  name TEXT
);

CREATE TABLE IF NOT EXISTS entry(
  id BIGINT PRIMARY KEY,
  date DATE,
  dispo ENUM('On', 'Off', 'Maybe', 'Planning')
);

CREATE TABLE IF NOT EXISTS security_user(
  id BIGINT PRIMARY KEY,
  security_id TEXT,
  type TEXT,
  user_id BIGINT,
  FOREIGN KEY (user_id) REFERENCES user(id)
);