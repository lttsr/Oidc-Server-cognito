drop table if exists cliant cascade;
drop table if exists cliant_plan cascade;
drop table if exists user_pool cascade;

drop sequence if exists cliant_id_seq;
create sequence cliant_id_seq start 10000000;

create table cliant (
    -- 企業ID
    cliant_id bigint not null default nextval('cliant_id_seq'), 
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
    primary key (cliant_id)
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

create table user_pool (
    -- 企業ID
    cliant_id bigint not null,
    -- CognitoユーザープールID（例: ap-northeast-1_12345678）
    user_pool_id varchar(255) not null,
    -- ユーザープールエイリアス
    user_pool_alias varchar(255) not null,
    -- リージョン（例: ap-northeast-1）
    region varchar(50) not null,
    -- クライアントID
    client_id varchar(255) not null,
    -- クライアントシークレット
    client_secret varchar(512),
    primary key (user_pool_id),
    foreign key (cliant_id) references cliant(cliant_id)
);