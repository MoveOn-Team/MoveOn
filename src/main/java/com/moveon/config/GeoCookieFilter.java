package com.moveon.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 현위치를 주소가 아니라 쿠키로 받는다.
 *
 * 전에는 geo.js 가 주소에 lat/lng 를 달아 다시 불렀다. 그러면 내 좌표가
 * 주소창·공유 링크·방문 기록에 그대로 남는다.
 * 이제 geo.js 는 쿠키(moveon_coords = "위도_경도")만 굽고,
 * 여기서 lat/lng 파라미터로 바꿔 넣어 컨트롤러의 @RequestParam 은 그대로 둔다.
 *
 * 주소에 lat/lng 가 이미 있으면(옛 즐겨찾기 등) 그쪽을 먼저 쓴다.
 */
@Component
public class GeoCookieFilter extends OncePerRequestFilter {

    public static final String COOKIE = "moveon_coords";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String[] pos = request.getParameter("lat") == null ? fromCookie(request) : null;
        if (pos == null) {
            chain.doFilter(request, response);
            return;
        }

        Map<String, String[]> params = new HashMap<>(request.getParameterMap());
        params.put("lat", new String[]{pos[0]});
        params.put("lng", new String[]{pos[1]});

        chain.doFilter(new HttpServletRequestWrapper(request) {
            @Override
            public String getParameter(String name) {
                String[] v = params.get(name);
                return v == null ? null : v[0];
            }

            @Override
            public String[] getParameterValues(String name) {
                return params.get(name);
            }

            @Override
            public Map<String, String[]> getParameterMap() {
                return Collections.unmodifiableMap(params);
            }

            @Override
            public Enumeration<String> getParameterNames() {
                return Collections.enumeration(params.keySet());
            }
        }, response);
    }

    /** "37.530900_126.840100" → {위도, 경도}. 숫자가 아니면 없는 셈 친다 */
    static String[] fromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie c : request.getCookies()) {
            if (!COOKIE.equals(c.getName())) {
                continue;
            }
            String[] p = c.getValue().split("_");
            try {
                if (p.length == 2) {
                    Double.parseDouble(p[0]);
                    Double.parseDouble(p[1]);
                    return p;
                }
            } catch (NumberFormatException ignored) {
                // 깨진 쿠키는 기준점으로 계산하게 둔다
            }
        }
        return null;
    }
}
