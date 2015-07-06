package se.natusoft.doc.markdown.util

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

/**
 * Provide resources that works:
 *
 * <ul>
 *     <li>Runtime</li>
 *     <li>In maven run JUnit test</li>
 *     <li>In IDEA run JUnit test</li>
 * </ul>
 *
 * That this is needed is bloody annoying!!
 */
@CompileStatic
@TypeChecked
class TestSafeResource {

    static @Nullable InputStream getResource(@NotNull String path) {

        InputStream is = TestSafeResource.class.getClassLoader().getResourceAsStream(path)
        if (is == null) {
            is = TestSafeResource.class.getClassLoader().getResourceAsStream("src/main/resources/" + path)
        }
        if (is == null) {
            is = TestSafeResource.class.getClassLoader().getResourceAsStream("Library/src/main/resources/" + path)
        }

        is
    }
}
