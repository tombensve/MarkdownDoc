# MarkdownDoc

Copyright © 2012 Natusoft AB

__Version:__ 1.2.10  
_Note:_ The previous version (1.2.9) is available in maven central, this is not yet! The only thing new in this version is support for &amp;lt;, &amp;gt;, and &amp;amp;.

__Author:__ Tommy Svensson (tommy@natusoft.se)

----

_A tool for generating HTML and PDF from markdown for the purpose of documentation._

[Introduction](https://github.com/tombensve/MarkdownDoc/blob/master/Docs/MarkdownDoc.md)

[Maven usage](https://github.com/tombensve/MarkdownDoc/blob/master/MavenPlugin/docs/MarkdownDoc-Maven-Plugin.md)

[Command line usage](https://github.com/tombensve/MarkdownDoc/blob/master/CommandLine/docs/MarkdownDoc-CommandLine.md) \[[markdowndoc-cmd-line-1.2.10-exec.jar download](http://download.natusoft.se/tools/markdowndoc-cmd-line-1.2.10-exec.jar)\] 

[Library](https://github.com/tombensve/MarkdownDoc/blob/master/Library/docs/MarkdownDoc-Library.md)

[Editor](https://github.com/tombensve/MarkdownDoc/blob/master/Editor/docs/MarkdownDoc-Editor.md) \[[MarkdownDocEditor-1.2.10-App.jar](http://download.natusoft.se/tools/MarkdownDocEditor-1.2.10-App.jar)\] 

[Licenses](https://github.com/tombensve/MarkdownDoc/blob/master/Docs/licenses.md)

----

[PDF Version](https://github.com/tombensve/MarkdownDoc/blob/master/Docs/MarkdownDoc-User-Guide.pdf)

----

__A note about building:__ "mvn clean install" from root will result in maven-assembly-plugin:2.4 failure complaining about not finding css/docs.css in Markdown-doc-lib-1.2.10.jar, something that is not required in neither the pom nor the assembly descriptor, and on top of that the file __does exist__ within the mentioned jar. Doing "mvn clean install" in each individual module however works fine. Version 2.3 ov maven-assembly-plugin fails completely in all cases. I have not tried earlier versions.
