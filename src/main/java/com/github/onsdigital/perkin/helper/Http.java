package com.github.onsdigital.perkin.helper;

import com.github.davidcarboni.httpino.Endpoint;
import com.github.davidcarboni.httpino.Response;
import com.github.davidcarboni.httpino.Serialiser;
import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

/**
 * Simplified Http client, providing for common operations.
 */
@Slf4j
public class Http implements AutoCloseable {

    protected CloseableHttpClient httpClient;
    protected ArrayList<Header> headers = new ArrayList<>();

    /**
     * Sends a GET request and returns the response.
     *
     * @param endpoint      The endpoint to send the request to.
     * @param responseClass The class to deserialise the Json response to. Can be null if no response message is expected.
     * @param headers       Any additional headers to send with this request. You can use {@link org.apache.http.HttpHeaders} constants for header names.
     * @param <T>           The type to deserialise the response to.
     * @return A {@link com.github.davidcarboni.httpino.Response} containing the deserialised body, if any.
     * @throws java.io.IOException If an error occurs.
     */
    public <T> Response<T> getJson(Endpoint endpoint, Class<T> responseClass, NameValuePair... headers) throws IOException {

        // Create the request
        HttpGet get = new HttpGet(endpoint.url());
        get.setHeaders(combineHeaders(headers));


        // Send the request and process the response
        try (CloseableHttpResponse response = httpClient().execute(get)) {
            //System.out.println(response);
            T body = deserialiseResponseMessage(response, responseClass);
            return new Response<>(response.getStatusLine(), body);
        }
    }

    /**
     * Sends a GET request and returns the response.
     *
     * @param endpoint The endpoint to send the request to.
     * @param type     The type to deserialise the Json response to. Can be null if no response message is expected.
     * @param headers  Any additional headers to send with this request. You can use {@link org.apache.http.HttpHeaders} constants for header names.
     * @param <T>      The type to deserialise the response to.
     * @return A {@link Response} containing the deserialised body, if any.
     * @throws IOException If an error occurs.
     */
    public <T> Response<T> getJson(Endpoint endpoint, Type type, NameValuePair... headers) throws IOException {

        // Create the request
        HttpGet get = new HttpGet(endpoint.url());
        get.setHeaders(combineHeaders(headers));


        // Send the request and process the response
        try (CloseableHttpResponse response = httpClient().execute(get)) {
            //System.out.println(response);
            T body = deserialiseResponseMessage(response, type);
            return new Response<>(response.getStatusLine(), body);
        }
    }

    /**
     * Sends a GET request and returns the response.
     *
     * @param endpoint The endpoint to send the request to.
     * @param headers  Any additional headers to send with this request. You can use {@link org.apache.http.HttpHeaders} constants for header names.
     * @return A {@link java.nio.file.Path} to the downloaded content, if any.
     * @throws IOException If an error occurs.
     * @see java.nio.file.Files#probeContentType(java.nio.file.Path)
     */
    public Response<Path> getFile(Endpoint endpoint, NameValuePair... headers) throws IOException {

        // Create the request
        HttpGet get = new HttpGet(endpoint.url());
        get.setHeaders(combineHeaders(headers));
        Path tempFile = null;

        // Send the request and process the response
        try (CloseableHttpResponse response = httpClient().execute(get)) {

            // Request the content
            HttpEntity entity = response.getEntity();

            // Download the content to a temporary file
            if (entity != null) {
                tempFile = Files.createTempFile("download", "file");
                try (InputStream input = entity.getContent();
                     OutputStream output = Files.newOutputStream(tempFile)) {
                    IOUtils.copy(input, output);
                }
            }

            return new Response<>(response.getStatusLine(), tempFile);
        }
    }

