SET GLOBAL time_zone = '-5:00';
CREATE DATABASE jumper_db CHARACTER SET utf8 COLLATE utf8_general_ci;
SHOW DATABASES;

CREATE USER 'jumper_user'@'localhost' identified by "AAAaaa111";
GRANT ALL ON jumper_db.* TO 'jumper_user'@'localhost';

USE jumper_db;

CREATE TABLE game_logs (
	id INT NOT NULL AUTO_INCREMENT,
	player_name VARCHAR(40) NOT NULL,
	score INT NOT NULL,
	PRIMARY KEY pk_game_logs(id)
) ENGINE = innoDB CHARACTER SET utf8 COLLATE utf8_general_ci;

select * from game_logs;

CREATE INDEX idx_highscore ON game_logs(score);
