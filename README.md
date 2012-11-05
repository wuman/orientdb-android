ORIENTDB-ANDROID
================

OrientDB-Android is a port/fork of [OrientDB](http://www.orientdb.org/) for the
Android platform.

Currently the port removes all server implementations as they use the
`javax.management.*` package, which is not part of the Android classpath.
The port is designed to be used in conjunction with the 
[blueprints-android](https://github.com/wuman/blueprints-android) library.

Currently only the following modules are ported:

* orient-android-commons
* orientdb-android-core
* orientdb-android-enterprise
* orientdb-android-client
* orientdb-android-nativeos
* orientdb-android-object
* orientdb-android-tools
* orientdb-android-distribution

Note that orientdb-android is still under development and  only 
`orient-android-commons` and `orientdb-android-core` have been used and tested 
in the wild, so use it at your own risk. We welcome contributions and feedbacks.

The current release of orientdb-android is 1.1.0.0, which is in line with version
1.1.0 of the upstream OrientDB. 


Including in Your Project
-------------------------

There are two ways to include orientdb-android in your projects:

1. You can download the released jar file in the [Downloads section](https://github.com/wuman/orientdb-android/downloads).
2. If you use Maven to build your project you can simply add a dependency to 
   the desired component of the library.

        <dependency>
            <groupId>com.wu-man</groupId>
            <artifactId>orient[db]-android-*</artifactId>
            <version>1.1.0.0</version>
        </dependency>


Introduction to OrientDB
------------------------

[OrientDB](http://code.google.com/p/orient/) is an open source 
[NoSQL](http://en.wikipedia.org/wiki/NoSQL) DBMS with both the features of 
Document and Graph DBMS. It's written in Java and it's amazing fast: it can store 
up to 150,000 records per second on common hardware.

OrientDB is in its core a [graph database](http://en.wikipedia.org/wiki/Graph_database),
although it also offers the interface of a document database. When used a 
document database, the relationships are managed as in graph databases with 
direct connections among records. You can traverse entire or part of trees and 
graphs of records in few milliseconds.

To see how OrientDB compares with other databases, take a look at

* [GraphDB comparison](http://code.google.com/p/orient/wiki/GraphDBComparison)
* [DocumentDB comparison](http://code.google.com/p/orient/wiki/DocumentDBComparison)


Contribute
----------

If you would like to contribute code you can do so through GitHub by forking 
the repository and sending a pull request.


Developed By
------------

* Android porting contributor
    * David Wu - <david@wu-man.com> - [http://blog.wu-man.com](http://blog.wu-man.com)
* Original contributors to OrientDB
    * Luca Garulli - <l.garulli@orientechnologies.com> - http://www.orientechnologies.com
    * Luca Molino - <molino.luca@gmail.com>
    * Andrey Lomakin - <lomakin.andrey@gmail.com> 


License
-------

    Copyright 2012 David Wu
    Copyright 2010-2012 Luca Garulli (l.garulli--at--orientechnologies.com)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

