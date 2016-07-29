package se.natusoft.doc.markdown.generator.pdfbox.internal

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * This represents the information on a front page.
 */
@CompileStatic
@TypeChecked
class FrontPage {

    //
    // Properties
    //

    String title
    String subTitle
    String version
    String imagePath
    String Author
    String copyright
    int year

}
