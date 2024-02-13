SET GLOBAL time_zone = '-5:00';
CREATE DATABASE mes_memos_db CHARACTER SET utf8 COLLATE utf8_general_ci;
CREATE USER 'mes_memos_user'@'localhost' identified by "AAAaaa111";
GRANT ALL ON mes_memos_db.* TO 'mes_memos_user'@'localhost';
USE mes_memos_db;
CREATE TABLE user (
	id INT NOT NULL AUTO_INCREMENT,
	username VARCHAR(40) NOT NULL UNIQUE,
	password VARCHAR(255) NOT NULL,
	PRIMARY KEY pk_user(id)
) ENGINE = innoDB CHARACTER SET utf8 COLLATE utf8_general_ci;

CREATE TABLE category (
	id INT NOT NULL AUTO_INCREMENT,
	id_user INT NOT NULL,
	name VARCHAR(255) NOT NULL,
	PRIMARY KEY pk_category(id)
) ENGINE = innoDB CHARACTER SET utf8 COLLATE utf8_general_ci;

CREATE INDEX idx_category ON category(id_user, name);

CREATE TABLE memo (
	id INT NOT NULL AUTO_INCREMENT,
	id_category INT NOT NULL,
	memo TEXT NOT NULL,
    created TIMESTAMP,
	PRIMARY KEY pk_memo(id)
) ENGINE = innoDB CHARACTER SET utf8 COLLATE utf8_general_ci;