    /**
     * Sends a POST request and returns the response.
     *
     * @param endpoint       The endpoint to send the request to.
     * @param requestMessage A message to send in the request body. Can be null.
     * @param headers        Any additional headers to send with this request. You can use {@link org.apache.http.HttpHeaders} constants for header names.
     * @return A {@link Response} containing the deserialised body, if any.
     * @throws IOException If an error occurs.
     */
    public Response<String> postJson(Endpoint endpoint, Object requestMessage, NameValuePair... headers) throws IOException {
        if (requestMessage == null) {
            return post(endpoint, headers);
        } // deal with null case

        // Create the request
        HttpPost post = new HttpPost(endpoint.url());
        post.setHeaders(combineHeaders(headers));

        // Add the request message if there is one
        log.debug("HTTP|request: {}", requestMessage);
        post.setEntity(serialiseRequestMessage(requestMessage));

        // Send the request and process the response
        try (CloseableHttpResponse response = httpClient().execute(post)) {
            String body = deserialiseResponseMessage(response);
            log.debug("HTTP|response body: {}", body);
            return new Response<>(response.getStatusLine(), body);
        }
    }

    public Response<String> postString(Endpoint endpoint, String requestMessage, NameValuePair... headers) throws IOException {
        if (requestMessage == null) {
            return post(endpoint, headers);
        } // deal with null case

        // Create the request
        HttpPost post = new HttpPost(endpoint.url());
        post.setHeaders(combineHeaders(headers));

        // Add the request message if there is one
        log.debug("HTTP|request: {}", requestMessage);
        post.setEntity(new StringEntity(requestMessage));

        // Send the request and process the response
        try (CloseableHttpResponse response = httpClient().execute(post)) {
            String body = deserialiseResponseMessage(response);
            log.debug("HTTP|response body: {}", body);
            return new Response<>(response.getStatusLine(), body);
        }
    }

    /**
     * Sends a POST request and returns the response.
     *
     * Specifically for the use case where we have no requestMessage
     *
     * @param endpoint      The endpoint to send the request to.
     * @param responseClass The class to deserialise the Json response to. Can be null if no response message is expected.
     * @param headers       Any additional headers to send with this request. You can use {@link org.apache.http.HttpHeaders} constants for header names.
     * @param <T>           The type to deserialise the response to.
     * @return A {@link Response} containing the deserialised body, if any.
     * @throws IOException If an error occurs.
     */
    public <T> Response<T> post(Endpoint endpoint, Class<T> responseClass, NameValuePair... headers) throws IOException {

        // Create the request
        HttpPost post = new HttpPost(endpoint.url());
        post.setHeaders(combineHeaders(headers));

        // Add the request message if there is one
        post.setEntity(serialiseRequestMessage(null));

        // Send the request and process the response
        try (CloseableHttpResponse response = httpClient().execute(post)) {
            T body = deserialiseResponseMessage(response, responseClass);
            return new Response<>(response.getStatusLine(), body);
        }
    }

    public Response<String> post(Endpoint endpoint, NameValuePair... headers) throws IOException {

        // Create the request
        HttpPost post = new HttpPost(endpoint.url());
        post.setHeaders(combineHeaders(headers));

        // Add the request message if there is one
        post.setEntity(serialiseRequestMessage(null));

        // Send the request and process the response
        try (CloseableHttpResponse response = httpClient().execute(post)) {
            String body = deserialiseResponseMessage(response);
            return new Response<>(response.getStatusLine(), body);
        }
    }

    public Response<String> post(Endpoint endpoint, InputStream inputStream, String filename, NameValuePair... fields) throws IOException {

        // Create the request
        HttpPost post = new HttpPost(endpoint.url());

        // Add fields as text pairs
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        for (NameValuePair field : fields) {
            multipartEntityBuilder.addTextBody(field.getName(), field.getValue());
        }

        InputStreamBody body = new InputStreamBody(inputStream, filename);
        multipartEntityBuilder.addPart("file", body);

        // Set the body
        post.setEntity(multipartEntityBuilder.build());

        // Send the request and process the response
        try (CloseableHttpResponse response = httpClient().execute(post)) {
            String responseBody = deserialiseResponseMessage(response);
            return new Response<>(response.getStatusLine(), responseBody);
        }
    }

