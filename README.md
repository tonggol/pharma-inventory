# 製薬在庫管理システム (Pharmaceutical Inventory System)

## 📋 プロジェクト概要

中小規模の製薬会社向けに開発された、Webベースの医薬品在庫管理システムです。このシステムは、医薬品の在庫を効率的かつ正確に管理し、日々の業務をサポートすることを目的としています。

**デプロイ先:** [[https://pharma-inventory-645890639261.asia-northeast2.run.app/](https://pharma-inventory-645890639261.asia-northeast2.run.app/)](https://pharma-backend-877543033848.asia-northeast2.run.app)
https://pharma-backend-877543033848.asia-northeast2.run.app/swagger-ui/index.html

---

## ✨ 主な機能

- **医薬品マスター管理**: 医薬品の基本情報を登録・編集・削除します。
- **ロット別在庫追跡**: ロット番号ごとに医薬品の在庫を追跡し、正確な在庫状況を把握します。
- **入出庫管理**: 医薬品の入庫および出庫トランザクションを記録・管理します。
- **有効期限管理**: 医薬品の有効期限を管理し、期限切れが近い製品を通知します。
- **在庫アラート**: 在庫が設定された最小数量を下回った場合に警告します。
- **ユーザー権限管理**: 役職（例：管理者、一般ユーザー）に応じたアクセス制御を行います。
- **レポート機能**: 在庫状況や入出庫履歴に関するレポートを生成し、Excelファイルとしてエクスポートできます。

---

## 🛠️ 技術スタック

`build.gradle`ファイルに基づき、本プロジェクトで使用されている主要な技術は以下の通りです。

| カテゴリ | 技術 | バージョン |
| :--- | :--- | :--- |
| **Backend** | Java | 21 |
| | Spring Boot | 3.3.0 |
| | Spring Security | 6.x |
| | Spring Data JPA | - |
| | QueryDSL | 5.0.0 |
| **Frontend** | Thymeleaf | - |
| | Bootstrap | 5.x |
| **Database** | MySQL | 8.0.33 |
| | PostgreSQL | 42.7.1 |
| | H2 (開発/テスト用) | - |
| **Build Tool** | Gradle | - |
| **API Docs** | SpringDoc OpenAPI (Swagger) | 2.5.0 |
| **Auth** | JSON Web Tokens (JWT) | 0.11.5 |
| **Others** | Lombok | - |
| | Apache POI (Excel連携) | 5.2.4 |
| | Spring Boot Actuator | - |

---

## 📁 プロジェクト構造

```
pharma-inventory/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/pharma/inventory/
│   │   │       ├── config/         # Spring設定クラス
│   │   │       ├── controller/     # Webコントローラー (APIエンドポイント)
│   │   │       ├── dto/            # データ転送オブジェクト (Data Transfer Objects)
│   │   │       ├── entity/         # JPAエンティティ
│   │   │       ├── exception/      # カスタム例外処理
│   │   │       ├── repository/     # データアクセス層 (Spring Data JPA, QueryDSL)
│   │   │       ├── service/        # ビジネスロジック
│   │   │       └── util/           # ユーティリティクラス (例: JWT関連)
│   │   └── resources/
│   │       ├── static/             # 静的リソース (CSS, JS, 画像)
│   │       ├── templates/          # Thymeleaf テンプレート
│   │       └── application.properties # アプリケーション設定
│   └── test/
├── build.gradle                    # プロジェクト依存性およびビルド設定
└── README.md
```

---

## 🚀 始め方

### 動作環境

- JDK 21 以上
- Gradle 7.0 以上

### 実行方法

1.  **プロジェクトをクローンします。**
    ```bash
    git clone [repository-url]
    cd pharma-inventory
    ```

2.  **アプリケーションを実行します。**
    ```bash
    ./gradlew bootRun
    ```
    `application.properties`で有効になっているデータベース（H2, MySQL, PostgreSQLなど）が起動している必要があります。

3.  **ブラウザでアクセスします。**
    `http://localhost:8080`

### H2 Console (開発用)

- **URL**: `http://localhost:8080/h2-console`
- **JDBC URL**: `jdbc:h2:mem:testdb`
- **Username**: `sa`
- **Password**: (空欄)

---

## 🔐 デフォルトアカウント

- **Username**: `admin`
- **Password**: `admin123`

---

## 📝 APIドキュメント

アプリケーション実行後、以下のURLでSwagger UIを介してAPI仕様書を確認できます。

`http://localhost:8080/swagger-ui.html`

---

## 👤 開発者

- **氏名**: beom
- **目的**: 日本のIT企業への就職活動用ポートフォリオ

---

## 📄 ライセンス

このプロジェクトはポートフォリオ目的で作成されました。
