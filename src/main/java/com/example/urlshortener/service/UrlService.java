package com.example.urlshortener.service;

import com.example.urlshortener.dto.UrlResponse;
import com.example.urlshortener.model.Url;
import com.example.urlshortener.repository.UrlRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UrlService {

    private static final String BASE62_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int CODE_LENGTH = 6;
    private static final int RATE_LIMIT_MAX_REQUESTS = 10;
    private static final long RATE_LIMIT_WINDOW_MS = 60_000;

    private final UrlRepository urlRepository;
    private final String baseUrl;
    private final SecureRandom random = new SecureRandom();

    private final Map<String, List<Long>> rateLimitMap = new ConcurrentHashMap<>();

    public UrlService(UrlRepository urlRepository, @Value("${app.base-url}") String baseUrl) {
        this.urlRepository = urlRepository;
        this.baseUrl = baseUrl;
    }

    public boolean allowRateLimit(String clientIp) {
        long now = System.currentTimeMillis();
        long windowStart = now - RATE_LIMIT_WINDOW_MS;

        rateLimitMap.compute(clientIp, (ip, timestamps) -> {
            if (timestamps == null) {
                timestamps = new ArrayList<>();
            }
            timestamps.removeIf(ts -> ts < windowStart);
            return timestamps;
        });

        List<Long> timestamps = rateLimitMap.get(clientIp);
        synchronized (timestamps) {
            if (timestamps.size() >= RATE_LIMIT_MAX_REQUESTS) {
                return false;
            }
            timestamps.add(now);
            return true;
        }
    }

    public boolean isValidUrl(String originalUrl) {
        if (originalUrl == null || originalUrl.trim().isEmpty()) {
            return false;
        }
        String trimmed = originalUrl.trim();
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            return false;
        }
        try {
            URI uri = new URI(trimmed);
            return uri.getHost() != null && !uri.getHost().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public UrlResponse createShortUrl(String originalUrl) {
        String trimmedUrl = originalUrl.trim();
        String shortCode = generateUniqueShortCode();

        Url url = new Url(trimmedUrl, shortCode);
        Url savedUrl = urlRepository.save(url);

        return mapToUrlResponse(savedUrl);
    }

    public Optional<String> getAndIncrementOriginalUrl(String shortCode) {
        Optional<Url> optionalUrl = urlRepository.findByShortCode(shortCode);
        if (optionalUrl.isEmpty()) {
            return Optional.empty();
        }

        Url url = optionalUrl.get();
        url.setClickCount(url.getClickCount() + 1);
        url.setLastAccessedAt(LocalDateTime.now());
        urlRepository.save(url);

        return Optional.of(url.getOriginalUrl());
    }

    public Optional<UrlResponse> getStats(String shortCode) {
        return urlRepository.findByShortCode(shortCode)
                .map(this::mapToUrlResponse);
    }

    private String generateUniqueShortCode() {
        String code;
        int maxAttempts = 100;
        int attempt = 0;

        do {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                int randomIndex = random.nextInt(BASE62_ALPHABET.length());
                sb.append(BASE62_ALPHABET.charAt(randomIndex));
            }
            code = sb.toString();
            attempt++;
            if (attempt > maxAttempts) {
                throw new IllegalStateException("Could not generate a unique short code after " + maxAttempts + " attempts.");
            }
        } while (urlRepository.existsByShortCode(code));

        return code;
    }

    private UrlResponse mapToUrlResponse(Url url) {
        String cleanBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String fullShortUrl = cleanBaseUrl + "/" + url.getShortCode();

        return new UrlResponse(
                url.getShortCode(),
                fullShortUrl,
                url.getOriginalUrl(),
                url.getClickCount(),
                url.getCreatedAt(),
                url.getLastAccessedAt()
        );
    }
}
