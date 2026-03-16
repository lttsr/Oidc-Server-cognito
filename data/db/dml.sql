-- company_plan サンプル（company の plan_id で参照する）
insert into company_plan (plan_id, plan_name, price, plan_description)
values (1, '基本プラン', 0, null);

-- company サンプル（1件目は identity で company_id = 10000000 になる）
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

-- company_logo サンプル（企業ロゴの静的パスを保持）
insert into company_logo (company_id, file_path)
values (
    10000000,
    '/images/logo/sample-company-logo.png'
);

-- user_pool サンプル（user_pool_id / region は実際のCognito環境に合わせて変更すること）
insert into user_pool (company_id, user_pool_id, user_pool_alias, region, client_id, client_secret)
values (
    10000000,
    'ap-northeast-1_A2YE03mKf',
    'user_mst',
    'ap-northeast-1',
    '2u7d5d6kuvpnftinr6jnfhrgq',
    'pseu5jujinm9d646rlht04ih9r368fog01ecn55q57v8gg80kbs'
);
