-- company_plan
insert into plan (plan_id, plan_name, price, plan_description) values (1, '基本プラン', 0, null);
insert into plan (plan_id, plan_name, price, plan_description) values (2, 'プロ', 10000, null);
insert into plan (plan_id, plan_name, price, plan_description) values (3, 'エンタープライズ', 100000, null);

-- company サンプル
insert into company (name, plan_id, status, phone_number, description, registered_date, register_id, updated_date, update_id)
values (
    'サンプル企業',
    1,
    1,
    null,
    null,
    current_timestamp,
    'system',
    current_timestamp,
    'system'
);

-- company_logo
insert into company_logo (company_id, file_path)
values (
    10000000,
    '/images/logo/sample-company-logo.png'
);

-- user_pool サンプル
insert into user_pool (company_id, user_pool_id, user_pool_alias, region, client_id, client_secret)
values (
    10000000,
    'ap-northeast-1_A2YE03mKf',
    'user_mst',
    'ap-northeast-1',
    '2u7d5d6kuvpnftinr6jnfhrgq',
    'pseu5jujinm9d646rlht04ih9r368fog01ecn55q57v8gg80kbs'
);

-- oauth_client サンプル
insert into oauth_client (company_id, client_id, client_secret, redirect_uris, scopes, registered_date)
values (
    10000000,
    'web-7f3c9e1a6b2d4c58',
    'sec_b9d2f61a8c7e4f0ab3d5e9c2a1f67834',
    'http://localhost:3000/callback',
    'openid',
    current_timestamp
);
