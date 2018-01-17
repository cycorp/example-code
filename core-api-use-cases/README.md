Cyc Core API Use Cases
======================

This project provides examples of common usage of the 
[Cyc Core API Suite](https://github.com/cycorp/api-suite).

Examples are grouped into three difference packages:

* `com.cyc.core.examples.basics`   - Basic usage of common features.
* `com.cyc.core.examples.advanced` - Advanced features.
* `com.cyc.core.examples.impl`     - Some features currently only in the Core Client implementation.


Requirements
------------

* `JDK 1.8` or greater.
* [Apache Maven](http://maven.apache.org/), version `3.2` or higher. If you are new to Maven, you 
  may wish to view the [quick start](http://maven.apache.org/run-maven/index.html).
* A suitable Cyc server.


Getting Started
---------------

This project is intended to demonstrate common usages of the Cyc APIs. It's built in Maven, and it 
should be straightforward to open, modify, and run the examples from any common Java IDE (IntelliJ, 
NetBeans, Eclipse, etc.)

**Note:** Running these examples will alter a Cyc server's KB contents. These code samples should 
not be run against a production system.

Running any code that requires a Cyc server will pop up a GUI panel asking for a Cyc server address.
You may instead specify a Cyc server by setting the Java System property `cyc.session.server` to 
your server's location, in the format `[HOST_NAME]:[BASE_PORT]`; e.g., 
`cyc.session.server=localhost:3600`. See your IDE's documentation for details.

You can also run a particular class from the command line. First, build the project:

    mvn clean compile

Then, for example, if you wanted to run the `BasicWalkthrough` class against a Cyc server at 
`localhost:3600`, you would issue the following command:

    mvn exec:java -Dexec.mainClass="com.cyc.core.examples.basics.BasicWalkthrough" -Dcyc.session.server=localhost:3600

### Standalone build

In a typical Maven project, the jar files for all transitive dependencies are stored in a local
repository (typically in the userdir under `~/.m2`), with Maven retrieving them from a repository
server (up to and including the Maven Central Repository) if necessary. Of course, this is not 
always desirable (in some secured environments, this may not even be _possible_) so this project 
comes bundled with all of its dependencies (in the `lib` directory) which can be used in an optional
"standalone" mode. To use it, add the `-P standalone` flag. E.g.:

    mvn clean compile -P standalone

However, note that Apache Maven can require dependencies for _itself_ (to provide plugins for its 
build system). As these jars are expected in any standard Maven installation, they are not included
in this project.

### Logging

Warning and error messages from the Cyc APIs are logged to the console via SLF4J's SimpleLogger.
If you wish to modify the logging configuration for the Cyc APIs, you can change the settings in 
`src/main/resources/simplelogger.properties`.


Knowledge Editing and Ontology Development
------------------------------------------

A Cyc application is only as good as the knowledge in Cyc's KB. Read up on 
[ontology development](http://dev.cyc.com/ontology-development/).


Further Documentation
---------------------

Further API documentation, including Javadocs, is available at 
[Cyc Developer Center](http://dev.cyc.com/api/).


Contact
-------

For questions about the APIs or issues with using them, please visit the 
[Cyc Dev Center issues page](http://dev.cyc.com/issues/).
