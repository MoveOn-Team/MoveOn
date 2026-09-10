package com.moveon.util;

/**
 * 밖으로 내보낼 주소를 가리는 일.
 *
 * 공공데이터에는 값이 비었다는 뜻으로 "null" 이라는 글자가 그대로 들어온 행이 많다.
 * (programs.reservation_url 만 249건) 자바의 null 검사로는 안 걸러진다.
 */
public class UrlUtil {

    private UrlUtil() {
    }

    /** 링크로 쓸 수 있는 주소인지 */
    public static boolean isUsable(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        String v = url.trim();
        if ("null".equalsIgnoreCase(v) || "-".equals(v)) {
            return false;
        }
        return v.regionMatches(true, 0, "http://", 0, 7)
                || v.regionMatches(true, 0, "https://", 0, 8);
    }

    /** 앞에서부터 쓸 만한 주소를 고름. 없으면 null */
    public static String firstUsable(String... urls) {
        if (urls == null) {
            return null;
        }
        for (String u : urls) {
            if (isUsable(u)) {
                return u.trim();
            }
        }
        return null;
    }
}
