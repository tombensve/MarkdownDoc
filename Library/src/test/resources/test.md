<!--
    This is a comment!
-->
# Headers

This is the ** headers section of the functional test document.

## Two

This is a paragraph in a level 2 section.

### Three

#### Four

##### Five

###### Six

## Header text
with text on 2 lines ##

Test of underline header H1
===========================

Test of underline header H2
---------------------------


# Code Blocks

Here is a code Block:

    class Paragraph extends DocItem {

        /**
         * Adds an item to the Paragraph.
         *
         * @param docItem The item to add.
         */
        public void add(DocItem docItem) {
            super.items.add(docItem)
        }

        @Override
        public boolean validate() {
            return super.items.size() > 0
        }
    }

End of code block.

# BlockQuote

> This is
> a block
> quoted paragraph.

> This is also
  a block quoted
  paragraph!

> This is a block quoted one line paragraph.

# List

* Unordered list

* Second entry

  * List within list.

  * Second entry sub list

* Third entry orig list.
  This entry has 2 lines.

* Fourth entry orig list.

  This entry has two paragraphs.

  * New sub list uo

  * New sub list l2 uo

1. Ordered list

2. Second entry ol

   1. sub entry ol

   2. sub entry 2 ol

3. Third entry orig ol.

# Horizontal ruler

---

This is a paragraph between 2 horizontal rulers.

---

# Paragraphs

This is a simple one line paragraph.

A paragraph is simply one or more consecutive lines of text, separated by one or more blank lines.
(A blank line is any line that looks like a blank line — a line containing nothing but spaces or
tabs is considered blank.) Normal paragraphs should not be indented with spaces or tabs.

This is a _formatting_ __Paragraph__. Here is [a link](http://to.somewhere.net/). Another [link] to somewhere. This paragraph
also contains a `simple code block`. And here is an ![image](http://groovy.codehaus.org/images/groovy-logo-medium.png).
Some more text in this paragraph. <www.somewhere.net> ends this paragraph.

\* This is not a list since the \'\*\' is escaped!

![Another Image](http://www.natusoft.se/Natusoft/Startsida_files/Skarmavbild%202009-12-12%20kl.%2017.40.40.jpg) ![image2]

[link]: http://to.somewhere.net/ "This links to somewhere on the net"

[image2]: http://groovy.codehaus.org/images/groovy-logo-medium.png

