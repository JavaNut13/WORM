# WORM

_Worm is an Object Relational Map_

Worm lets you store objects as rows in a database with as little friction as possible. At the moment it is geared toward JDBC SQLite databases, but you can either modify or implement a new `Connection` class to use WORM with another database or platform.

## Getting Started

### Tables

You'll need to make some models - these are just classes with the `@Table` annotation.
    
    @Table
    public class Person extends Row {
        @Stored public String name;
        @Stored public int age;
        @Stored public float height;
    }
    
Extending `Row` is optional, however it adds a handy `save()` method and adds a unique integer ID to all your records. If you want to use a custom key you can specify this in the table annotation:

    @Table(key="name")
    public class Person { ... }

By default WORM will use the auto-generated SQLite `rowid` column to identify the rows.

### Migrations

To change the schema of your database, or to perform other operations when a connection is opened. This is done by extending `Migrator`:

    public class MyMigrator extends Migrator {
      protected void upgrade() {
        ...
      }
    
      protected Class[] tables() {
        return new Class[] {
            Person.class
        };
      }
    }

Migrations go in the `upgrade()` method, and can be in three basic forms:

Adjustment migations add, remove, or change columns of a specific table. This migration will drop the 'name' column, add two new columns and cast age from an int to a float.

    migrate(new Migration(Person.class) {
        @Remove String name;
        @Add String firstName;
        @Add String lastName;
        @Adjust(old="age") float age;
    });

Table-based migrations can rename and drop tables. The first migration will drop the `person` table, the second will rename a table called 'person' to map to the `Human` class.

    migrate(new Migration(Person.class, Migration.Type.DROP));
    migrate(new Migration("person", Migration.Type.RENAME, Human.class));

The astute will notice that there are no creation migrations - this is because if a table defined in the `tables()` method doesn't exist, it will automatically be created with the correct columns.

### Connections

To access the database you need a `Connection` object. For JDBC SQLite there is the `JDBCConnection` class. When your program starts you should open a new database connection:

    Connection con = new JDBCConnection(MyMigrator.class, "/path/to/sqlite/database.db");
    con.open();

To make `con` the default connection that can be statically accessed from anywhere in your application, call `con.globalize()` - whenever a method that takes a `Connection` as a parameter isn't given one it will fall back to the global database connection.

### Querying

Data can be retrieved from the database using the `Query` class:

    // To get all the people whose name starts with 'John'
    ArrayList<Person> people = new Query(con).where("name LIKE ?", "John%").all();
    // Get the eldest
    Person eldest = new Query(con).order("age DESC").first();
    // Just get the age of the oldest person
    int age = new Query(con).max("age");
    
💾