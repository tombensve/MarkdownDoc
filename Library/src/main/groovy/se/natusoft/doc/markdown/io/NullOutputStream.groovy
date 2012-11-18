package se.natusoft.doc.markdown.io

/**
 * This is an OutputStream that writes nothing to nowhere!
 */
class NullOutputStream extends OutputStream {

    @Override
    void write(int b) {}
}
