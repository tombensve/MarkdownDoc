# Simple Markdown Reference

## Headings

    # Heading level 1
    ## Heading level 2
    ...
    ###### Heading level 6

## Paragraphs

An empty line marks the end of a paragraph. 

    Paragraph 1 ...
    More text in paragraph 1.

    Paragraph 2 ...

Paragraph 1 ...
More text in paragraph 1.

Paragraph 2 ...

##  Italics

    _This is in italics_

    This *is also italics* but can't start a line with * since it will be treated as list.

_This is in italics_

This *is also italics* but can't start a line with * since it will be treated as list.

## Bold

    __This text is bold.__

    **This text is also bold.**

__This text is bold.__

**This text is also bold.**

## Blockquote

    > This line is block quoted.

> This line is block quoted.

## Lists

### Unordered lists (* or -)

    * item 1
      Also part of item 1.

    - item 2
        * item 2.1 (this is indented 4 spaces!)

* item 1
  Also part of item 1.

* item 2
    * item 2.1 (this is indented 4 spaces!)

### Ordered list (n.)

    1. item 1.
    2. item 2.
        1. item 2.1

1. item 1.
2. item 2.
    1. item 2.1

Please note that the actual numbers does not matter! They could all be "1."! The items will be enumerated automatically in order no matter what numbers you enter in the source.

## Code block (pre formatted with a fixed width font)

Each line starting with a tab or 4 spaces are considered belonging to a pre formatted block.

    This
        is
            a
                preformatted
                    block!

## Horizontal rule

Any of:

    * * *
    ***
    ********...***
    - - -
    ----
    ---------...---


## Links

    [This is a link to markdown syntax on daringfireball.net](http://daringfireball.net/projects/markdown/syntax)

[This is a link to markdown syntax on daringfireball.net](http://daringfireball.net/projects/markdown/syntax)

Short "autolink" version:

    <http://www.daringfireball.net>

<http://www.daringfireball.net>

## Images

    ![Alt text](/path/to/img.png)

    ![Alt text](/path/to/img.png "title")

## backslash (\)

The \\ character can be used to escape characters that have markdown meaning. \\\\ will for example produce \\. \\\* will produce \*.
