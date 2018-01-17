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
* A suitable Cyc server.
* One of the following:
    * [Apache Maven](http://maven.apache.org/), version `3.2` or higher. If you are new to Maven,
      you may wish to view the [quick start](http://maven.apache.org/run-maven/index.html).
    * [Apache Ant](http://ant.apache.org/), version `1.9` or higher


Getting Started
---------------

This project is intended to demonstrate common usages of the Cyc APIs. It can be run as either a 
Maven project or an Ant project, and it should be straightforward to open the project and view, 
modify, and run the examples from any common Java IDE (IntelliJ, NetBeans, Eclipse, etc.)

Running any code that requires a Cyc server will pop up a GUI panel asking for a Cyc server address.
You may instead specify a Cyc server by setting the Java System property `cyc.session.server` to 
your server's location, in the format `[HOST_NAME]:[BASE_PORT]`; e.g., 
`cyc.session.server=localhost:3600`. See your IDE's documentation for details.

Warnings and error messages from the Cyc APIs are logged to the console via SLF4J's SimpleLogger.
If you wish to modify the logging configuration for the Cyc APIs, you can change the settings in 
`src/main/resources/simplelogger.properties`.

**Note:** Running these examples will alter a Cyc server's KB contents. These code samples should 
not be run against a production system.


### Maven

To build the project via Maven:

    mvn clean compile

You can then run a particular class from the command line. For example, if you wanted to run the 
`BasicWalkthrough` class against a Cyc server at `localhost:3600`, you would issue the following 
command:

    mvn exec:java -Dexec.mainClass="com.cyc.core.examples.basics.BasicWalkthrough" -Dcyc.session.server=localhost:3600


### Ant

To build the project and run `com.cyc.core.examples.basics.BasicWalkthrough` against a Cyc server
running at `localhost:3600`, you can simply invoke ant:

    ant

To run an existing build of the project:

    ant run

The example class may be overridden with the `exampleClass` property, and the Cyc server location 
may be overridden with the `cyc.session.server` property. For example:

    ant run -DexampleClass=com.cyc.core.examples.impl.KnowledgeManagementExample -Dcyc.session.server=someremotehost:3680


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
