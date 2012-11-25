package se.natusoft.doc.markdowndoc;

import junit.framework.TestCase;

import java.io.File;
import java.net.URL;

public class SourcePathsTest extends TestCase {

    public void testSourcePaths() throws Exception {

        String spec ="\n" +
                "                                    MarkdownDoc.md,\n" +
                "                                    ../Library/docs/.*.md,\n" +
                "                                    ../MavenPlugin/docs/.*.md,\n" +
                "                                    ../CommandLine/docs/.*.md,\n" +
                "                                    licenses.md,\n" +
                "                                    .*-.*.md\n" +
                "                                ";
        SourcePaths sourcePaths = new SourcePaths(new File("/Volumes/PROMISE_PEGASUS/Development/projects/Tools/MarkdownDoc/Docs"), spec);
        for (File file : sourcePaths.getSourceFiles()) {
            System.out.println(file.toString());
        }
    }
}
