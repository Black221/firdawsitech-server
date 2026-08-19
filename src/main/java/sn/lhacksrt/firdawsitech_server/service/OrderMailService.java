package sn.lhacksrt.firdawsitech_server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import sn.lhacksrt.firdawsitech_server.dto.OrderResponse;

@Service
@RequiredArgsConstructor
public class OrderMailService {

    private final SpringTemplateEngine templateEngine;
    private final MailService mail;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.mail.admin:}")
    private String admin;

    public void sendConfirmation(OrderResponse order, String customerName, String customerEmail) {
        if (customerEmail == null || customerEmail.isBlank()) return;

        Context ctx = new Context();
        ctx.setVariable("customerName", customerName != null ? customerName : "Client");
        ctx.setVariable("orderNumber", order.orderNumber());
        ctx.setVariable("createdAt", order.createdAt());
        ctx.setVariable("items", order.items());
        ctx.setVariable("total", order.totalAmount());

        String html = templateEngine.process("mail/order-confirmation.html", ctx);

        mail.sendHtml(from, customerEmail, "Confirmation de commande #" + order.orderNumber(), html);

        // Optionnel: copie admin
        if (admin != null && !admin.isBlank()) {
            mail.sendHtml(from, admin, "[Copie] Nouvelle commande #" + order.orderNumber(), html);
        }
    }
}
