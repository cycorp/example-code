Cyc API Use Cases
=================

Examples of common [Cyc Core API Suite](https://github.com/cycorp/CycCoreAPI)
usage.

For more information, visit the [Cyc Developer Center](http://dev.cyc.com/).

Requirements
------------

* `JDK 1.6` or greater.
* An EnterpriseCyc or ResearchCyc server running at least system `10.152303`. 
  The current version of the Cyc API suite is _not_ compatible with the latest 
  4.0 OpenCyc release. For inquiries about obtaining a suitable version of
  EnterpriseCyc or ResearchCyc, visit the 
  [Cyc Dev Center download page](http://dev.cyc.com/cyc-api/download.html).
* [Apache Maven](http://maven.apache.org/), version `3.2` or
  higher. If you are new to Maven, you may wish to view the
  [quick start](http://maven.apache.org/run-maven/index.html).
* The [CycCoreAPI](https://github.com/cycorp/CycCoreAPI) suite, version 
  `1.0.0-rc1`.

Getting Started
---------------

This project is intended to demonstrate common usages of the Cyc APIs. It's
built in Maven, and it should be straightforward to open, modify, and run the
examples from any common Java IDE (IntelliJ, NetBeans, Eclipse, etc.)

**Note:** Running these examples will alter a Cyc server's KB contents. These
code samples should not be run against a production system.

Running any code that requires a Cyc server will pop up a GUI panel asking for 
a Cyc server address. You may instead specify a Cyc server by setting the Java 
System property `cyc.session.server` to your server's location, in the format 
`[SOME_HOST_NAME]:[SOME_BASE_PORT]`; e.g., `localhost:3600`. See your IDE's
documentation for details.

You can also run a particular class from the command line. For example, assuming
that you wanted to run the `CoreAPIUsage` class against a Cyc server at
`localhost:3600`, you would issue following command:

    mvn exec:java -Dexec.mainClass="com.cyc.coreapiusecases.CoreAPIUsage" -Dcyc.session.server=localhost:3600

### Logging

If you wish to modify the logging level of the Cyc APIs, you can change the 
settings in `src/main/resources/log4j.properties`.

Knowledge Editing and Ontology Development
------------------------------------------

A Cyc application is only as good as the knowledge in Cyc's KB. Read up on 
[ontology development](http://dev.cyc.com/ontology-development/).

Further Documentation
---------------------

Further API documentation, including Javadocs, is available at 
[Cyc Developer Center](http://dev.cyc.com/cyc-api/).

Contact
-------

For questions about the APIs or issues with using them, please visit the
[Cyc Dev Center issues page](http://dev.cyc.com/cyc-api/issues.html).
