![](https://download.natusoft.se/Images/MarkdownDoc/MarkdownDoc.png)

Copyright (C) 2012 Natusoft AB

__Version:__ 3.1.0

__Author:__ Tommy Svensson (tommy@natusoft.se)

![ci_status](https://github.com/tombensve/MarkdownDoc/actions/workflows/maven.yml/badge.svg?branch=master)

----

_A tool for generating HTML and PDF from markdown for the purpose of documentation._

[User Guide](https://github.com/tombensve/MarkdownDoc/blob/master/Docs/MarkdownDoc-User-Guide.md)

[User Guide PDF](https://github.com/tombensve/MarkdownDoc/blob/master/Docs/MarkdownDoc-User-Guide.pdf)

# Binaries

Binaries are for the moment available at https://download.natusoft.se/maven.

See [Maven repo setup](https://github.com/tombensve/CommonStuff/blob/master/docs/MavenRepository.md)

# JDK version support

MarkdownDoc is now upgraded to Groovy 4.0.1 producing JDK 11 bytecode by default. 

It is now also, since Groovy 4, possible to specify which level of bytecode to generate by groovy code. Currently 11 is specified: `<byte-code>11</byte-code>` in top pom. I have tested JDK 17 also, and it works fine. 

In my maven repo (see above) there is also a 3.1.0_17 version available. This is 
compiled to JDK 17 bytecode.

Everything but the maven plugin is now done in Groovy. This so that when the byte code version specified in `<byte-code>11</byte-code>` in the root POM is changed and an `mvn clean install` is done, then all code will be compiled to that byte code level.

# Licenses


