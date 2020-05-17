![](http://download.natusoft.se/Images/MarkdownDoc/MarkdownDoc.png)

Copyright (C) 2012 Natusoft AB

__Version:__ 2.1.3

__Author:__ Tommy Svensson (tommy@natusoft.se)

----

_A tool for generating HTML and PDF from markdown for the purpose of documentation._

[User Guide](https://github.com/tombensve/MarkdownDoc/blob/master/Docs/MarkdownDoc-User-Guide.md)

[User Guide PDF](https://github.com/tombensve/MarkdownDoc/blob/master/Docs/MarkdownDoc-User-Guide.pdf)

# Binaries

Available through maven at bintray JCentral: [http://jcenter.bintray.com/](http://jcenter.bintray.com/).
MarkdownDoc on [Bintray](https://bintray.com/tommy/maven/MarkdownDoc/).

Command line \[[markdowndoc-cmd-line-2.1.3.exec.jar](http://dl.bintray.com/tommy/maven/se/natusoft/tools/doc/markdowndoc/markdowndoc-cmd-line/2.1.3/markdowndoc-cmd-line-2.1.3.exec.jar)\]


Editor \[[MarkdownDocEditor-2.1.3.App.jar](http://dl.bintray.com/tommy/maven/se/natusoft/tools/doc/markdowndoc/MarkdownDocEditor/2.1.3/MarkdownDocEditor-2.1.3.App.jar)\].

[Maven repo setup](https://github.com/tombensve/CommonStuff/blob/master/docs/MavenRepository.md)

# JDK version support

MarkdownDoc is written in Groovy 2.5 which produces JDK 5 compatible bytecode. In JDK 11 it still works, but you get the warning. If I fix this, then MarkdownDoc will not run on anything less than JDK 11 (maybe 10). I think a lot of people still is using JDK 8 due to the crazyness of 9+ (using modules requires ALL other dependencies to be modules also, that is 9+ and modularized!! Few of the third party libraries used by many are modularized, sticking you between a rock and a hard place), and as of JDK 12 there is a guaranteed backwards incompatibility. You need 2 versions of code one for \<9 and one for \>=9. Java does not have a preprocessor so no #ifdef ! You need to keep 2 different versions of the code. With GIT it would be possible to have \<9 branches and \>=9 branches, but it can get messy.

I have found a project that does an attempt at some form of remedy for this: <https://github.com/moditect/moditect>. It requires a lot of config and intimate knowledge of all 3rd party libs you need to convert to 9+ modules.

There is a catch to Moditect: It updates 3rd party jars with a module-info! Note that dependency jars contain compiled code! So if it is JDK 8 compiled code ? If so even with a module-info it is likely to crash due to bytecode not supported on 12+. 

Maybe it is possible to avoid code that differs in <12 & 12+, but since you don't have control over 3rd party libraries you have to write all code your self, and not use third party libraries. There seem to be only stupid/annoying solutions to the problem. 

As I said above, MarkdownDoc is written in Groovy! The current version produces Java 5 bytecode. If I bring Groovy version up to 3.0 then it will produce Java 8 bytecode. It is Groovy (org.codehaus.groovy.reflectionCachedClass) that produces a warning in Java 11. It will not work on Java 12, and there is nothing I can do about that until Groovy has a Java 12 supporting version. Or I need to port it to Java, but that does not of course automatically solve everything. There is a reason I have chosen Groovy as language, and it wasn't just for the heck of it!

Personally I'm a bit scared for the future of Java. In Sweden I have still not seen any company using anything higher than 8. There probably are, but the majority is still sticking to 8.

<https://dzone.com/articles/java-8-bastion-of-long-term-support>

# Licenses


