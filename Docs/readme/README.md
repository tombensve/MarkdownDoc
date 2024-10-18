![](https://download.natusoft.se/Images/MarkdownDoc/MarkdownDoc.png)

Copyright (C) 2012 Natusoft AB

__Version:__ 3.1.11

__Author:__ Tommy Svensson (tommy@natusoft.se)

The below CI status seem to have some problems. It builds perfectly fine for me. 
Right now I don't have the time to look into this.

![ci_status](https://github.com/tombensve/MarkdownDoc/actions/workflows/maven.yml/badge.svg?branch=master)

----

_A tool for generating HTML and PDF from markdown for the purpose of documentation._

There are no new changes to the code! There is however a change in versions to adapt
to my new personal version number standard: 

    major.minor.bytecode 

[User Guide](https://github.com/tombensve/MarkdownDoc/blob/master/Docs/MarkdownDoc-User-Guide.md)

[User Guide PDF](https://github.com/tombensve/MarkdownDoc/blob/master/Docs/MarkdownDoc-User-Guide.pdf)

# Binaries

Binaries are available at:

    <repositories>

        <repository>
            <id>repsy</id>
            <name>My Private Maven Repository on Repsy</name>
            <url>https://repo.repsy.io/mvn/tombensve/natusoft-os</url>
        </repository>

    </repositories>

    <pluginRepositories>

        <pluginRepository>
            <id>repsy</id>
            <name>repsy</name>
            <url>https://repo.repsy.io/mvn/tombensve/natusoft-os</url>
            <releases>
                <enabled>true</enabled>
            </releases>
        </pluginRepository>

    </pluginRepositories>

This is a free repo (for open source binaries). This will remain after I no longer can do this.

My old repo at my web server is already gone.

# JDK version support

MarkdownDoc is now upgraded to Groovy 4.0.1 producing JDK 11 bytecode by default. 

It is now also, since Groovy 4, possible to specify which level of bytecode to generate by groovy code. Currently 11 is specified: `<byte-code>11</byte-code>` in top pom. I have tested JDK 17 also, and it works fine. 

In my maven repo (see above) there is also a 3.1.0_17 version available. This is 
compiled to JDK 17 bytecode.

Everything but the maven plugin is now done in Groovy. This so that when the byte code version specified in `<byte-code>11</byte-code>` in the root POM is changed and an `mvn clean install` is done, then all code will be compiled to that byte code level.

# Licenses