    /**
     * Sends a POST request with a file and returns the response.
     *
     * @param endpoint      The endpoint to send the request to.
     * @param path          The file to upload
     * @param responseClass The class to deserialise the Json response to. Can be null if no response message is expected.
     * @param fields        Any name-value pairs to serialise
     * @param <T>           The type to deserialise the response to.
     * @return A {@link Response} containing the deserialised body, if any.
     * @throws IOException If an error occurs.
     * @see MultipartEntityBuilder
     */
    public <T> Response<T> postFile(Endpoint endpoint, Path path, Class<T> responseClass, NameValuePair... fields) throws IOException {

        // Create the request
        HttpPost post = new HttpPost(endpoint.url());
        post.setHeaders(combineHeaders());

        // Add fields as text pairs
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        for (NameValuePair field : fields) {
            multipartEntityBuilder.addTextBody(field.getName(), field.getValue());
        }
        // Add file as binary
        FileBody bin = new FileBody(path.toFile());
        multipartEntityBuilder.addPart("file", bin);

        // Set the body
        post.setEntity(multipartEntityBuilder.build());

        // Send the request and process the response
        try (CloseableHttpResponse response = httpClient().execute(post)) {
            T body = deserialiseResponseMessage(response, responseClass);
            return new Response<>(response.getStatusLine(), body);
        }
    }

    /**
     * Sends a POST request with a file and returns the response.
     *
     * @param endpoint      The endpoint to send the request to.
     * @param input         The file data to upload
     * @param responseClass The class to deserialise the Json response to. Can be null if no response message is expected.
     * @param fields        Any name-value pairs to serialise
     * @param <T>           The type to deserialise the response to.
     * @return A {@link Response} containing the deserialised body, if any.
     * @throws IOException If an error occurs.
     * @see MultipartEntityBuilder
     */
    public <T> Response<T> postFile(Endpoint endpoint, InputStream input, Class<T> responseClass, NameValuePair... fields) throws IOException {

        // Create the request
        HttpPost post = new HttpPost(endpoint.url());
        post.setHeaders(combineHeaders());

        // Add fields as text pairs
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        for (NameValuePair field : fields) {
            multipartEntityBuilder.addTextBody(field.getName(), field.getValue());
        }
        // Add file as binary
        Path tempFile = Files.createTempFile("upload", ".dat");
        try (OutputStream output = Files.newOutputStream(tempFile)) {
            IOUtils.copy(input, output);
        }
        FileBody bin = new FileBody(tempFile.toFile());
        multipartEntityBuilder.addPart("file", bin);

        // Set the body
        post.setEntity(multipartEntityBuilder.build());

        // Send the request and process the response
        try (CloseableHttpResponse response = httpClient().execute(post)) {
            T body = deserialiseResponseMessage(response, responseClass);
            return new Response<>(response.getStatusLine(), body);
        }
    }

    /**
     * Sends a POST request with a file and returns the response.
     *
     * @param endpoint      The endpoint to send the request to.
     * @param file          The file to upload
     * @param responseClass The class to deserialise the Json response to. Can be null if no response message is expected.
     * @param <T>           The type to deserialise the response to.
     * @return A {@link Response} containing the deserialised body, if any.
     * @throws IOException If an error occurs.
     * @see MultipartEntityBuilder
     */
    public <T> Response<T> post(Endpoint endpoint, File file, Class<T> responseClass) throws IOException {
        if (file == null) {
            return post(endpoint, responseClass);
        } // deal with null case

        // Create the request
        HttpPost post = new HttpPost(endpoint.url());
        post.setHeaders(combineHeaders());

        // Add fields as text pairs
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        // Add file as binary
        FileBody bin = new FileBody(file);
        multipartEntityBuilder.addPart("file", bin);

        // Set the body
        post.setEntity(multipartEntityBuilder.build());

        // Send the request and process the response
        try (CloseableHttpResponse response = httpClient().execute(post)) {
            T body = deserialiseResponseMessage(response, responseClass);
            return new Response<>(response.getStatusLine(), body);
        }
    }

