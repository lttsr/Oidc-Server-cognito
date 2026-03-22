-- テーブル削除
drop table if exists company_oauth_config cascade;
drop table if exists user_pool cascade;
drop table if exists company_logo cascade;
drop table if exists company cascade;
drop table if exists company_plan cascade;

-- シーケンス削除
drop sequence if exists company_id_seq;

-- シーケンス作成
create sequence company_id_seq start 10000000;


-- 企業テーブル
create table company (
    -- 企業ID
    company_id bigint not null generated always as identity (start with 10000000),
    -- 企業名
    name varchar(255) not null,
    -- 契約プラン
    plan_id bigint not null,
    -- ステータス（0:仮登録, 1:有効, 2:無効）
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
    primary key (company_id)
);

-- 企業ロゴテーブル
create table company_logo (
    -- 企業ID
    company_id bigint not null,
    -- ロゴファイルパス（静的リソースへのパス）
    file_path varchar(512) not null,
    -- 主キー設定
    primary key (company_id),
    -- 外部キー制約
    foreign key (company_id) references company(company_id)
);

-- 企業プランテーブル
create table company_plan (
    -- プランID
    plan_id bigint not null,
    -- プラン名
    plan_name varchar(255) not null,
    -- プラン価格
    price integer not null,
    -- プラン説明
    plan_description varchar(255),
    -- 主キー設定
    primary key (plan_id)
);

-- ユーザープールテーブル
create table user_pool (
    -- 企業ID
    company_id bigint not null,
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
    -- 主キー設定
    primary key (company_id, user_pool_id),
    -- 外部キー制約
    foreign key (company_id) references company(company_id)
);

-- OAuthクライアント情報 --
create table company_oauth_config (
    company_id bigint not null,
    client_id varchar(255) not null unique primary key,
    client_secret varchar(255) not null,
    redirect_uris varchar(1000) not null,
    scopes varchar(255) not null,
    registered_date timestamp not null,
    constraint fk_company foreign key (company_id) references company(company_id)
);

