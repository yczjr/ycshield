package com.blued.common;

import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 离线 IP 归属地解析（ip2region）。库文件缺失时自动降级为"未知"，不影响业务。
 */
@Slf4j
@Service
public class IpRegionService {

    private Searcher searcher;

    public IpRegionService(@Value("${IP_DB:/opt/blued/data/ip2region.xdb}") String dbPath) {
        try {
            Path p = Paths.get(dbPath);
            if (Files.exists(p)) {
                searcher = Searcher.newWithBuffer(Files.readAllBytes(p));
                log.info("IP 归属地库已加载：{}", dbPath);
            } else {
                log.warn("IP 归属地库不存在：{}，归属地记为未知", dbPath);
            }
        } catch (Exception e) {
            log.warn("IP 归属地库加载失败：{}", e.getMessage());
        }
    }

    /** 返回 [国家, 省, 市]，解析失败返回 null */
    public String[] region(String ip) {
        if (searcher == null || ip == null || ip.isBlank() || !ip.contains(".")) {
            return null;
        }
        try {
            String r = searcher.search(ip.trim());
            if (r == null || r.isEmpty()) {
                return null;
            }
            String[] parts = r.split("\\|");
            if (parts.length < 3) {
                return null;
            }
            return new String[]{norm(parts[0]), norm(parts[2]), parts.length > 3 ? norm(parts[3]) : ""};
        } catch (Exception e) {
            return null;
        }
    }

    /** 大屏归属标签：国内取省份短名，境外取国家名 */
    public String label(String ip) {
        String[] r = region(ip);
        if (r == null) {
            return "未知";
        }
        if ("中国".equals(r[0])) {
            String p = shortProvince(r[1]);
            return p.isEmpty() ? "中国" : p;
        }
        return r[0].isEmpty() || "0".equals(r[0]) ? "未知" : r[0];
    }

    public String country(String ip) {
        String[] r = region(ip);
        if (r == null || r[0].isEmpty()) {
            return "未知";
        }
        return r[0];
    }

    public boolean isReady() {
        return searcher != null;
    }

    private static String norm(String s) {
        if (s == null) {
            return "";
        }
        String t = s.trim();
        return "0".equals(t) ? "" : t;
    }

    /** 省份名称归一（江苏省 → 江苏、内蒙古自治区 → 内蒙古） */
    public static String shortProvince(String p) {
        if (p == null || p.isEmpty() || "0".equals(p)) {
            return "";
        }
        String s = p.trim();
        String[] suffixes = {"维吾尔自治区", "壮族自治区", "回族自治区", "特别行政区", "自治区", "省", "市"};
        for (String suf : suffixes) {
            if (s.endsWith(suf) && s.length() > suf.length()) {
                return s.substring(0, s.length() - suf.length());
            }
        }
        return s;
    }
}
