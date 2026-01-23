package io.akikr.app.shared;

import jakarta.servlet.FilterChain;
import jakarta.servlet.GenericFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
public class AppLoggingFilter extends GenericFilter {

  private static final Logger log = LoggerFactory.getLogger(AppLoggingFilter.class);

  private final AppLoggingProperties appLoggingProperties;

  public AppLoggingFilter(AppLoggingProperties appLoggingProperties) {
    this.appLoggingProperties = appLoggingProperties;
  }

  @Override
  public void doFilter(
      ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
      throws IOException, ServletException {
    ContentCachingRequestWrapper requestWrapper = requestWrapper(servletRequest);
    ContentCachingResponseWrapper responseWrapper = responseWrapper(servletResponse);

    filterChain.doFilter(requestWrapper, responseWrapper);

    if (appLoggingProperties.enabled()) {
      logRequest(requestWrapper);
      logResponse(responseWrapper);
    }
    responseWrapper.copyBodyToResponse();
  }

  private void logRequest(ContentCachingRequestWrapper request) {
    var body = "[Not Logged]";
    if (appLoggingProperties.includeRequestBody()) {
      body = new String(request.getContentAsByteArray());
      if (body.length() > appLoggingProperties.maxBodyLength()) {
        body = body.substring(0, appLoggingProperties.maxBodyLength()) + "...[truncated]";
      }
    }
    log.debug("REQUEST BODY: {}", (body.isEmpty()) ? "[No-Body]" : body);
  }

  private void logResponse(ContentCachingResponseWrapper response) {
    var body = "[Not Logged]";
    if (appLoggingProperties.includeRequestBody()) {
      body = new String(response.getContentAsByteArray());
      if (body.length() > appLoggingProperties.maxBodyLength()) {
        body = body.substring(0, appLoggingProperties.maxBodyLength()) + "...[truncated]";
      }
    }
    log.debug("RESPONSE BODY: {}", (body.isEmpty()) ? "[No-Body]" : body);
  }

  private ContentCachingRequestWrapper requestWrapper(ServletRequest servletRequest) {
    if (servletRequest instanceof ContentCachingRequestWrapper requestWrapper) {
      return requestWrapper;
    }
    return new ContentCachingRequestWrapper((HttpServletRequest) servletRequest, Integer.MAX_VALUE);
  }

  private ContentCachingResponseWrapper responseWrapper(ServletResponse servletResponse) {
    if (servletResponse instanceof ContentCachingResponseWrapper responseWrapper) {
      return responseWrapper;
    }
    return new ContentCachingResponseWrapper((HttpServletResponse) servletResponse);
  }
}
