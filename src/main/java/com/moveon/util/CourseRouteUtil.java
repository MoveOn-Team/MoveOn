package com.moveon.util;

import com.moveon.dto.CoursePointDTO;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 코스 지점으로 카카오맵 도보 길찾기 주소를 만든다.
 *
 * link/by 는 경유지를 최대 5개까지 받는다. 코스는 지점이 스물몇에서 예순여덟까지라
 * 그 안에 들어가도록 추려야 한다.
 */
public final class CourseRouteUtil {

    /** 출발 1 + 경유 5 + 도착 1. 카카오·네이버 둘 다 경유지가 5개다 */
    private static final int MAX_POINTS = 7;

    private static final String KAKAO = "https://map.kakao.com/link/by/walk/";
    private static final String NAVER = "nmap://route/walk?";

    private CourseRouteUtil() {
    }

    /**
     * 코스를 따라가는 도보 길찾기 주소. 지점이 2개 미만이면 null.
     *
     * @param loop 순환형이면 마지막에 출발점을 다시 붙여 제자리로 돌아오게 한다
     */
    public static String walkUrl(List<CoursePointDTO> points, boolean loop) {
        if (points == null || points.size() < 2) {
            return null;
        }

        List<CoursePointDTO> picked = thinOut(points, loop ? MAX_POINTS - 1 : MAX_POINTS);

        StringBuilder sb = new StringBuilder(KAKAO);
        for (int i = 0; i < picked.size(); i++) {
            if (i > 0) {
                sb.append('/');
            }
            sb.append(leg(label(i, picked.size(), loop), picked.get(i)));
        }
        if (loop) {
            sb.append('/').append(leg("도착", picked.get(0)));
        }
        return sb.toString();
    }

    /**
     * 네이버지도 도보 길찾기 앱 스킴. 지점이 2개 미만이면 null.
     *
     * 카카오와 달리 여기는 실시간 안내(안내시작·음성)가 있다.
     * 웹 주소(map.naver.com)는 좌표를 자체 형식으로 인코딩해서 우리가 만들 수 없다.
     * 앱 스킴은 생좌표를 받으므로 이쪽을 쓴다. 대신 앱이 없으면 아무 일도 안 일어난다.
     */
    public static String naverWalkUrl(List<CoursePointDTO> points, boolean loop) {
        if (points == null || points.size() < 2) {
            return null;
        }

        List<CoursePointDTO> picked = thinOut(points, loop ? MAX_POINTS - 1 : MAX_POINTS);
        CoursePointDTO start = picked.get(0);
        CoursePointDTO end = loop ? start : picked.get(picked.size() - 1);

        StringBuilder sb = new StringBuilder(NAVER);
        sb.append("slat=").append(start.getLat())
          .append("&slng=").append(start.getLng())
          .append("&sname=").append(enc("출발"));

        // 경유지는 v1..v5. 출발·도착을 뺀 가운데 점들이다
        int last = loop ? picked.size() : picked.size() - 1;
        for (int i = 1, v = 1; i < last; i++, v++) {
            sb.append("&v").append(v).append("lat=").append(picked.get(i).getLat())
              .append("&v").append(v).append("lng=").append(picked.get(i).getLng())
              .append("&v").append(v).append("name=").append(enc("경유" + v));
        }

        sb.append("&dlat=").append(end.getLat())
          .append("&dlng=").append(end.getLng())
          .append("&dname=").append(enc("도착"))
          .append("&appname=com.moveon");
        return sb.toString();
    }

    /**
     * 한 지점까지 가는 네이버 도보 길찾기. 좌표가 없으면 null.
     *
     * 출발지를 안 넣으면 앱이 현위치를 출발지로 잡는다. 카카오 link/to 와 같은 모양이다.
     */
    public static String naverToUrl(double lat, double lng, String name) {
        if (lat == 0 && lng == 0) {
            return null;
        }
        return NAVER + "dlat=" + lat + "&dlng=" + lng
                + "&dname=" + enc(name) + "&appname=com.moveon";
    }

    private static String enc(String s) {
        return UriUtils.encode(s, StandardCharsets.UTF_8);
    }

    /** 이름에 쉼표·빗금이 들어가면 좌표가 밀리므로 자리 이름만 쓴다 */
    private static String label(int i, int size, boolean loop) {
        if (i == 0) {
            return "출발";
        }
        if (!loop && i == size - 1) {
            return "도착";
        }
        return "경유" + i;
    }

    private static String leg(String name, CoursePointDTO p) {
        return UriUtils.encode(name, StandardCharsets.UTF_8) + "," + p.getLat() + "," + p.getLng();
    }

    /**
     * 코스 길이를 고르게 나눈 자리에서 가장 가까운 지점을 골라 max 개로 줄인다.
     *
     * 번호를 고르게 나누면 안 된다. 원본은 좁은 구간에 점이 몰려 있는 코스가 있어,
     * 그러면 뽑힌 점들이 한쪽에 쏠려 코스 모양이 무너진다.
     */
    private static List<CoursePointDTO> thinOut(List<CoursePointDTO> points, int max) {
        if (points.size() <= max) {
            return points;
        }

        double[] upto = new double[points.size()];
        for (int i = 1; i < points.size(); i++) {
            upto[i] = upto[i - 1] + gap(points.get(i - 1), points.get(i));
        }
        double total = upto[points.size() - 1];

        List<CoursePointDTO> picked = new ArrayList<>();
        int prev = -1;
        for (int k = 0; k < max; k++) {
            int at = nearest(upto, total * k / (max - 1.0));
            if (at > prev) {
                picked.add(points.get(at));
                prev = at;
            }
        }
        return picked;
    }

    private static int nearest(double[] upto, double target) {
        int best = 0;
        double bestGap = Double.MAX_VALUE;
        for (int i = 0; i < upto.length; i++) {
            double d = Math.abs(upto[i] - target);
            if (d < bestGap) {
                bestGap = d;
                best = i;
            }
        }
        return best;
    }

    /** 두 점 사이 거리. 어느 자리를 고를지만 정하면 되므로 단순 평면 거리로 충분하다 */
    private static double gap(CoursePointDTO a, CoursePointDTO b) {
        double dy = a.getLat() - b.getLat();
        double dx = (a.getLng() - b.getLng()) * 0.79;   // 서울 위도에서의 경도 보정
        return Math.sqrt(dy * dy + dx * dx);
    }
}
