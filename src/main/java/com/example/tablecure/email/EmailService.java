package com.example.tablecure.email;

import com.example.tablecure.entity.Order;
import com.example.tablecure.entity.OrderItem;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${mail.from}")
    private String fromAddress;

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
                            <!-- Header -->
                            <tr>
                              <td style="background:#1a1a2e;padding:32px 40px;text-align:center;">
                                <h1 style="margin:0;color:#ffffff;font-size:28px;letter-spacing:1px;">Tablecure</h1>
                              </td>
                            </tr>
                            <!-- Body -->
                            <tr>
                              <td style="padding:40px;">
                                <h2 style="margin:0 0 12px;color:#1a1a2e;font-size:22px;">Verify your email address</h2>
                                <p style="margin:0 0 24px;color:#555;font-size:15px;line-height:1.6;">
                                  Hi %s, thanks for signing up! Use the OTP below to verify your account.
                                  It expires in <strong>10 minutes</strong>.
                                </p>
                                <!-- OTP Block -->
                                <div style="background:#f0f4ff;border:2px dashed #4a6cf7;border-radius:8px;
                                            padding:24px;text-align:center;margin:0 0 32px;">
                                  <p style="margin:0 0 8px;color:#555;font-size:13px;text-transform:uppercase;
                                            letter-spacing:2px;">Your verification code</p>
                                  <span style="font-size:42px;font-weight:bold;color:#1a1a2e;letter-spacing:8px;">%s</span>
                                </div>
                                <!-- Button -->
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
                            <!-- Footer -->
                            <tr>
                              <td style="background:#f8f8f8;padding:20px 40px;text-align:center;
                                         border-top:1px solid #eee;">
                                <p style="margin:0;color:#aaa;font-size:12px;">
                                  &copy; 2025 Tablecure. All rights reserved.
                                </p>
                              </td>
                            </tr>
                          </table>
                        </td></tr>
                      </table>
                    </body>
                    </html>
                    """.formatted(name, otp, verifyLink);

            sendHtml(toEmail, "Verify your Tablecure account", html);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendOrderConfirmationEmail(Order order) {
        try {
            String toEmail = order.getUser().getEmail();
            String name = order.getUser().getName();

            StringBuilder itemRows = new StringBuilder();
            double subtotal = 0;
            for (OrderItem item : order.getOrderItems()) {
                double lineTotal = item.getPrice().doubleValue() * item.getQuantity();
                subtotal += lineTotal;
                itemRows.append("""
                        <tr>
                          <td style="padding:10px 8px;border-bottom:1px solid #f0f0f0;color:#333;font-size:14px;">%s</td>
                          <td style="padding:10px 8px;border-bottom:1px solid #f0f0f0;color:#333;font-size:14px;text-align:center;">%d</td>
                          <td style="padding:10px 8px;border-bottom:1px solid #f0f0f0;color:#333;font-size:14px;text-align:right;">₹%.2f</td>
                          <td style="padding:10px 8px;border-bottom:1px solid #f0f0f0;color:#333;font-size:14px;text-align:right;">₹%.2f</td>
                        </tr>
                        """.formatted(
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getPrice().doubleValue(),
                        lineTotal
                ));
            }

            String discountRow = "";
            if (order.getDiscountAmount() != null && order.getDiscountAmount() > 0) {
                discountRow = """
                        <tr>
                          <td colspan="3" style="padding:8px;text-align:right;color:#27ae60;font-size:14px;">
                            Discount (%s)
                          </td>
                          <td style="padding:8px;text-align:right;color:#27ae60;font-size:14px;">-₹%.2f</td>
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
                        """.formatted(
                        addr.getFullName(),
                        addr.getStreet(),
                        addr.getCity(),
                        addr.getState(),
                        addr.getPincode()
                );
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
                            <!-- Header -->
                            <tr>
                              <td style="background:#1a1a2e;padding:32px 40px;text-align:center;">
                                <h1 style="margin:0 0 4px;color:#ffffff;font-size:28px;letter-spacing:1px;">Tablecure</h1>
                                <p style="margin:0;color:#a0a8c8;font-size:14px;">Order Confirmation</p>
                              </td>
                            </tr>
                            <!-- Body -->
                            <tr>
                              <td style="padding:40px;">
                                <h2 style="margin:0 0 6px;color:#1a1a2e;font-size:22px;">Thank you, %s!</h2>
                                <p style="margin:0 0 24px;color:#555;font-size:15px;">
                                  Your order has been confirmed and is being processed.
                                </p>
                                <!-- Order Meta -->
                                <div style="display:flex;justify-content:space-between;margin-bottom:24px;">
                                  <div>
                                    <p style="margin:0;color:#999;font-size:12px;text-transform:uppercase;letter-spacing:1px;">Order ID</p>
                                    <p style="margin:4px 0 0;color:#1a1a2e;font-weight:bold;font-size:15px;">#%d</p>
                                  </div>
                                  <div style="text-align:right;">
                                    <p style="margin:0;color:#999;font-size:12px;text-transform:uppercase;letter-spacing:1px;">Order Date</p>
                                    <p style="margin:4px 0 0;color:#1a1a2e;font-size:14px;">%s</p>
                                  </div>
                                </div>
                                <!-- Items Table -->
                                <table width="100%%" cellpadding="0" cellspacing="0"
                                       style="border-collapse:collapse;margin-bottom:8px;">
                                  <thead>
                                    <tr style="background:#f8f8f8;">
                                      <th style="padding:10px 8px;text-align:left;color:#999;font-size:12px;text-transform:uppercase;letter-spacing:1px;">Product</th>
                                      <th style="padding:10px 8px;text-align:center;color:#999;font-size:12px;text-transform:uppercase;letter-spacing:1px;">Qty</th>
                                      <th style="padding:10px 8px;text-align:right;color:#999;font-size:12px;text-transform:uppercase;letter-spacing:1px;">Price</th>
                                      <th style="padding:10px 8px;text-align:right;color:#999;font-size:12px;text-transform:uppercase;letter-spacing:1px;">Total</th>
                                    </tr>
                                  </thead>
                                  <tbody>
                                    %s
                                  </tbody>
                                </table>
                                <!-- Totals -->
                                <table width="100%%" cellpadding="0" cellspacing="0" style="margin-bottom:0;">
                                  <tr>
                                    <td colspan="3" style="padding:8px;text-align:right;color:#555;font-size:14px;">Subtotal</td>
                                    <td style="padding:8px;text-align:right;color:#555;font-size:14px;">₹%.2f</td>
                                  </tr>
                                  %s
                                  <tr style="border-top:2px solid #1a1a2e;">
                                    <td colspan="3" style="padding:12px 8px;text-align:right;color:#1a1a2e;font-weight:bold;font-size:16px;">Total</td>
                                    <td style="padding:12px 8px;text-align:right;color:#1a1a2e;font-weight:bold;font-size:16px;">₹%.2f</td>
                                  </tr>
                                </table>
                                %s
                              </td>
                            </tr>
                            <!-- Footer -->
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
                    """.formatted(
                    name,
                    order.getId(),
                    orderDate,
                    itemRows.toString(),
                    subtotal,
                    discountRow,
                    order.getFinalAmount(),
                    addressBlock
            );

            sendHtml(toEmail, "Order Confirmed – #" + order.getId(), html);
        } catch (Exception e) {
            log.error("Failed to send order confirmation email for order {}: {}", order.getId(), e.getMessage());
        }
    }

    @Async
    public void sendCustomEmail(String toEmail, String name, String subject, String message) {
        try {
            // Convert newlines to <br> so multi-line messages render correctly
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
                            <!-- Header -->
                            <tr>
                              <td style="background:#1a1a2e;padding:32px 40px;text-align:center;">
                                <h1 style="margin:0;color:#ffffff;font-size:28px;letter-spacing:1px;">Tablecure</h1>
                              </td>
                            </tr>
                            <!-- Body -->
                            <tr>
                              <td style="padding:40px;">
                                <p style="margin:0 0 20px;color:#1a1a2e;font-size:16px;font-weight:bold;">
                                  Hi %s,
                                </p>
                                <div style="color:#444;font-size:15px;line-height:1.7;margin:0 0 32px;">
                                  %s
                                </div>
                                <p style="margin:0;color:#888;font-size:13px;">
                                  Warm regards,<br>
                                  <strong style="color:#1a1a2e;">The Tablecure Team</strong>
                                </p>
                              </td>
                            </tr>
                            <!-- Footer -->
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

            sendHtml(toEmail, subject, html);
        } catch (Exception e) {
            log.error("Failed to send custom email to {}: {}", toEmail, e.getMessage());
        }
    }

    private void sendHtml(String to, String subject, String html) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromAddress, "Tablecure");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(message);
    }
}