    /**
     * Sends a POST request and returns the response.
     *
     * @param endpoint       The endpoint to send the request to.
     * @param requestMessage A message to send in the request body. Can be null.
     * @param responseClass  The class to deserialise the Json response to. Can be null if no response message is expected.
     * @param headers        Any additional headers to send with this request. You can use {@link org.apache.http.HttpHeaders} constants for header names.
     * @param <T>            The type to deserialise the response to.
     * @return A {@link Response} containing the deserialised body, if any.
     * @throws IOException If an error occurs.
     */
    public <T> Response<T> put(Endpoint endpoint, Object requestMessage, Class<T> responseClass, NameValuePair... headers) throws IOException {

        // Create the request
        HttpPut put = new HttpPut(endpoint.url());
        put.setHeaders(combineHeaders(headers));

        // Add the request message if there is one
        put.setEntity(serialiseRequestMessage(requestMessage));

        // Send the request and process the response
        try (CloseableHttpResponse response = httpClient().execute(put)) {
            T body = deserialiseResponseMessage(response, responseClass);
            return new Response<>(response.getStatusLine(), body);
        }
    }

    /**
     * Sends a POST request and returns the response.
     *
     * @param endpoint      The endpoint to send the request to.
     * @param responseClass The class to deserialise the Json response to. Can be null if no response message is expected.
     * @param headers       Any additional headers to send with this request. You can use {@link org.apache.http.HttpHeaders} constants for header names.
     * @param <T>           The type to deserialise the response to.
     * @return A {@link Response} containing the deserialised body, if any.
     * @throws IOException If an error occurs.
     */
    public <T> Response<T> delete(Endpoint endpoint, Class<T> responseClass, NameValuePair... headers) throws IOException {

        // Create the request
        HttpDelete delete = new HttpDelete(endpoint.url());
        delete.setHeaders(combineHeaders(headers));

        // Send the request and process the response
        try (CloseableHttpResponse response = httpClient().execute(delete)) {
            T body = deserialiseResponseMessage(response, responseClass);
            return new Response<>(response.getStatusLine(), body);
        }
    }

    /**
     * Adds a header that will be used for all requests made by this instance.
     *
     * @param name  The header name. You can use {@link org.apache.http.HttpHeaders} constants for header names.
     * @param value The header value.
     */
    public void addHeader(String name, String value) {
        headers.add(new BasicHeader(name, value));
    }

    /**
     * Handles reading an uploaded file.
     *
     * @param request The http request.
     * @return A temp file containing the file data.
     * @throws IOException If an error occurs in processing the file.
     */
    public static Path getFile(HttpServletRequest request)
            throws IOException {
        Path result = null;

        // Set up the objects that do all the heavy lifting
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);

        try {
            // Read the items - this will save the values to temp files
            for (FileItem item : upload.parseRequest(request)) {
                if (!item.isFormField()) {
                    result = Files.createTempFile("upload", ".file");
                    item.write(result.toFile());
                }
            }
        } catch (Exception e) {
            // item.write throws a general Exception, so specialise it by wrapping with IOException
            throw new IOException("Error processing uploaded file", e);
        }

