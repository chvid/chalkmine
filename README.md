# Chalk Mine - Simple Object Relational Mapper for Java

## Introduction

Chalkmine is an ORM (Object Relational Mapper) for Java 5 and later. It provides a simple and concise way of mapping
back and forth between Java types and relational data in an SQL-database.

It is written by Christian Hvid 2007 - 2016.

## Examples

### Querying simple types

This is an example of its typical use:

```java
openConnection();
try {
    int userCount = queryScalar(Int.class, "select count(*) from users where name = ?", "Smith");
    System.out.println("There are " + userCount + " users named 'Smith'.");
} finally {
    closeConnection();
}
```

The methods openConnection, queryScalar and closeConnection are all static methods in com.apelab.chalkmine.ChalkMine and are here
imported using a static import.

The methods openConnection and closeConnection open and close a connection.
The connection is bound to the current thread which is why it is not given as an explicit parameter to queryScalar.

The method queryScalar does a query expecting exactly one row otherwise com.apelab.chalkmine.NonScalarException is thrown.

The row is mapped to the type supplied as the first parameter.

Since the type is a simple type (int, double, string etc.) the first column of row is used.

The last parameter ("Smith") as one a variable number of parameters that will be substituted into the ?'s in the query.
(This happens using Java's PreparedStatement mechanism taking care of proper escaping of strings etc.)

### Querying complex data

Suppose you have a Java class defined like this:

```java
public class User {
    private String name;
    private String country;
    private int yearOfBirth;

    public getName() { return name; }
    public getCountry() { return country; }
    public getYearOfBirth() { return yearOfBirth; }

    public User(String name, String country, int yearOfBirth) {
        this.name = name;
        this.country = country;
        this.yearOfBirth = yearOfBirth;
    }
}
```

You query a table into instances of this class like this:

```java
List<User> user = queryList(User.class, "select name, country, year_of_birth from users");
```

The method queryList will do the query expecting any number of rows. The result will be a list of the given type.
Because it is a not a simple type, ChalkMine will look for a constructor matching the SQL types of columns
(here probably varchar, varchar, integer).

### Modifying data

The following updates a database table:

```sql
update("insert into users(name, country, year_of_birth) values(?, ?, ?)", "Smith", "Bahamas", 1956);
```

### Accessing multiple databases within the same application

When you write openConnection() rather than openConnection("some explicit name") ChalkMine will look for a configuration
matching the package name of the calling class. This means that if you call openConnection() from a class inside
com.apelab.bananas then ChalkMine will look for the first configuration matching the following:

 - com.apelab.bananas
 - com.apelab
 - com
 - default

### Configuration

ChalkMine has two ways of configuring its datasources; either will get its datasource from JNDI which typically is a
good way if you use ChalkMine within an application server.

To setup a database used across the application configure a datasource under the name "java:/comp/env/jdbc/default".
To setup a database for a specific part of the application put it in under ie.
```java:/comp/env/jdbc/com/apelab/bananas```.

Alternatively you configure ChalkMine to use its own datasource by providing ChalkMine a database url, database driver
and so on.

ChalkMine reads it configuration from Java's system property mechanism.

This will configure a default database:

```
com.apelab.chalkmine.configurationProvider=system

default.dbDataSource = com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource
default.dbDatabaseName = test
default.dbServerName = localhost
default.dbUser = root
```

### A complete, functioning example

See test.HsqlIntegrationTest for an example using an in memory HSQL-database.

## License

Open source, provided as is.
