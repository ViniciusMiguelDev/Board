--liquibase formatted sql
--changeset vinicius:202503201752 runOnChange:false
--comment: boards table create
CREATE TABLE BOARDS (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
) ENGINE = InnoDB;
--rollback DROP TABLE BOARDS;