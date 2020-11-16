package ca.bc.gov.gwa.http;

import ca.bc.gov.gwa.util.Json;
import com.google.common.collect.Lists;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

@Slf4j
public class JsonHttpClient implements Closeable {
  private final String serviceUrl;

  private CloseableHttpClient httpClient;

  public JsonHttpClient(final String serviceUrl) {
    this(serviceUrl, null, null);
  }

  public JsonHttpClient(final String serviceUrl, final String token) {
    this.serviceUrl = serviceUrl;

    final HttpClientBuilder clientBuilder = HttpClients.custom();
    if (token != null) {
      Collection<Header> defaultHeaders = Lists.newArrayList(new BasicHeader("Authorization", String.format("token %s", token)));
      clientBuilder.setDefaultHeaders(defaultHeaders);
    }
    this.httpClient = clientBuilder.build();
  }
  
  public JsonHttpClient(final String serviceUrl, final String username, final String password) {
    this.serviceUrl = serviceUrl;

    try {
      final HttpClientBuilder clientBuilder = HttpClients.custom();
      if (username != null) {
        final URI uri = new URI(serviceUrl);
        final String hostName = uri.getHost();
        int port = uri.getPort();
        if (port == -1) {
          if (serviceUrl.startsWith("https")) {
            port = 443;
          } else {
            port = 80;
          }
        }
        final AuthScope authscope = new AuthScope(hostName, port);
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        final Credentials credentials = new UsernamePasswordCredentials(username, password);
        credentialsProvider.setCredentials(authscope, credentials);
        clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
      }
      this.httpClient = clientBuilder.build();
    } catch (final URISyntaxException e) {
      throw new IllegalArgumentException("Invalid URL: " + serviceUrl, e);
    }
  }

  
  private void appendContent(final StringBuilder content, final CloseableHttpResponse response)
    throws IOException {
    try (
      InputStream in = response.getEntity().getContent();
      Reader reader = new InputStreamReader(in)) {
      final char[] chars = new char[4096];
      for (int readCount = reader.read(chars); readCount != -1; readCount = reader.read(chars)) {
        content.append(chars, 0, readCount);
      }
    }
  }

  @Override
  public void close() throws IOException {
    if (this.httpClient != null) {
      this.httpClient.close();
      this.httpClient = null;
    }
  }
  
  public Map<String, Object> delete(final String path) throws IOException {
      return delete(null, path);
  }
  
  public Map<String, Object> delete(final BearerAccessToken token, final String path) throws IOException {
    final HttpDelete httpRequest = new HttpDelete(this.serviceUrl + path);
    if (token != null) {
        httpRequest.addHeader("Authorization", token.toAuthorizationHeader());
    }
    try (
      CloseableHttpResponse updateResponse = this.httpClient.execute(httpRequest)) {
      final StatusLine statusLine = updateResponse.getStatusLine();
      final int statusCode = statusLine.getStatusCode();
      if (statusCode == 200 || statusCode == 204 || statusCode == 404) {
        return Collections.singletonMap("deleted", true);
      } else {
        final Map<String, Object> errorData = new LinkedHashMap<>();
        errorData.put("errorCode", statusCode);
        errorData.put("errorMessage", statusLine.getReasonPhrase());
        final String body = getContent(updateResponse);
        errorData.put("errorDetail", body);
        return errorData;
      }
    }
  }

  public <V> V executeRequest(final HttpUriRequest httpRequest) throws IOException {
    httpRequest.addHeader("Accept", "application/json");

    try (
      CloseableHttpResponse updateResponse = this.httpClient.execute(httpRequest)) {
      final StatusLine statusLine = updateResponse.getStatusLine();
      final int statusCode = statusLine.getStatusCode();
      if (statusCode == 200 || statusCode == 201) {
        return getContentJson(updateResponse);
      } else if (statusCode == 400) {
        final String message = statusLine.getReasonPhrase();
        final Map<String,Object> body = getContentJson(updateResponse);
        log.debug("ExecuteRequest 400 Failure {} {}", message, body.get("error"));
        throw new HttpStatusException(httpRequest, statusCode, message, (String) body.get("error"));
          
      } else {
        final String message = statusLine.getReasonPhrase();
        final String body = getContent(updateResponse);
        log.debug("executeRequest Failure {} {} {}", statusCode, message, body);
        throw new HttpStatusException(httpRequest, statusCode, message, body);
      }
    }
  }

  public Map<String, Object> executeRequestJson(final HttpEntityEnclosingRequestBase httpRequest,
    final Map<String, Object> data) throws IOException {
    final String requestText = Json.toString(data);
    final StringEntity updateRequestEntity = new StringEntity(requestText,
      ContentType.APPLICATION_JSON);
    httpRequest.setEntity(updateRequestEntity);
    return executeRequest(httpRequest);
  }

  
  public <V> V get(final String path) throws IOException {
    final String url = this.serviceUrl + path;
    return getByUrl(url);
  }

  public <V> V get(final BearerAccessToken token, final String path) throws IOException {
    final String url = this.serviceUrl + path;
    return getByUrl(token, url);
  }

  public <V> List<V> list(final BearerAccessToken token, final String path) throws IOException {
    final String url = this.serviceUrl + path;
    return getByUrl(token, url);
  }
  
  public <V> V getByUrl(final String url) throws IOException {
    final HttpGet request = new HttpGet(url);
    return executeRequest(request);
  }
  
  public <V> V getByUrl(final BearerAccessToken token, final String url) throws IOException {
    final HttpGet request = new HttpGet(url);
    request.addHeader("Authorization", token.toAuthorizationHeader());
    return executeRequest(request);
  }

  public String getContent(final CloseableHttpResponse response) throws IOException {
    final StringBuilder content = new StringBuilder();
    appendContent(content, response);
    return content.toString();
  }

  public <V> V getContentJson(final CloseableHttpResponse response) throws IOException {
    final HttpEntity entity = response.getEntity();
    try (
      InputStream in = entity.getContent();
      Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
      return Json.read(reader);
    }
  }

  public Map<String, Object> patch(final String path, final Map<String, Object> data)
    throws IOException {
    final HttpPatch request = new HttpPatch(this.serviceUrl + path);
    return executeRequestJson(request, data);
  }

  public Map<String, Object> post(final String path, final Map<String, Object> data)
    throws IOException {
    final HttpPost request = new HttpPost(this.serviceUrl + path);
    return executeRequestJson(request, data);
  }

  public Map<String, Object> post(final BearerAccessToken token, final String path, final Map<String, Object> data)
    throws IOException {
    final HttpPost request = new HttpPost(this.serviceUrl + path);
    request.addHeader("Authorization", token.toAuthorizationHeader());
    return executeRequestJson(request, data);
  }
  
  public Map<String, Object> put(final String path) throws IOException {
    final HttpPut httpRequest = new HttpPut(this.serviceUrl + path);
    final StringEntity updateRequestEntity = new StringEntity("", ContentType.TEXT_PLAIN);
    httpRequest.setEntity(updateRequestEntity);
    return executeRequest(httpRequest);
  }

  public Map<String, Object> put(final String path, final Map<String, Object> data)
    throws IOException {
    final HttpPut updateRequest = new HttpPut(this.serviceUrl + path);
    return executeRequestJson(updateRequest, data);
  }

  public Map<String, Object> put(final BearerAccessToken token, final String path, final Map<String, Object> data)
    throws IOException {
    final HttpPut updateRequest = new HttpPut(this.serviceUrl + path);
    updateRequest.addHeader("Authorization", token.toAuthorizationHeader());
    return executeRequestJson(updateRequest, data);
  }

}