        return result;
    }

    /**
     * Sets the combined request headers.
     *
     * @param headers Additional header values to add over and above {@link #headers}.
     */

    private Header[] combineHeaders(NameValuePair[] headers) {

        Header[] fullHeaders = new Header[this.headers.size() + headers.length];

        // Add class-level headers (for all requests)
        for (int i = 0; i < this.headers.size(); i++) {
            fullHeaders[i] = this.headers.get(i);
        }

        // Add headers specific to this request:
        for (int i = 0; i < headers.length; i++) {
            NameValuePair header = headers[i];
            fullHeaders[i + this.headers.size()] = new BasicHeader(header.getName(), header.getValue());
        }

        //System.out.println(Arrays.toString(fullHeaders));
        return fullHeaders;
    }

    private Header[] combineHeaders() {

        Header[] fullHeaders = new Header[this.headers.size()];

        // Add class-level headers (for all requests)
        for (int i = 0; i < this.headers.size(); i++) {
            fullHeaders[i] = this.headers.get(i);
        }

        //System.out.println(Arrays.toString(fullHeaders));
        return fullHeaders;
    }

    /**
     * Serialises the given object as a {@link org.apache.http.entity.StringEntity}.
     *
     * @param requestMessage The object to be serialised.
     * @throws java.io.UnsupportedEncodingException If a serialisation error occurs.
     */
    protected StringEntity serialiseRequestMessage(Object requestMessage) throws UnsupportedEncodingException {
        StringEntity result = null;

        // Add the request message if there is one
        if (requestMessage != null) {
            // Send the message
            String message = Serialiser.serialise(requestMessage);
            result = new StringEntity(message);
        }

        return result;
    }

    /**
     * Deserialises the given {@link CloseableHttpResponse} to the specified type.
     *
     * @param response      The response.
     * @param responseClass The type to deserialise to. This can be null, in which case {@link org.apache.http.util.EntityUtils#consume(HttpEntity)} will be used to consume the response body (if any).
     * @param <T>           The type to deserialise to.
     * @return The deserialised response, or null if the response does not contain an entity.
     * @throws IOException If an error occurs.
     */
    protected <T> T deserialiseResponseMessage(CloseableHttpResponse response, Class<T> responseClass) throws IOException {
        T body = null;

        HttpEntity entity = response.getEntity();
        if (entity != null && responseClass != null) {
            try (InputStream inputStream = entity.getContent()) {
                try {
                    body = Serialiser.deserialise(inputStream, responseClass);
                } catch (JsonSyntaxException e) {
                    // This can happen if an error HTTP code is received and the
                    // body of the response doesn't contain the expected object:
                    body = null;
                }
            }
        } else {
            EntityUtils.consume(entity);
        }

        return body;
    }

    protected String deserialiseResponseMessage(CloseableHttpResponse response) throws IOException {
        String body = null;

        HttpEntity entity = response.getEntity();
        if (entity != null) {
            log.debug("HTTP|entity: {}", entity);
            log.debug("HTTP|entity content type: {}", entity.getContentType());
            log.debug("HTTP|entity length: {}", entity.getContentLength());
            try (InputStream inputStream = entity.getContent()) {
                body = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            }
        } else {
            EntityUtils.consume(entity);
        }

        log.debug("HTTP|body: {}", body);

        return body;
    }

    /**
     * Deserialises the given {@link CloseableHttpResponse} to the specified type.
     *
     * @param response The response.
     * @param type     The type to deserialise to. This can be null, in which case {@link EntityUtils#consume(HttpEntity)} will be used to consume the response body (if any).
     * @param <T>      The type to deserialise to.
     * @return The deserialised response, or null if the response does not contain an entity.
     * @throws IOException If an error occurs.
     */
    protected <T> T deserialiseResponseMessage(CloseableHttpResponse response, Type type) throws IOException {
        T body = null;

        HttpEntity entity = response.getEntity();
        if (entity != null && type != null) {
            try (InputStream inputStream = entity.getContent()) {
                try {
                    body = Serialiser.deserialise(inputStream, type);
                } catch (JsonSyntaxException e) {
                    // This can happen if an error HTTP code is received and the
                    // body of the response doesn't contain the expected object:
                    System.out.println(ExceptionUtils.getStackTrace(e));
                    body = null;
                }
            }
        } else {
            EntityUtils.consume(entity);
        }

        return body;
    }

    protected CloseableHttpClient httpClient() {
        if (httpClient == null) {
            HttpClientBuilder b = HttpClientBuilder.create();
            // setup a Trust Strategy that allows all certificates.
            //
            SSLContext sslContext = null;
            try {
                sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                    public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                        return true;
                    }
                }).build();
                b.setSslcontext(sslContext);
                httpClient = b.build();// HttpClients.createDefault();
            } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
                throw new RuntimeException("Error building httpclient.");
            }
        }
        return httpClient;
    }

    @Override
    public void close() {
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (IOException e) {
                // Mostly ignore it
                e.printStackTrace();
            }
        }
    }

}

