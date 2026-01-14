drop table if exists cliant cascade;
drop table if exists cliant_key cascade;
drop table if exists cliant_plan cascade;

drop sequence if exists id_seq;
drop sequence if exists key_id_seq;
create sequence id_seq start 10000000;
create sequence key_id_seq start 1;

create table cliant (
    id bigint not null default nextval('id_seq'), 
    -- 企業名
    name varchar(255) not null, 
    -- 契約プラン
    plan_id varchar(255) not null, 
    -- ステータス
    status smallint not null, 
    -- 電話番号
    phone_number varchar(50), 
    -- 説明
    description varchar(255), 
    -- 登録日時
    registered_date timestamp not null, 
    -- 登録者
    register_id varchar(255) not null, 
    -- 更新日時
    updated_date timestamp not null, 
    -- 更新者
    update_id varchar(255) not null, 
    -- 主キー設定
    primary key (id)
);


create table cliant_key (
    cliant_id bigint not null, 
    key_id varchar(50) not null default nextval('key_id_seq'),
    secret_key_hash varchar(255) not null, 
    expires_start_date timestamp not null, 
    expires_end_date timestamp not null, 
    primary key (key_id),
    constraint fk_cliant_key_cliant_id 
        foreign key (cliant_id) 
        references cliant (id)
);

create table cliant_plan (
    -- プランID
    plan_id varchar(255) not null, 
    -- プラン名
    plan_name varchar(255) not null, 
    -- プラン価格
    price integer not null, 
    -- プラン説明
    plan_description varchar(255), 
    -- 主キー設定
    primary key (plan_id)
);