package com.moveon.config;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GeoCookieFilterTest {

    private ServletRequest run(MockHttpServletRequest req) throws Exception {
        MockFilterChain chain = new MockFilterChain();
        new GeoCookieFilter().doFilter(req, new MockHttpServletResponse(), chain);
        return chain.getRequest();
    }

    @Test
    void 쿠키가_lat_lng_로_들어간다() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setParameter("sportId", "1");
        req.setCookies(new Cookie(GeoCookieFilter.COOKIE, "37.530900_126.840100"));

        ServletRequest out = run(req);
        assertEquals("37.530900", out.getParameter("lat"));
        assertEquals("126.840100", out.getParameter("lng"));
        assertEquals("1", out.getParameter("sportId"));
    }

    @Test
    void 주소의_좌표가_먼저다() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setParameter("lat", "1");
        req.setParameter("lng", "2");
        req.setCookies(new Cookie(GeoCookieFilter.COOKIE, "37.5_126.8"));

        assertEquals("1", run(req).getParameter("lat"));
    }

    @Test
    void 깨진_쿠키는_무시한다() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setCookies(new Cookie(GeoCookieFilter.COOKIE, "abc_def"));

        assertNull(run(req).getParameter("lat"));
    }
}
