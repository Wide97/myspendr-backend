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

    public void sendVerificationEmail(String to, String nome, String verifyLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("✅ Conferma la tua email - MySpendr");
        message.setText(
                "Ciao " + nome + ",\n\n" +
                        "Grazie per esserti registrato su MySpendr!\n" +
                        "Per completare la registrazione, clicca sul link qui sotto entro 24 ore:\n\n" +
                        verifyLink + "\n\n" +
                        "Se non hai richiesto questa registrazione, ignora questo messaggio.\n\n" +
                        "— Il team di MySpendr"
        );

        try {
            mailSender.send(message);
            log.info("Email di verifica inviata a {}", to);
        } catch (MailException e) {
            log.error("Errore durante l'invio dell'email di verifica a {}: {}", to, e.getMessage());
            throw new EmailSendingException("Errore durante l'invio dell'email a " + to, e);
        }
    }

    public void sendPasswordResetEmail(String to, String nome, String newPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("🔐 Recupero password - MySpendr");
        message.setText(
                "Ciao " + nome + ",\n\n" +
                        "Hai richiesto il recupero della password.\n" +
                        "La tua nuova password temporanea è:\n\n" +
                        newPassword + "\n\n" +
                        "Ti consigliamo di cambiarla appena possibile.\n\n" +
                        "— Il team di MySpendr"
        );

        try {
            mailSender.send(message);
            log.info("Email di recupero inviata a {}", to);
        } catch (MailException e) {
            log.error("Errore durante invio email recupero a {}: {}", to, e.getMessage());
            throw new EmailSendingException("Errore durante l'invio dell'email a " + to, e);
        }
    }


}
