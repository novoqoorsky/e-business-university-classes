# --- !Ups

CREATE TABLE role
(
    id   INTEGER NOT NULL PRIMARY KEY,
    name VARCHAR
);

INSERT INTO role (id, name) VALUES (1, 'user');
INSERT INTO role (id, name) VALUES (2, 'admin');

CREATE TABLE "user"
(
    id           UUID    NOT NULL PRIMARY KEY,
    first_name   VARCHAR,
    last_name    VARCHAR,
    email        VARCHAR,
    role_id      INT     NOT NULL,
    activated    INT NOT NULL,
    avatar_url   VARCHAR,
    signed_up_at DATETIME,
    CONSTRAINT auth_user_role_id_fk FOREIGN KEY (role_id) REFERENCES role (id)
);

CREATE TABLE login_info
(
    id           INTEGER NOT NULL PRIMARY KEY ,
    provider_id  VARCHAR,
    provider_key VARCHAR
);

CREATE TABLE user_login_info
(
    user_id       UUID   NOT NULL,
    login_info_id BIGINT NOT NULL,
    CONSTRAINT auth_user_login_info_user_id_fk FOREIGN KEY (user_id) REFERENCES "user" (id),
    CONSTRAINT auth_user_login_info_login_info_id_fk FOREIGN KEY (login_info_id) REFERENCES login_info (id)
);

CREATE TABLE oauth1_info
(
    id            INTEGER NOT NULL PRIMARY KEY,
    token         VARCHAR   NOT NULL,
    secret        VARCHAR   NOT NULL,
    login_info_id BIGINT    NOT NULL,
    CONSTRAINT auth_oauth1_info_login_info_id_fk FOREIGN KEY (login_info_id) REFERENCES login_info (id)
);

CREATE TABLE oauth2_info
(
    id            INTEGER NOT NULL PRIMARY KEY,
    access_token  VARCHAR   NOT NULL,
    token_type    VARCHAR,
    expires_in    INT,
    refresh_token VARCHAR,
    login_info_id BIGINT    NOT NULL,
    CONSTRAINT auth_oauth2_info_login_info_id_fk FOREIGN KEY (login_info_id) REFERENCES login_info (id)
);

CREATE TABLE password_info
(
    hasher        VARCHAR NOT NULL,
    password      VARCHAR NOT NULL,
    salt          VARCHAR,
    login_info_id BIGINT  NOT NULL,
    CONSTRAINT auth_password_info_login_info_id_fk FOREIGN KEY (login_info_id) REFERENCES login_info (id)
);

CREATE TABLE token
(
    id      UUID        NOT NULL PRIMARY KEY,
    user_id UUID        NOT NULL,
    expiry  DATETIME NOT NULL,
    CONSTRAINT auth_token_user_id_fk FOREIGN KEY (user_id) REFERENCES "user" (id)
);

# --- !Downs

DROP TABLE token;
DROP TABLE password_info;
DROP TABLE oauth2_info;
DROP TABLE oauth1_info;
DROP TABLE user_login_info;
DROP TABLE login_info;
DROP TABLE "user";
DROP TABLE role;