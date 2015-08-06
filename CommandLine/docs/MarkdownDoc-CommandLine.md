# Command Line

## General

MarkdownDoc can be run using `java -jar markdowndoc-cmd-line-n.n[.n]-exec.jar`. If you just run it without any arguments you get the following:

    Usage: java -jar markdowndoc-cmd-line-n.n[.n].exec.jar <generator> --help
           or
           java -jar markdowndoc-cmd-line-n.n[.n].exec.jar <generator> <fileSpec> --<generator option> ...
           or
           java -jar markdowndoc-cmd-line-n.n[.n].exec.jar <generator> <fileSpec> parserOptions:<parserOptions> —-<generator option> ...
           or
           java -jar markdowndoc-cmd-line-n.n[.n].exec.jar <path to a .mddoc file>

The last usage example requires an _.mddoc_ file. See _’The _mddoc_ file type’_ (section 5) for more information on this file type.

What the generator options are depends on the specified generator.

The markdowndoc-cmd-line-n.n\[.n\]-exec.jar is a jar generated to contain all dependencies in the same jar, making it easy to execute with java -jar.

The _\<generator\>_ part should be either _pdf_, _html_, or _md_.

The _\<filespec/\>_ part is a comma separated list of paths relative to the current directory. The filename part of the path can contain regular expressions and the directory part of the path can specify `.../**/...` to mean any levels of subdirectories. 

Example: `root/\*\*/docs/.\*.md`

See "Settings / Options" elsewhere in this document for all the options to the different generators and parsers.
