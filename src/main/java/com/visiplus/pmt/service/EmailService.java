package com.visiplus.pmt.service;

public interface EmailService {
    void sendTaskAssignmentEmail(String to, String subject, String body);
}
