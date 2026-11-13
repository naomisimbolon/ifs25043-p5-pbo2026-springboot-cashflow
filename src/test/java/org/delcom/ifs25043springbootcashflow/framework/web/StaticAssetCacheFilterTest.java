package org.delcom.ifs25043springbootcashflow.framework.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class StaticAssetCacheFilterTest {

    private final StaticAssetCacheFilter filter = new StaticAssetCacheFilter();

    @Test
    void passesRequestThroughTheChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET",
                "/VAADIN/build/generated-flow-imports-abc123.js");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isSameAs(request);
    }

    @Test
    void rewritesCacheControlWrittenByVaadin() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request("/VAADIN/build/app.js"), response,
                (req, res) -> http(res).setHeader("Cache-Control", "max-age=3600"));

        assertThat(response.getHeader("Cache-Control"))
                .isEqualTo(StaticAssetCacheFilter.LONG_CACHE_CONTROL);
    }

    @Test
    void rewritesCacheControlAddedByVaadin() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request("/VAADIN/build/app.js"), response,
                (req, res) -> http(res).addHeader("cache-control", "max-age=3600"));

        assertThat(response.getHeader("Cache-Control"))
                .isEqualTo(StaticAssetCacheFilter.LONG_CACHE_CONTROL);
    }

    @Test
    void keepsOtherHeadersUntouched() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request("/VAADIN/build/app.js"), response, (req, res) -> {
            http(res).setHeader("Content-Type", "text/javascript");
            http(res).addHeader("X-Test", "value");
        });

        assertThat(response.getHeader("Content-Type")).isEqualTo("text/javascript");
        assertThat(response.getHeader("X-Test")).isEqualTo("value");
        assertThat(response.getHeader("Cache-Control")).isNull();
    }

    @Test
    void ignoresRequestsOutsideBuildDirectory() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request("/icons/icon.png"), response,
                (req, res) -> http(res).setHeader("Cache-Control", "max-age=60"));

        assertThat(response.getHeader("Cache-Control")).isEqualTo("max-age=60");
    }

    @Test
    void resolvesAssetPathIncludingContextPath() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET",
                "/cashflow/VAADIN/build/app.js");
        request.setContextPath("/cashflow");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response,
                (req, res) -> http(res).setHeader("Cache-Control", "max-age=60"));

        assertThat(response.getHeader("Cache-Control"))
                .isEqualTo(StaticAssetCacheFilter.LONG_CACHE_CONTROL);
    }

    @Test
    void ignoresNonBuildPathWhenContextPathIsSet() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET",
                "/cashflow/VAADIN/build/app.js");
        request.setContextPath("/cashflow");
        request.setRequestURI("/cashflow/other/app.js");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response,
                (req, res) -> http(res).setHeader("Cache-Control", "max-age=60"));

        assertThat(response.getHeader("Cache-Control")).isEqualTo("max-age=60");
    }

    @Test
    void ignoresNullRequestUri() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(null);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response,
                (req, res) -> http(res).setHeader("Cache-Control", "max-age=60"));

        assertThat(response.getHeader("Cache-Control")).isEqualTo("max-age=60");
    }

    private static HttpServletResponse http(jakarta.servlet.ServletResponse response) {
        return (HttpServletResponse) response;
    }

    private static MockHttpServletRequest request(String uri) {
        return new MockHttpServletRequest("GET", uri);
    }
}
