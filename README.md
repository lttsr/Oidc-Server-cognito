# cognito-api

## 概要
AWS Cognitoをバックエンドに持つ、OAuth2 / OIDC 認証サーバー（Spring Authorization Server）です。
---

## 認証フロー全体像
```
➀ブラウザ/フロントエンド
      │
      │ GET /oauth2/authorize?response_type=code&client_id=xxx&redirect_uri=xxx&scope=openid
      ▼
➁auth-service
      │ 未認証の場合 → /api/auth/init（ログイン画面）へリダイレクト
      │ 入力情報を元にAWS Cognitoにて認証開始(sdk利用)
      │
      ▼
➂AWS Cognito
      │ IDトークン（JWT）を返す
      ▼
➃auth-service
      │ CognitoのJWKS公開鍵でIDトークンを署名検証
      │ 認証済みトークン（UsernamePasswordAuthenticationToken）をSecurityContextHolderに格納
      │
      │ GET /callback?code=xxxxxx  ← 設定された値へリダイレクト
      ▼
➄リダイレクト先
      │ POST /oauth2/token に code を送ってトークンと交換
      ▼
➅レスポンス返却
      {
        "access_token":  "eyJ..."  // APIアクセス用
        "refresh_token": "eyJ..."  // トークン更新用
        "id_token":      "eyJ..."  // ユーザー情報（OIDC）
      }
```

---
## エンドポイント一覧
| エンドポイント | メソッド | 説明 |
|---|---|---|
| `/oauth2/authorize` | GET | 認可エンドポイント（ログイン開始） |
| `/api/auth/init` | GET | ログイン画面 |
| `/api/auth/login` | POST | ログイン処理 |
| `/api/auth/mfa` | GET | MFA認証画面 |
| `/api/auth/forgot-password` | GET/POST | パスワードリセット |
| `/oauth2/token` | POST | トークン発行 |
| `/oauth2/userinfo` | GET | ユーザー情報取得（OIDC） |
| `/.well-known/openid-configuration` | GET | OIDC設定情報 |
---

## 認可エンドポイントURL（サンプル）
```
http://localhost:8080/oauth2/authorize?response_type=code&client_id=web-7f3c9e1a6b2d4c58&redirect_uri=http://localhost:3000/callback&scope=openid
```
---


## Spring Security カスタム設定

### CustomRegisteredClientRepository

`RegisteredClientRepository`（Spring Security のインターフェース）を独自実装したクラスです。

`/oauth2/authorize` にリクエストが来ると、Spring Security は自動で `findByClientId()` を呼び出し、  
`oauth_client` テーブルを検索してクライアントの正当性を検証します。  
DBの `OAuthClient` エンティティは `toRegisteredClient()` で Spring Security が扱える `RegisteredClient` 型に変換されます。

```
OAuthClient（DBエンティティ）→ toRegisteredClient() → RegisteredClient（Spring Security型）
```

> `oauth_client` テーブルへのデータ登録はクライアントとの利用契約後に必ず行ってください。  
> 未登録の場合、認可エンドポイントへのアクセス時にエラーとなります。

### id_token

本サーバーからの `code` を受け取り後、すぐに `/oauth2/token` にアクセスしてトークン取得を行ってください。

#### 発行されるトークン一覧

| トークン | 用途 |
|---|---|
| `access_token` | APIアクセス時の認可（`Authorization: Bearer xxx`） |
| `refresh_token` | トークン期限切れ時の再取得（`/api/auth/refresh`） |
| `id_token` | ログインユーザーの情報取得（OIDC） |

#### id_token のクレーム

| クレーム | 説明 |
|---|---|
| `sub` | CognitoユーザーのUUID。Cognitoが発行する**不変の一意識別子**。連携先DBでの外部キーとして使用してください。 |
| `user_pool_id` | 認証に使用したCognitoユーザープールID |

> `sub` はユーザー削除・再作成をしない限り変わりません。連携先DBは初回ログイン時に `sub` を主キーとしてユーザーレコードを作成してください。

---

---
## OAuth2 と OIDC の関係
```
OIDC（上位層）
  └─ scope=openid を付けると id_token が発行される
  └─ /oauth2/userinfo エンドポイントが使える
OAuth2（基盤層）
  └─ Authorization Code フロー
  └─ access_token / refresh_token の発行
```
---
