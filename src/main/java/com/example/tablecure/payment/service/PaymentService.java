package com.example.tablecure.payment.service;

import com.razorpay.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class PaymentService {

    @Value("${razorpay.key}")
    private String key;

    @Value("${razorpay.secret}")
    private String secret;

    public String createOrder(int amount) throws Exception {

        RazorpayClient client = new RazorpayClient(key, secret);

        JSONObject options = new JSONObject();
        options.put("amount", amount * 100); // paise
        options.put("currency", "INR");
        options.put("receipt", "txn_123456");

        Order order = client.orders.create(options);

        return order.toString();
    }
    public boolean verifyPayment(String orderId, String paymentId, String signature) throws Exception {

        String data = orderId + "|" + paymentId;

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));

        byte[] rawHmac = mac.doFinal(data.getBytes());
        String generatedSignature = Hex.encodeHexString(rawHmac);

        return MessageDigest.isEqual(
                generatedSignature.getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8)
        );
    }
}