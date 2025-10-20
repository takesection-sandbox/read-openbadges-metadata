OpenBadgeの画像ファイル(PNG)からメタ情報を読み出す
===

## 構成

S3にアップロードされたOpen Badgesイメージ (png) からメタ情報を読み出して、DynamoDBに保存します。

![](openbadge.png)

## 参考

- [Open Badges](https://openbadges.org/)

## 環境構築

```
pip install aws-sam-cli
pip freeze > requirements.txt
```
