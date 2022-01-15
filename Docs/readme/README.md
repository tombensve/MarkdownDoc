![](https://download.natusoft.se/Images/MarkdownDoc/MarkdownDoc.png)

Copyright (C) 2012 Natusoft AB

__Version:__ 3.0.1

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

MarkdownDoc is written in Groovy 3.0 which produces JDK 1.8 compatible bytecode. In JDK 11 it still works, but you get this warning:

    WARNING: Illegal reflective access by org.codehaus.groovy.vmplugin.v9.Java9 ...

When run with openjdk version "11.0.9.1" 2020-11-04 LTS.

This is a groovy problem I have no control over. There seem to be a groovy 4.x now, which I'll consider for the
next version.

As I said above, MarkdownDoc is written in Groovy! The current version produces Java 8 bytecode. It is Groovy (org.codehaus.groovy.reflectionCachedClass) that produces a warning in Java 11. It will not work on Java 12, and there is nothing I can do about that until Groovy has a Java 12 supporting version. 

<https://dzone.com/articles/java-8-bastion-of-long-term-support>

# Licenses


