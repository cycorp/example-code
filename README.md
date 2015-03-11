Cyc Core API Use Cases
======================

This project provides examples of common usage of the 
[Cyc Core API Suite](https://github.com/cycorp/CycCoreAPI).

It is generally recommended that you download the latest release of the Core API Use Cases from the
[releases page](https://github.com/cycorp/CoreAPIUseCases/releases), as it will rely on the latest
_released version_ of the Core APIs.

For more information, visit the [Cyc Developer Center](http://dev.cyc.com/).

Requirements
------------

### Java

* `JDK 1.6` or greater.
* [Apache Maven](http://maven.apache.org/), version `3.2` or higher. If you are new to Maven, you 
  may wish to view the [quick start](http://maven.apache.org/run-maven/index.html).

### Cyc Core API Suite

You have two options:

If you are using a [tagged release](https://github.com/cycorp/CoreAPIUseCases/releases) of the Core
API Use Cases project, Maven will automatically download and install appropriate versions of the 
Core API libraries for you.

If you are using the latest version of the Use Cases from the 
[git repository](https://github.com/cycorp/CoreAPIUseCases), then you will need to manually build
and install the latest \*-SNAPSHOT version of the [Core APIs](https://github.com/cycorp/CycCoreAPI)
from source.

### Cyc Server

These examples are intended to be run against **ResearchCyc 4.0q** or higher.

All of the code demonstrated here is compatible with **EnterpriseCyc 1.7-preview** or higher, but 
note that, by design, EnterpriseCyc does not contain the _KB content_ necessary to run these 
specific examples.

The Core API Suite and these examples are _not_ presently compatible with any current release of 
**OpenCyc.**

For inquiries about obtaining a suitable version of Research Cyc or Enterprise Cyc, please visit the
[Cyc Dev Center download page](http://dev.cyc.com/cyc-api/download.html).


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

You can also run a particular class from the command line. For example, assuming that you wanted to 
run the `CoreAPIUsage` class against a Cyc server at `localhost:3600`, you would issue following 
command:

    mvn exec:java -Dexec.mainClass="com.cyc.coreapiusecases.CoreAPIUsage" -Dcyc.session.server=localhost:3600

### Logging

If you wish to modify the logging level of the Cyc APIs, you can change the settings in 
`src/main/resources/log4j.properties`.

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
