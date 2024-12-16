# clickhouse-acid-delete-test

- For https://github.com/apache/shardingsphere/issues/29052 .
- Verified unit test under Ubuntu 22.04.4 LTS with `SDKMAN!` and `Docker CE`.

```shell
sdk install java 23-open

git clone git@github.com:linghengqian/clickhouse-acid-delete-test.git
cd ./clickhouse-acid-delete-test/
sdk use java 23-open
./mvnw clean test
```

- The log is as follows.

```shell
[INFO] Scanning for projects...
[INFO] 
[INFO] ---------< io.github.linghengqian:clickhouse-acid-delete-test >---------
[INFO] Building clickhouse-acid-delete-test 1.0-SNAPSHOT
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- clean:3.2.0:clean (default-clean) @ clickhouse-acid-delete-test ---
[INFO] Deleting /home/linghengqian/TwinklingLiftWorks/git/public/clickhouse-acid-delete-test/target
[INFO] 
[INFO] --- resources:3.3.1:resources (default-resources) @ clickhouse-acid-delete-test ---
[INFO] skip non existing resourceDirectory /home/linghengqian/TwinklingLiftWorks/git/public/clickhouse-acid-delete-test/src/main/resources
[INFO] 
[INFO] --- compiler:3.13.0:compile (default-compile) @ clickhouse-acid-delete-test ---
[INFO] No sources to compile
[INFO] 
[INFO] --- resources:3.3.1:testResources (default-testResources) @ clickhouse-acid-delete-test ---
[INFO] Copying 2 resources from src/test/resources to target/test-classes
[INFO] 
[INFO] --- compiler:3.13.0:testCompile (default-testCompile) @ clickhouse-acid-delete-test ---
[INFO] Recompiling the module because of changed source code.
[INFO] Compiling 2 source files with javac [debug target 23] to target/test-classes
[INFO] 
[INFO] --- surefire:3.2.5:test (default-test) @ clickhouse-acid-delete-test ---
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running io.github.linghengqian.ClickHouseTest
SLF4J: No SLF4J providers were found.
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See https://www.slf4j.org/codes.html#noProviders for further details.
[ERROR] Tests run: 1, Failures: 0, Errors: 1, Skipped: 0, Time elapsed: 6.667 s <<< FAILURE! -- in io.github.linghengqian.ClickHouseTest
[ERROR] io.github.linghengqian.ClickHouseTest.testClickHouse -- Time elapsed: 0.770 s <<< ERROR!
java.lang.RuntimeException: 
java.lang.RuntimeException: java.sql.BatchUpdateException: Code: 341. DB::Exception: Exception happened during execution of mutation 'mutation_14.txt' with part 'all_1_6_1' reason: 'Serialization error: part all_1_6_1 is locked by transaction 4745739302109261907'. This error maybe retryable or not. In case of unretryable error, mutation can be killed with KILL MUTATION query 

0. DB::checkMutationStatus(std::optional<DB::MergeTreeMutationStatus>&, std::set<String, std::less<String>, std::allocator<String>> const&) @ 0x00000000129ef95b
1. DB::StorageMergeTree::waitForMutation(long, String const&, bool) @ 0x0000000012bf9652
2. DB::StorageMergeTree::mutate(DB::MutationCommands const&, std::shared_ptr<DB::Context const>) @ 0x0000000012bfa8cb
3. DB::InterpreterAlterQuery::executeToTable(DB::ASTAlterQuery const&) @ 0x0000000011669699
4. DB::InterpreterAlterQuery::execute() @ 0x000000001166698d
5. DB::executeQueryImpl(char const*, char const*, std::shared_ptr<DB::Context>, DB::QueryFlags, DB::QueryProcessingStage::Enum, DB::ReadBuffer*) @ 0x0000000011c7cea6
6. DB::executeQuery(DB::ReadBuffer&, DB::WriteBuffer&, bool, std::shared_ptr<DB::Context>, std::function<void (DB::QueryResultDetails const&)>, DB::QueryFlags, std::optional<DB::FormatSettings> const&, std::function<void (DB::IOutputFormat&, String const&, std::shared_ptr<DB::Context const> const&, std::optional<DB::FormatSettings> const&)>) @ 0x0000000011c812ac
7. DB::HTTPHandler::processQuery(DB::HTTPServerRequest&, DB::HTMLForm&, DB::HTTPServerResponse&, DB::HTTPHandler::Output&, std::optional<DB::CurrentThread::QueryScope>&, StrongTypedef<unsigned long, ProfileEvents::EventTag> const&) @ 0x0000000012e3b092
8. DB::HTTPHandler::handleRequest(DB::HTTPServerRequest&, DB::HTTPServerResponse&, StrongTypedef<unsigned long, ProfileEvents::EventTag> const&) @ 0x0000000012e3f49e
9. DB::HTTPServerConnection::run() @ 0x0000000012ede461
10. Poco::Net::TCPServerConnection::start() @ 0x0000000015d89527
11. Poco::Net::TCPServerDispatcher::run() @ 0x0000000015d899b9
12. Poco::PooledThread::run() @ 0x0000000015d563fc
13. Poco::ThreadImpl::runnableEntry(void*) @ 0x0000000015d5499d
14. ? @ 0x00007f72a6253ac3
15. ? @ 0x00007f72a62e4a04

. (UNFINISHED) (version 24.11.1.2557 (official build))

        at java.base/jdk.internal.reflect.DirectConstructorHandleAccessor.newInstance(DirectConstructorHandleAccessor.java:62)
        at java.base/java.lang.reflect.Constructor.newInstanceWithCaller(Constructor.java:501)
        at java.base/java.lang.reflect.Constructor.newInstance(Constructor.java:485)
        at java.base/java.util.concurrent.ForkJoinTask.getException(ForkJoinTask.java:555)
        at java.base/java.util.concurrent.ForkJoinTask.reportException(ForkJoinTask.java:573)
        at java.base/java.util.concurrent.ForkJoinTask.join(ForkJoinTask.java:663)
        at java.base/java.util.concurrent.ForkJoinTask.invoke(ForkJoinTask.java:677)
        at java.base/java.util.stream.ForEachOps$ForEachOp.evaluateParallel(ForEachOps.java:160)
        at java.base/java.util.stream.ForEachOps$ForEachOp$OfInt.evaluateParallel(ForEachOps.java:189)
        at java.base/java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:264)
        at java.base/java.util.stream.IntPipeline.forEach(IntPipeline.java:466)
        at java.base/java.util.stream.IntPipeline$Head.forEach(IntPipeline.java:623)
        at io.github.linghengqian.ClickHouseTest.testClickHouse(ClickHouseTest.java:107)
        at java.base/java.lang.reflect.Method.invoke(Method.java:580)
        at java.base/java.util.ArrayList.forEach(ArrayList.java:1597)
        at java.base/java.util.ArrayList.forEach(ArrayList.java:1597)
Caused by: java.lang.RuntimeException: java.sql.BatchUpdateException: Code: 341. DB::Exception: Exception happened during execution of mutation 'mutation_14.txt' with part 'all_1_6_1' reason: 'Serialization error: part all_1_6_1 is locked by transaction 4745739302109261907'. This error maybe retryable or not. In case of unretryable error, mutation can be killed with KILL MUTATION query 

0. DB::checkMutationStatus(std::optional<DB::MergeTreeMutationStatus>&, std::set<String, std::less<String>, std::allocator<String>> const&) @ 0x00000000129ef95b
1. DB::StorageMergeTree::waitForMutation(long, String const&, bool) @ 0x0000000012bf9652
2. DB::StorageMergeTree::mutate(DB::MutationCommands const&, std::shared_ptr<DB::Context const>) @ 0x0000000012bfa8cb
3. DB::InterpreterAlterQuery::executeToTable(DB::ASTAlterQuery const&) @ 0x0000000011669699
4. DB::InterpreterAlterQuery::execute() @ 0x000000001166698d
5. DB::executeQueryImpl(char const*, char const*, std::shared_ptr<DB::Context>, DB::QueryFlags, DB::QueryProcessingStage::Enum, DB::ReadBuffer*) @ 0x0000000011c7cea6
6. DB::executeQuery(DB::ReadBuffer&, DB::WriteBuffer&, bool, std::shared_ptr<DB::Context>, std::function<void (DB::QueryResultDetails const&)>, DB::QueryFlags, std::optional<DB::FormatSettings> const&, std::function<void (DB::IOutputFormat&, String const&, std::shared_ptr<DB::Context const> const&, std::optional<DB::FormatSettings> const&)>) @ 0x0000000011c812ac
7. DB::HTTPHandler::processQuery(DB::HTTPServerRequest&, DB::HTMLForm&, DB::HTTPServerResponse&, DB::HTTPHandler::Output&, std::optional<DB::CurrentThread::QueryScope>&, StrongTypedef<unsigned long, ProfileEvents::EventTag> const&) @ 0x0000000012e3b092
8. DB::HTTPHandler::handleRequest(DB::HTTPServerRequest&, DB::HTTPServerResponse&, StrongTypedef<unsigned long, ProfileEvents::EventTag> const&) @ 0x0000000012e3f49e
9. DB::HTTPServerConnection::run() @ 0x0000000012ede461
10. Poco::Net::TCPServerConnection::start() @ 0x0000000015d89527
11. Poco::Net::TCPServerDispatcher::run() @ 0x0000000015d899b9
12. Poco::PooledThread::run() @ 0x0000000015d563fc
13. Poco::ThreadImpl::runnableEntry(void*) @ 0x0000000015d5499d
14. ? @ 0x00007f72a6253ac3
15. ? @ 0x00007f72a62e4a04

. (UNFINISHED) (version 24.11.1.2557 (official build))

        at io.github.linghengqian.ClickHouseTest.lambda$testClickHouse$2(ClickHouseTest.java:113)
        at java.base/java.util.stream.ForEachOps$ForEachOp$OfInt.accept(ForEachOps.java:205)
        at java.base/java.util.stream.Streams$RangeIntSpliterator.forEachRemaining(Streams.java:104)
        at java.base/java.util.Spliterator$OfInt.forEachRemaining(Spliterator.java:712)
        at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:570)
        at java.base/java.util.stream.ForEachOps$ForEachTask.compute(ForEachOps.java:291)
        at java.base/java.util.concurrent.CountedCompleter.exec(CountedCompleter.java:759)
        at java.base/java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:507)
        at java.base/java.util.concurrent.ForkJoinPool$WorkQueue.topLevelExec(ForkJoinPool.java:1458)
        at java.base/java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:2034)
        at java.base/java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:189)
Caused by: java.sql.BatchUpdateException: Code: 341. DB::Exception: Exception happened during execution of mutation 'mutation_14.txt' with part 'all_1_6_1' reason: 'Serialization error: part all_1_6_1 is locked by transaction 4745739302109261907'. This error maybe retryable or not. In case of unretryable error, mutation can be killed with KILL MUTATION query 

0. DB::checkMutationStatus(std::optional<DB::MergeTreeMutationStatus>&, std::set<String, std::less<String>, std::allocator<String>> const&) @ 0x00000000129ef95b
1. DB::StorageMergeTree::waitForMutation(long, String const&, bool) @ 0x0000000012bf9652
2. DB::StorageMergeTree::mutate(DB::MutationCommands const&, std::shared_ptr<DB::Context const>) @ 0x0000000012bfa8cb
3. DB::InterpreterAlterQuery::executeToTable(DB::ASTAlterQuery const&) @ 0x0000000011669699
4. DB::InterpreterAlterQuery::execute() @ 0x000000001166698d
5. DB::executeQueryImpl(char const*, char const*, std::shared_ptr<DB::Context>, DB::QueryFlags, DB::QueryProcessingStage::Enum, DB::ReadBuffer*) @ 0x0000000011c7cea6
6. DB::executeQuery(DB::ReadBuffer&, DB::WriteBuffer&, bool, std::shared_ptr<DB::Context>, std::function<void (DB::QueryResultDetails const&)>, DB::QueryFlags, std::optional<DB::FormatSettings> const&, std::function<void (DB::IOutputFormat&, String const&, std::shared_ptr<DB::Context const> const&, std::optional<DB::FormatSettings> const&)>) @ 0x0000000011c812ac
7. DB::HTTPHandler::processQuery(DB::HTTPServerRequest&, DB::HTMLForm&, DB::HTTPServerResponse&, DB::HTTPHandler::Output&, std::optional<DB::CurrentThread::QueryScope>&, StrongTypedef<unsigned long, ProfileEvents::EventTag> const&) @ 0x0000000012e3b092
8. DB::HTTPHandler::handleRequest(DB::HTTPServerRequest&, DB::HTTPServerResponse&, StrongTypedef<unsigned long, ProfileEvents::EventTag> const&) @ 0x0000000012e3f49e
9. DB::HTTPServerConnection::run() @ 0x0000000012ede461
10. Poco::Net::TCPServerConnection::start() @ 0x0000000015d89527
11. Poco::Net::TCPServerDispatcher::run() @ 0x0000000015d899b9
12. Poco::PooledThread::run() @ 0x0000000015d563fc
13. Poco::ThreadImpl::runnableEntry(void*) @ 0x0000000015d5499d
14. ? @ 0x00007f72a6253ac3
15. ? @ 0x00007f72a62e4a04

. (UNFINISHED) (version 24.11.1.2557 (official build))

        at com.clickhouse.jdbc.SqlExceptionUtils.batchUpdateError(SqlExceptionUtils.java:107)
        at com.clickhouse.jdbc.internal.SqlBasedPreparedStatement.executeAny(SqlBasedPreparedStatement.java:223)
        at com.clickhouse.jdbc.internal.SqlBasedPreparedStatement.executeLargeUpdate(SqlBasedPreparedStatement.java:302)
        at com.clickhouse.jdbc.internal.AbstractPreparedStatement.executeUpdate(AbstractPreparedStatement.java:135)
        at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)
        at com.zaxxer.hikari.pool.HikariProxyPreparedStatement.executeUpdate(HikariProxyPreparedStatement.java)
        at io.github.linghengqian.ClickHouseTest.lambda$testClickHouse$2(ClickHouseTest.java:111)
        ... 10 more

[INFO] Running io.github.linghengqian.MySqlTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 10.49 s -- in io.github.linghengqian.MySqlTest
[INFO] 
[INFO] Results:
[INFO] 
[ERROR] Errors: 
[ERROR]   ClickHouseTest.testClickHouse:107 » Runtime java.lang.RuntimeException: java.sql.BatchUpdateException: Code: 341. DB::Exception: Exception happened during execution of mutation 'mutation_14.txt' with part 'all_1_6_1' reason: 'Serialization error: part all_1_6_1 is locked by transaction 4745739302109261907'. This error maybe retryable or not. In case of unretryable error, mutation can be killed with KILL MUTATION query 

0. DB::checkMutationStatus(std::optional<DB::MergeTreeMutationStatus>&, std::set<String, std::less<String>, std::allocator<String>> const&) @ 0x00000000129ef95b
1. DB::StorageMergeTree::waitForMutation(long, String const&, bool) @ 0x0000000012bf9652
2. DB::StorageMergeTree::mutate(DB::MutationCommands const&, std::shared_ptr<DB::Context const>) @ 0x0000000012bfa8cb
3. DB::InterpreterAlterQuery::executeToTable(DB::ASTAlterQuery const&) @ 0x0000000011669699
4. DB::InterpreterAlterQuery::execute() @ 0x000000001166698d
5. DB::executeQueryImpl(char const*, char const*, std::shared_ptr<DB::Context>, DB::QueryFlags, DB::QueryProcessingStage::Enum, DB::ReadBuffer*) @ 0x0000000011c7cea6
6. DB::executeQuery(DB::ReadBuffer&, DB::WriteBuffer&, bool, std::shared_ptr<DB::Context>, std::function<void (DB::QueryResultDetails const&)>, DB::QueryFlags, std::optional<DB::FormatSettings> const&, std::function<void (DB::IOutputFormat&, String const&, std::shared_ptr<DB::Context const> const&, std::optional<DB::FormatSettings> const&)>) @ 0x0000000011c812ac
7. DB::HTTPHandler::processQuery(DB::HTTPServerRequest&, DB::HTMLForm&, DB::HTTPServerResponse&, DB::HTTPHandler::Output&, std::optional<DB::CurrentThread::QueryScope>&, StrongTypedef<unsigned long, ProfileEvents::EventTag> const&) @ 0x0000000012e3b092
8. DB::HTTPHandler::handleRequest(DB::HTTPServerRequest&, DB::HTTPServerResponse&, StrongTypedef<unsigned long, ProfileEvents::EventTag> const&) @ 0x0000000012e3f49e
9. DB::HTTPServerConnection::run() @ 0x0000000012ede461
10. Poco::Net::TCPServerConnection::start() @ 0x0000000015d89527
11. Poco::Net::TCPServerDispatcher::run() @ 0x0000000015d899b9
12. Poco::PooledThread::run() @ 0x0000000015d563fc
13. Poco::ThreadImpl::runnableEntry(void*) @ 0x0000000015d5499d
14. ? @ 0x00007f72a6253ac3
15. ? @ 0x00007f72a62e4a04

. (UNFINISHED) (version 24.11.1.2557 (official build))

[INFO] 
[ERROR] Tests run: 2, Failures: 0, Errors: 1, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  19.853 s
[INFO] Finished at: 2024-12-16T10:02:53+08:00
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin:3.2.5:test (default-test) on project clickhouse-acid-delete-test: 
[ERROR] 
[ERROR] Please refer to /home/linghengqian/TwinklingLiftWorks/git/public/clickhouse-acid-delete-test/target/surefire-reports for the individual test results.
[ERROR] Please refer to dump files (if any exist) [date].dump, [date]-jvmRun[N].dump and [date].dumpstream.
[ERROR] -> [Help 1]
[ERROR] 
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR] 
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoFailureException
```
