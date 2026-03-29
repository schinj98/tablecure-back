package com.example.tablecure.email;

import com.example.tablecure.entity.Order;
import com.example.tablecure.entity.OrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final RestTemplate restTemplate;

    @Value("${brevo.api-key}")
    private String brevoApiKey;

    @Value("${mail.from}")
    private String fromEmail;

    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";

    @Async
    public void sendVerificationEmail(String toEmail, String name, String otp, String frontendUrl) {
        try {
            String encodedEmail = URLEncoder.encode(toEmail, StandardCharsets.UTF_8);
            String verifyLink = frontendUrl + "/verify-email?email=" + encodedEmail + "&otp=" + otp;

            String html = """
                    <!DOCTYPE html>
                    <html>
                    <head><meta charset="UTF-8"></head>
                    <body style="margin:0;padding:0;background:#f4f4f4;font-family:Arial,sans-serif;">
                      <table width="100%%" cellpadding="0" cellspacing="0">
                        <tr><td align="center" style="padding:40px 0;">
                          <table width="600" cellpadding="0" cellspacing="0"
                                 style="background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">
                            <tr>
                              <td style="background:#1a1a2e;padding:32px 40px;text-align:center;">
                                <h1 style="margin:0;color:#ffffff;font-size:28px;letter-spacing:1px;">Tablecure</h1>
                              </td>
                            </tr>
                            <tr>
                              <td style="padding:40px;">
                                <h2 style="margin:0 0 12px;color:#1a1a2e;font-size:22px;">Verify your email address</h2>
                                <p style="margin:0 0 24px;color:#555;font-size:15px;line-height:1.6;">
                                  Hi %s, thanks for signing up! Use the OTP below to verify your account.
                                  It expires in <strong>10 minutes</strong>.
                                </p>
                                <div style="background:#f0f4ff;border:2px dashed #4a6cf7;border-radius:8px;
                                            padding:24px;text-align:center;margin:0 0 32px;">
                                  <p style="margin:0 0 8px;color:#555;font-size:13px;text-transform:uppercase;
                                            letter-spacing:2px;">Your verification code</p>
                                  <span style="font-size:42px;font-weight:bold;color:#1a1a2e;letter-spacing:8px;">%s</span>
                                </div>
                                <div style="text-align:center;margin:0 0 32px;">
                                  <a href="%s"
                                     style="background:#4a6cf7;color:#ffffff;text-decoration:none;
                                            padding:14px 36px;border-radius:6px;font-size:15px;
                                            font-weight:bold;display:inline-block;">
                                    Verify Email
                                  </a>
                                </div>
                                <p style="margin:0;color:#999;font-size:13px;text-align:center;">
                                  If you didn't create an account, you can safely ignore this email.
                                </p>
                              </td>
                            </tr>
                            <tr>
                              <td style="background:#f8f8f8;padding:20px 40px;text-align:center;border-top:1px solid #eee;">
                                <p style="margin:0;color:#aaa;font-size:12px;">&copy; 2025 Tablecure. All rights reserved.</p>
                              </td>
                            </tr>
                          </table>
                        </td></tr>
                      </table>
                    </body>
                    </html>
                    """.formatted(name, otp, verifyLink);

            sendHtml(toEmail, name, "Verify your Tablecure account", html);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendOrderConfirmationEmail(Order order) {
        try {
            String toEmail = order.getUser().getEmail();
            String name    = order.getUser().getName();

            StringBuilder itemRows = new StringBuilder();
            double subtotal = 0;
            for (OrderItem item : order.getOrderItems()) {
                double lineTotal = item.getPrice().doubleValue() * item.getQuantity();
                subtotal += lineTotal;
                itemRows.append("""
                        <tr>
                          <td style="padding:10px 8px;border-bottom:1px solid #f0f0f0;color:#333;font-size:14px;">%s</td>
                          <td style="padding:10px 8px;border-bottom:1px solid #f0f0f0;color:#333;font-size:14px;text-align:center;">%d</td>
                          <td style="padding:10px 8px;border-bottom:1px solid #f0f0f0;color:#333;font-size:14px;text-align:right;">&#8377;%.2f</td>
                          <td style="padding:10px 8px;border-bottom:1px solid #f0f0f0;color:#333;font-size:14px;text-align:right;">&#8377;%.2f</td>
                        </tr>
                        """.formatted(item.getProduct().getName(), item.getQuantity(),
                        item.getPrice().doubleValue(), lineTotal));
            }

            String discountRow = "";
            if (order.getDiscountAmount() != null && order.getDiscountAmount() > 0) {
                discountRow = """
                        <tr>
                          <td colspan="3" style="padding:8px;text-align:right;color:#27ae60;font-size:14px;">
                            Discount (%s)
                          </td>
                          <td style="padding:8px;text-align:right;color:#27ae60;font-size:14px;">-&#8377;%.2f</td>
                        </tr>
                        """.formatted(order.getCouponCode(), order.getDiscountAmount());
            }

            String addressBlock = "";
            if (order.getAddress() != null) {
                var addr = order.getAddress();
                addressBlock = """
                        <div style="background:#f8f8f8;border-radius:6px;padding:16px;margin-top:24px;">
                          <p style="margin:0 0 8px;font-weight:bold;color:#1a1a2e;font-size:14px;">Shipping Address</p>
                          <p style="margin:0;color:#555;font-size:14px;line-height:1.7;">
                            %s<br>%s, %s %s<br>%s
                          </p>
                        </div>
                        """.formatted(addr.getFullName(), addr.getStreet(),
                        addr.getCity(), addr.getState(), addr.getPincode());
            }

            String orderDate = order.getOrderDate()
                    .format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));

            String html = """
                    <!DOCTYPE html>
                    <html>
                    <head><meta charset="UTF-8"></head>
                    <body style="margin:0;padding:0;background:#f4f4f4;font-family:Arial,sans-serif;">
                      <table width="100%%" cellpadding="0" cellspacing="0">
                        <tr><td align="center" style="padding:40px 0;">
                          <table width="600" cellpadding="0" cellspacing="0"
                                 style="background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">
                            <tr>
                              <td style="background:#1a1a2e;padding:32px 40px;text-align:center;">
                                <h1 style="margin:0 0 4px;color:#ffffff;font-size:28px;letter-spacing:1px;">Tablecure</h1>
                                <p style="margin:0;color:#a0a8c8;font-size:14px;">Order Confirmation</p>
                              </td>
                            </tr>
                            <tr>
                              <td style="padding:40px;">
                                <h2 style="margin:0 0 6px;color:#1a1a2e;font-size:22px;">Thank you, %s!</h2>
                                <p style="margin:0 0 24px;color:#555;font-size:15px;">
                                  Your order has been confirmed and is being processed.
                                </p>
                                <table width="100%%" cellpadding="0" cellspacing="0" style="margin-bottom:16px;">
                                  <tr>
                                    <td>
                                      <p style="margin:0;color:#999;font-size:12px;text-transform:uppercase;letter-spacing:1px;">Order ID</p>
                                      <p style="margin:4px 0 0;color:#1a1a2e;font-weight:bold;font-size:15px;">#%d</p>
                                    </td>
                                    <td style="text-align:right;">
                                      <p style="margin:0;color:#999;font-size:12px;text-transform:uppercase;letter-spacing:1px;">Order Date</p>
                                      <p style="margin:4px 0 0;color:#1a1a2e;font-size:14px;">%s</p>
                                    </td>
                                  </tr>
                                </table>
                                <table width="100%%" cellpadding="0" cellspacing="0" style="border-collapse:collapse;margin-bottom:8px;">
                                  <thead>
                                    <tr style="background:#f8f8f8;">
                                      <th style="padding:10px 8px;text-align:left;color:#999;font-size:12px;text-transform:uppercase;">Product</th>
                                      <th style="padding:10px 8px;text-align:center;color:#999;font-size:12px;text-transform:uppercase;">Qty</th>
                                      <th style="padding:10px 8px;text-align:right;color:#999;font-size:12px;text-transform:uppercase;">Price</th>
                                      <th style="padding:10px 8px;text-align:right;color:#999;font-size:12px;text-transform:uppercase;">Total</th>
                                    </tr>
                                  </thead>
                                  <tbody>%s</tbody>
                                </table>
                                <table width="100%%" cellpadding="0" cellspacing="0">
                                  <tr>
                                    <td colspan="3" style="padding:8px;text-align:right;color:#555;font-size:14px;">Subtotal</td>
                                    <td style="padding:8px;text-align:right;color:#555;font-size:14px;">&#8377;%.2f</td>
                                  </tr>
                                  %s
                                  <tr style="border-top:2px solid #1a1a2e;">
                                    <td colspan="3" style="padding:12px 8px;text-align:right;color:#1a1a2e;font-weight:bold;font-size:16px;">Total</td>
                                    <td style="padding:12px 8px;text-align:right;color:#1a1a2e;font-weight:bold;font-size:16px;">&#8377;%.2f</td>
                                  </tr>
                                </table>
                                %s
                              </td>
                            </tr>
                            <tr>
                              <td style="background:#f8f8f8;padding:20px 40px;text-align:center;border-top:1px solid #eee;">
                                <p style="margin:0;color:#aaa;font-size:12px;">&copy; 2025 Tablecure. All rights reserved.</p>
                              </td>
                            </tr>
                          </table>
                        </td></tr>
                      </table>
                    </body>
                    </html>
                    """.formatted(name, order.getId(), orderDate,
                    itemRows.toString(), subtotal, discountRow, order.getFinalAmount(), addressBlock);

            sendHtml(toEmail, name, "Order Confirmed – #" + order.getId(), html);
        } catch (Exception e) {
            log.error("Failed to send order confirmation email for order {}: {}", order.getId(), e.getMessage());
        }
    }

    @Async
    public void sendCustomEmail(String toEmail, String name, String subject, String message) {
        try {
            String htmlMessage = message.replace("\n", "<br>");
            String html = """
                    <!DOCTYPE html>
                    <html>
                    <head><meta charset="UTF-8"></head>
                    <body style="margin:0;padding:0;background:#f4f4f4;font-family:Arial,sans-serif;">
                      <table width="100%%" cellpadding="0" cellspacing="0">
                        <tr><td align="center" style="padding:40px 0;">
                          <table width="600" cellpadding="0" cellspacing="0"
                                 style="background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">
                            <tr>
                              <td style="background:#1a1a2e;padding:32px 40px;text-align:center;">
                                <h1 style="margin:0;color:#ffffff;font-size:28px;letter-spacing:1px;">Tablecure</h1>
                              </td>
                            </tr>
                            <tr>
                              <td style="padding:40px;">
                                <p style="margin:0 0 20px;color:#1a1a2e;font-size:16px;font-weight:bold;">Hi %s,</p>
                                <div style="color:#444;font-size:15px;line-height:1.7;margin:0 0 32px;">%s</div>
                                <p style="margin:0;color:#888;font-size:13px;">
                                  Warm regards,<br>
                                  <strong style="color:#1a1a2e;">The Tablecure Team</strong>
                                </p>
                              </td>
                            </tr>
                            <tr>
                              <td style="background:#f8f8f8;padding:20px 40px;text-align:center;border-top:1px solid #eee;">
                                <p style="margin:0;color:#aaa;font-size:12px;">&copy; 2025 Tablecure. All rights reserved.</p>
                              </td>
                            </tr>
                          </table>
                        </td></tr>
                      </table>
                    </body>
                    </html>
                    """.formatted(name, htmlMessage);

            sendHtml(toEmail, name, subject, html);
        } catch (Exception e) {
            log.error("Failed to send custom email to {}: {}", toEmail, e.getMessage());
        }
    }

    private void sendHtml(String toEmail, String toName, String subject, String html) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", brevoApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "sender",      Map.of("name", "Tablecure", "email", fromEmail),
                "to",          List.of(Map.of("email", toEmail, "name", toName != null ? toName : toEmail)),
                "subject",     subject,
                "htmlContent", html
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(BREVO_URL, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("Brevo API error for {}: {} {}", toEmail, response.getStatusCode(), response.getBody());
        }
    }
}
