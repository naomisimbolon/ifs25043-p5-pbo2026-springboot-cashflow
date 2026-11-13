package org.delcom.ifs25043springbootcashflow.framework.web;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Memberi header cache jangka panjang untuk aset frontend Vaadin yang sudah
 * content-hashed (nama file berubah setiap isi berubah).
 * <p>
 * Vaadin hanya mengirim {@code Cache-Control: max-age=31536000, immutable}
 * bila URL memiliki parameter {@code v-c}; aset di {@code /VAADIN/build/}
 * tidak memakai parameter tersebut sehingga hanya di-cache satu jam. Filter
 * ini menaikkan masa cache-nya agar audit performa (cache policy) lolos dan
 * kunjungan berikutnya tidak mengunduh ulang bundle JavaScript.
 */
@Component
public class StaticAssetCacheFilter extends OncePerRequestFilter {

    static final String LONG_CACHE_CONTROL = "public, max-age=31536000, immutable";

    private static final String BUILD_ASSET_PREFIX = "/VAADIN/build/";
    private static final String CACHE_CONTROL_HEADER = "Cache-Control";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (isContentHashedAsset(request)) {
            filterChain.doFilter(request, new LongCacheResponse(response));
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private boolean isContentHashedAsset(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null) {
            return false;
        }
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty()
                && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }
        return uri.startsWith(BUILD_ASSET_PREFIX);
    }

    /**
     * Membungkus response agar header {@code Cache-Control} yang ditulis
     * Vaadin diganti dengan masa cache satu tahun.
     */
    private static final class LongCacheResponse extends HttpServletResponseWrapper {

        private LongCacheResponse(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void setHeader(String name, String value) {
            super.setHeader(name, cacheControlValue(name, value));
        }

        @Override
        public void addHeader(String name, String value) {
            super.addHeader(name, cacheControlValue(name, value));
        }

        private String cacheControlValue(String name, String value) {
            if (CACHE_CONTROL_HEADER.equalsIgnoreCase(name)) {
                return LONG_CACHE_CONTROL;
            }
            return value;
        }
    }
}
