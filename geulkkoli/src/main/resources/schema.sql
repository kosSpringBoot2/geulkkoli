drop table if exists social_info;
drop table if exists report;
drop table if exists user_followings;
drop table if exists comments;
drop table if exists favorites;
drop table if exists post;
drop table if exists users;
drop table if exists topic_tags;

CREATE table if not exists users
(
    user_id   BIGINT PRIMARY KEY AUTO_INCREMENT,
    email     varchar(255) not null,
    password  varchar(255) not null,
    name      varchar(20)  not null,
    nick_name varchar(20)  not null,
    phone_no  varchar(20)  not null,
    gender    varchar(10)  not null,
    sign_up_date        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_email_nick_name_phone_no UNIQUE (email, nick_name, phone_no)
);
create table if not exists topic_tags
(
    topic_id   bigint primary key auto_increment,
    topic_body varchar(255) not null unique
);
create table if not exists post
(
    post_id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    author_id         BIGINT         not null,
    title             varchar(255)   not null,
    body              varchar(11000) not null,
    nick_name         varchar(20)    not null,
    post_hits         BIGINT,
    image_upload_name varchar(255),
    post_topic        Bigint         not null,
    created_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_users foreign key (author_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_post_topic foreign key (post_topic) references topic_tags (topic_id) ON DELETE CASCADE
);
drop table if exists comments;

create table if not exists comments
(
    comment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    author_id  BIGINT       NOT NULL,
    post_id    BIGINT       NOT NULL,
    body       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comment_author FOREIGN KEY (author_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES post (post_id) ON DELETE CASCADE
);

create table if not exists favorites
(
    favorites_id      BIGINT primary key AUTO_INCREMENT,
    post_id           BIGINT not null,
    favorites_user_id BIGINT not null unique,
    constraint fk_like_user FOREIGN KEY (favorites_user_id) REFERENCES users (user_id) ON DELETE cascade,
    constraint fk_like_post FOREIGN KEY (post_id) REFERENCES post (post_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_followings
(
    followings_id bigint primary key AUTO_INCREMENT,
    follower_id   BIGINT NOT NULL,
    followee_id   BIGINT NOT NULL,
    CONSTRAINT fk_follower FOREIGN KEY (follower_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_followee FOREIGN KEY (followee_id) REFERENCES users (user_id) ON DELETE CASCADE
);


create table if not exists roles
(
    role_id bigint primary key auto_increment,
    role_name varchar(255) not null unique,
    user_id bigint not null,
    constraint fk_role_user foreign key (user_id) references users (user_id)
);

create table if not exists report
(
    report_id bigint primary key auto_increment,
    reported_post_id bigint not null,
    reporter_id bigint not null,
    reported_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reason varchar(100) not null,
    constraint fk_reported_post foreign key (reported_post_id) references post (post_id),
    constraint fk_reporter foreign key (reporter_id) references users (user_id),
    constraint unique_report unique (reported_post_id, reporter_id)
);

create table if not exists social_info
(
    social_info_id bigint primary key auto_increment,
    user_id bigint not null,
    social_id varchar(255) not null,
    social_type varchar(255) not null,
    social_connect_date TIMESTAMP Null,
    is_connected boolean default true,
    constraint fk_social_info_user foreign key (user_id) references users (user_id),
    constraint unique_social_info unique (social_id, social_type)
);