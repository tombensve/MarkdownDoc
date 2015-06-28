package se.natusoft.doc.markdown.model

/**
 * This represents a div.
 */
class Div extends DocItem {
    //
    // Properties
    //

    String name

    //
    // Methods
    //

    /**
     * This overrides the same method in DocItem and returns the name of this div rather than
     * passing it upp to the parent. So any children of a div will get the div name on getDivName().
     * <p/>
     * If the div have any divs in parent chain they will also be included first with each div separated
     * by ':'.
     */
    @Override
    String getDivName() {
        String div = ""
        if (super.divName != null) {
            div = super.divName + ":"
        }
        div + this.name
    }
}
