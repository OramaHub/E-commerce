package com.orama.e_commerce.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

  private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

  private final JavaMailSender mailSender;

  public EmailService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  public void sendPasswordResetEmail(String to, String name, String resetLink) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setTo(to);
      helper.setSubject("Redefinição de Senha");
      helper.setText(buildResetEmailHtml(name, resetLink), true);

      mailSender.send(message);
      logger.info("Password reset email sent to: {}", to);
    } catch (MessagingException e) {
      logger.error("Failed to send password reset email to: {}", to, e);
    }
  }

  private String buildResetEmailHtml(String name, String resetLink) {
    return "<!DOCTYPE html>"
        + "<html><head><meta charset=\"UTF-8\"></head><body style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;\">"
        + "<h2 style=\"color: #333;\">Redefinição de Senha</h2>"
        + "<p>Olá, <strong>"
        + name
        + "</strong>!</p>"
        + "<p>Recebemos uma solicitação para redefinir sua senha. Clique no botão abaixo para criar uma nova senha:</p>"
        + "<div style=\"text-align: center; margin: 30px 0;\">"
        + "<a href=\""
        + resetLink
        + "\" style=\"background-color: #007bff; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; font-size: 16px;\">Redefinir Senha</a>"
        + "</div>"
        + "<p>Se você não solicitou essa alteração, ignore este email. Sua senha permanecerá a mesma.</p>"
        + "<p>Este link expira em <strong>30 minutos</strong>.</p>"
        + "<hr style=\"border: none; border-top: 1px solid #eee; margin: 20px 0;\">"
        + "<p style=\"color: #999; font-size: 12px;\">Se o botão não funcionar, copie e cole este link no seu navegador: "
        + resetLink
        + "</p>"
        + "</body></html>";
  }
}
