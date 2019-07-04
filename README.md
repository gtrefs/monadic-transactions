# Mondadic transactions 

This is the code for the blog post about *Higher order functions are useful*.

The transaction code is refactored to use the *execute around method pattern*.
This enables already quite some code reuse and conciseness, but the pattern is not purely functional.
Because there still are side effects.
This makes it hard to compose transactional actions and reuse them on a larger scale.

Next, a transaction monad is introduced.
This monad enables the combination of multiple transaction into one single transaction.
Further, the description of an effect is separated from its actual execution.
This means, one can first describe what should happen, before the actual action starts.
Thus, primitive transactions can be declared and composed to more complex transactions.
A new interface `CrudOperations` allows us to mix in the Create, Read, Update and Delete transactions.

Have a look into `Transaction`, `EntityRepository` and `CrudTransactions` to get an impression how the code works.
Further, the tests in `TransactionsMonadShould` provide useful examples.

# Discussion 
While `Transaction` performs reasonable well, there are problems.
Using the `flatMap` combinator extensively results into a `StackOverflowException` because it has a recursive nature.
In **Functional Programming in Scala** Paul and Rúnar cover this topic on page 236 and following.

Further, the side-effecting interpreter `run` is bound to the `Transactional` type itself.
However, it would makes sense to separate it and provide interpreters suitable for a given situation.

Currently, transactions are bound to JPA because of the dependency on `EntityManager`.
It would make sense to invert the dependency and introduce another abstraction.

# Run the example program
To run the example program, clone this repository and execute the following commands in the target directory.

1. Create executable distribution

```
./gradlew clean installDist
:clean
:compileJava
Note: Some input files use unchecked or unsafe operations.
Note: Recompile with -Xlint:unchecked for details.
:processResources
:classes
:jar
:startScripts
:installDist

BUILD SUCCESSFUL

Total time: 1.799 secs
```

2. Execute it with the created start script (For Windows use code.bat)

```
build/install/code/bin/code

Jun 12, 2017 8:04:44 PM org.jboss.weld.bootstrap.WeldStartup <clinit>
INFO: WELD-000900: 2.4.0 (Final)
Jun 12, 2017 8:04:44 PM org.jboss.weld.environment.deployment.discovery.DiscoveryStrategyFactory create
INFO: WELD-ENV-000020: Using jandex for bean discovery
Jun 12, 2017 8:04:44 PM org.jboss.weld.bootstrap.WeldStartup startContainer
INFO: WELD-000101: Transactional services not available. Injection of @Inject UserTransaction not available. Transactional observers will be invoked synchronously.
Jun 12, 2017 8:04:44 PM org.jboss.weld.bootstrap.FastAnnotatedTypeLoader <init>
WARN: WELD-000169: Jandex cannot distinguish inner and static nested classes! Update Jandex to 2.0.3.Final version or newer to improve scanning performance.
Jun 12, 2017 8:04:45 PM org.jboss.weld.environment.se.WeldContainer initialize
INFO: WELD-ENV-002003: Weld SE container STATIC_INSTANCE initialized
Jun 12, 2017 8:04:45 PM org.hibernate.jpa.internal.util.LogHelper logPersistenceUnitInformation
INFO: HHH000204: Processing PersistenceUnitInfo [
	name: example
	...]
Jun 12, 2017 8:04:45 PM org.hibernate.Version logVersion
INFO: HHH000412: Hibernate Core {5.2.10.Final}
Jun 12, 2017 8:04:45 PM org.hibernate.cfg.Environment <clinit>
INFO: HHH000206: hibernate.properties not found
Jun 12, 2017 8:04:45 PM org.hibernate.annotations.common.reflection.java.JavaReflectionManager <clinit>
INFO: HCANN000001: Hibernate Commons Annotations {5.0.1.Final}
Jun 12, 2017 8:04:45 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl configure
WARN: HHH10001002: Using Hibernate built-in connection pool (not for production use!)
Jun 12, 2017 8:04:45 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001005: using driver [org.hsqldb.jdbcDriver] at URL [jdbc:hsqldb:file:target/testdb;shutdown=true]
Jun 12, 2017 8:04:45 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001001: Connection properties: {user=sa}
Jun 12, 2017 8:04:45 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001003: Autocommit mode: false
Jun 12, 2017 8:04:45 PM org.hibernate.engine.jdbc.connections.internal.PooledConnections <init>
INFO: HHH000115: Hibernate connection pool size: 20 (min=1)
Enter an option: 1) Insert a new user. 2) Find a user. 3) List all users 4) Edit a user. 5) Delete a user. 6) Quit the application
```
