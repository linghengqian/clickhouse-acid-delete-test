# clickhouse-acid-delete-test

- For https://github.com/apache/shardingsphere/issues/29052 .
- Verified unit test under Ubuntu 22.04.4 LTS with `SDKMAN!` and `Docker CE`.

```shell
sdk install java 23-open

git clone git@github.com:linghengqian/clickhouse-acid-delete-test.git
cd ./clickhouse-acid-delete-test/
sdk use java 23-open
./mvnw -T 1C clean test
```
