-- auto-generated definition
create table admin
(
    adm_idx      bigint auto_increment
        primary key,
    created_at   datetime(6) null,
    updated_at   datetime(6) null,
    adm_id       varchar(50) null comment '관리자 아이디',
    adm_password varchar(50) null comment '관리자 비밀번호'
);

