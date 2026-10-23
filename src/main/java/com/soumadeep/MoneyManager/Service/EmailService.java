package com.soumadeep.MoneyManager.Service;

import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";

    @Value("${BREVO_API_KEY}")
    private String apiKey;

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromEmail;

    private final RestClient restClient;

    public EmailService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(15));
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    public void sendEmail(String to, String subject, String body) {
        send(to, subject, body, null, null);
    }

    public void sendEmailWithAttachment(String to, String subject, String body,
                                        byte[] attachment, String fileName) throws MessagingException {
        send(to, subject, body, attachment, fileName);
    }

    private void send(String to, String subject, String body, byte[] attachment, String fileName) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("sender", Map.of("email", fromEmail));
        payload.put("to", List.of(Map.of("email", to)));
        payload.put("subject", subject);
        payload.put("textContent", body);
        if (attachment != null) {
            payload.put("attachment", List.of(Map.of(
                    "name", fileName,
                    "content", Base64.getEncoder().encodeToString(attachment))));
        }
        try {
            restClient.post()
                    .uri(BREVO_URL)
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }
}