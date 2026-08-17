package com.example.urlshortener.controller;

import com.example.urlshortener.dto.CreateUrlRequest;
import com.example.urlshortener.dto.UrlResponse;
import com.example.urlshortener.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

@RestController
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/api/urls")
    public ResponseEntity<?> createShortUrl(@RequestBody(required = false) CreateUrlRequest request, HttpServletRequest servletRequest) {
        String clientIp = getClientIp(servletRequest);

        if (!urlService.allowRateLimit(clientIp)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", "Rate limit exceeded. Maximum 10 requests per minute."));
        }

        if (request == null || request.originalUrl() == null || !urlService.isValidUrl(request.originalUrl())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid URL"));
        }

        UrlResponse response = urlService.createShortUrl(request.originalUrl());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{shortCode:[a-zA-Z0-9]{6}}")
    public ResponseEntity<?> redirectToOriginalUrl(@PathVariable String shortCode) {
        Optional<String> originalUrlOpt = urlService.getAndIncrementOriginalUrl(shortCode);

        if (originalUrlOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Short URL not found"));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(originalUrlOpt.get()));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/api/urls/{shortCode}/stats")
    public ResponseEntity<?> getUrlStats(@PathVariable String shortCode) {
        Optional<UrlResponse> statsOpt = urlService.getStats(shortCode);

        if (statsOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Short URL not found"));
        }

        return ResponseEntity.ok(statsOpt.get());
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.trim().isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
