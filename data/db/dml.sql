-- user_pool サンプル（user_pool_id / region は実際のCognito環境に合わせて変更すること）
insert into user_pool (cliant_id, user_pool_id, user_pool_alias, region, client_id, client_secret)
values (
    10000000,
    'ap-northeast-1_12345678',
    'test_pool',
    'ap-northeast-1',
    'your-client-id',
    'your-client-secret'
);
