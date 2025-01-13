package com.baotruongtuan.RdpServer.utils;

import com.baotruongtuan.RdpServer.exception.AppException;
import com.baotruongtuan.RdpServer.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class DomainExtractHelper {
    public boolean isValidUrl(String content) {
        try {
            URI uri = new URI(content);
            return uri.getScheme() != null && uri.getHost() != null;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractDomain(String url) {
        try {
            URI uri = new URI(url);
            return uri.getHost().replace("www.", "").split("\\.")[0];
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URL: " + url);
        }
    }

    public String processedContent(String content) {
        if (!content.contains(" - ")) {
            return content.replace(" ", "").toLowerCase();
        }

        String[] parts = content.split(" - ");

        String[] reversedParts = new String[parts.length];
        for (int i = 0; i < parts.length; i++) {
            reversedParts[i] = parts[parts.length - 1 - i];
        }

        if (reversedParts.length > 1) {
            if(reversedParts[0].equals("Microsoft Edge"))
                return reversedParts[2].replace(" ", "").toLowerCase();
            return reversedParts[1].replace(" ", "").toLowerCase();
        } else {
            throw new AppException(ErrorCode.INVALID_DATA);
        }
    }
}
