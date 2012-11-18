package se.natusoft.doc.markdown.model

/**
 * This represents a comment.
 */
class Comment extends PlainText {

    /**
     * Returns the format this model represents.
     */
    @Override
    public DocFormat getFormat() {
        return DocFormat.Comment
    }

}
