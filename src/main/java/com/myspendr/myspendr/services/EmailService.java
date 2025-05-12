package com.myspendr.myspendr.services;

import com.myspendr.myspendr.exceptions.EmailSendingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendWelcomeEmail(String to, String nome) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("🎉 Benvenuto su MySpendr!");
        message.setText("Ciao " + nome + ",\n\nGrazie per esserti registrato su MySpendr! Ora puoi iniziare a tracciare le tue spese in modo semplice e veloce.\n\nBuon risparmio!");

        try {
            mailSender.send(message);
            log.info("Email di benvenuto inviata a {}", to);
        } catch (MailException e) {
            log.error("Errore durante l'invio dell'email a {}: {}", to, e.getMessage());
            throw new EmailSendingException("Errore durante l'invio dell'email a " + to, e);
        }
    }
}
