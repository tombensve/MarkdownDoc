package se.natusoft.doc.markdown.generator.utils

/**
 * If the connection is http and the connection fails then the
 * URL is recreated replacing http with https and tried again.
 *
 * URLs are passed to lower APIs that does not seem to be able to
 * upgrade an http to an https when the latter is required.
 */
class HttpsUpgradingURL {

    //
    // Private Members
    //

    /** Our wrapped URL. */
    private URL url

    //
    // Constructors
    //

    /**
     * Creates a new SmartUrl.
     */
    private HttpsUpgradingURL(URL url) {
        this.url = url
    }

    /**
     * Creates a new SmartUrl from an URL instance.
     *
     * @param url The URL instance to use.
     */
    static HttpsUpgradingURL fromURL( URL url) {
        new HttpsUpgradingURL(url)
    }

    /**
     * Creates a new SmartUrl from a string.
     *
     * @param url The url to use in string format.
     */
    static HttpsUpgradingURL fromString( String url) {
        new HttpsUpgradingURL(new URL(url))
    }

    //
    // Members
    //

    /**
     * Opens an InputStream to the URL for reading its content. If the first 100 bytes contains "301"
     * then it is assumed that the URL was an http URL and http is then replaced with https, and then
     * the connection is tried again.
     *
     * @return an InputStream to URL.
     */
    InputStream openStream() throws IOException {

        HttpURLConnection urlConnection

        urlConnection = this.url.openConnection() as HttpURLConnection
        urlConnection.setRequestMethod( "GET" )
        urlConnection.connect(  )
        int respCode = urlConnection.getResponseCode(  )

        if (respCode == 301) {

            urlConnection.disconnect()

            if ( this.url.protocol.toLowerCase() == "http" ) {

                System.err.print( "HttpsUpgradingURL(${this.url.toString(  )}): Got 301! trying https ... " )

                this.url = new URL( this.url.toString().replace( "http", "https" ) )

                urlConnection = this.url.openConnection() as HttpURLConnection
                urlConnection.setRequestMethod( "GET" )
                urlConnection.connect()

                if (urlConnection.getResponseCode(  ) != 200) {
                    System.err.println("failed!")
                    throw new IOException("Tried to upgrade to https, but no go! Got: " + urlConnection.getResponseCode(  ))
                }

                System.err.println("successful!")
            }
            else {
                throw new IOException("Connection failed! Respnsecode: ${urlConnection.getResponseCode(  )}")
            }
        }

        urlConnection.getInputStream(  ) // Lets hope this also closes the UrlConnection ...
    }
}
