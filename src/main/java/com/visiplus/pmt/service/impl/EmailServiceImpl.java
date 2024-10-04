package com.visiplus.pmt.service.impl;

import com.visiplus.pmt.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    // Logger for logging information and error messages
    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    // JavaMailSender is responsible for sending email messages
    private final JavaMailSender mailSender;

    // Constructor-based dependency injection for JavaMailSender
    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends an email notification for task assignment.
     *
     * @param to the recipient's email address
     * @param subject the email subject
     * @param body the email content
     */
    @Override
    public void sendTaskAssignmentEmail(String to, String subject, String body) {
        // Create a simple mail message object and set its properties
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        try {
            // Log the email sending attempt
            logger.info("Attempting to send email to {}", to);

            // Send the email using the JavaMailSender
            mailSender.send(message);

            // Log successful email sending
            logger.info("Email sent successfully to {}", to);
        } catch (Exception e) {
            // Log any failure during email sending
            logger.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
