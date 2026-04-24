package org.example.core.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import ua_parser.Client;
import ua_parser.Parser;


@Component
public class DeviceInfoExtractor {
    public DeviceInfoExtractor() {}

    public String extract(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String ip = extractIp(request);
        if (userAgent == null) return ip + " | unknown";

        Parser parser = new Parser();
        Client client  = parser.parse(userAgent);
        String readable = client.userAgent.family + " " + client.userAgent.major
                + " на " + client.os.family + " " + client.os.major;

        return ip + " | " + readable;
    }

    public String extractIp(HttpServletRequest request) {
        // если приложегние за проки или балансировщиком
        String ip = request.getHeader("x-forwarded-for");
        if (ip != null && !ip.isBlank()){
            return ip.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}
