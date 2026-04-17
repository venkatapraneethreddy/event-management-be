package com.college.eventclub.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.college.eventclub.model.Club;
import com.college.eventclub.model.Event;
import com.college.eventclub.model.EventRegistration;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    // Optional injection — null if spring-mail not configured
    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@eventclub.com}")
    private String fromAddress;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    private boolean isMailConfigured() {
        return mailSender != null
            && mailUsername != null
            && !mailUsername.isBlank()
            && !mailUsername.equals("noreply@eventclub.com");
    }

    @Async
    public void sendClubApproved(Club club) {
        if (!isMailConfigured()) { log.debug("Mail not configured — skipping club approved email"); return; }
        String to = club.getCreatedBy().getEmail();
        String name = club.getCreatedBy().getFullName();
        String clubName = club.getClubName();
        String html = buildClubApprovedHtml(name, clubName);
        send(to, "Your club \"" + clubName + "\" has been approved!", html);
    }

    @Async
    public void sendClubRejected(Club club) {
        if (!isMailConfigured()) { log.debug("Mail not configured — skipping club rejected email"); return; }
        String to = club.getCreatedBy().getEmail();
        String name = club.getCreatedBy().getFullName();
        String clubName = club.getClubName();
        String html = buildClubRejectedHtml(name, clubName);
        send(to, "Update on your club application: \"" + clubName + "\"", html);
    }

    @Async
    public void sendRegistrationConfirmed(EventRegistration reg) {
        if (!isMailConfigured()) { log.debug("Mail not configured — skipping registration email"); return; }
        String to = reg.getUser().getEmail();
        String name = reg.getUser().getFullName();
        Event event = reg.getEvent();
        String eventName = event.getTitle();
        String location = event.getLocation() != null ? event.getLocation() : "To be announced";
        String date = event.getEventDate() != null
            ? event.getEventDate().toString().replace("T", " at ").substring(0, 19) : "Date TBA";
        String ticketId = "#EC-" + reg.getRegistrationId();
        String html = buildRegistrationHtml(name, eventName, location, date, ticketId);
        send(to, "You're registered for \"" + eventName + "\"!", html);
    }

    @Async
    public void sendPaymentConfirmed(EventRegistration reg) {
        if (!isMailConfigured()) { log.debug("Mail not configured — skipping payment email"); return; }
        String to = reg.getUser().getEmail();
        String name = reg.getUser().getFullName();
        Event event = reg.getEvent();
        String amount = "Rs." + (event.getFee() != null ? event.getFee().longValue() : 0);
        String html = buildPaymentHtml(name, amount, event.getTitle());
        send(to, "Payment confirmed for \"" + event.getTitle() + "\"", html);
    }

    private void send(String to, String subject, String html) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(msg);
            log.info("Email sent to {} — {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private String buildClubApprovedHtml(String name, String clubName) {
        return emailWrapper("Club Approved! 🎉",
            "<p style='margin:0 0 12px;color:#374151'>Hi <strong>" + esc(name) + "</strong>,</p>" +
            "<p style='margin:0 0 16px;color:#374151'>Great news! Your club application for <strong style='color:#3b5bff'>\"" + esc(clubName) + "\"</strong> has been approved by the administrator.</p>" +
            "<p style='margin:0 0 20px;color:#374151'>You can now create and publish events for your club. Head over to your dashboard to get started.</p>" +
            "<a href='" + frontendUrl + "/organizer/dashboard' style='display:inline-block;background:#3b5bff;color:#fff;padding:12px 28px;border-radius:8px;text-decoration:none;font-weight:600;font-size:15px'>Go to Organizer Dashboard →</a>");
    }

    private String buildClubRejectedHtml(String name, String clubName) {
        return emailWrapper("Club Application Update",
            "<p style='margin:0 0 12px;color:#374151'>Hi <strong>" + esc(name) + "</strong>,</p>" +
            "<p style='margin:0 0 16px;color:#374151'>Unfortunately, your club application for <strong>\"" + esc(clubName) + "\"</strong> was not approved at this time.</p>" +
            "<p style='margin:0 0 20px;color:#374151'>Please contact your platform administrator for more details or to reapply.</p>" +
            "<a href='" + frontendUrl + "/organizer/dashboard' style='display:inline-block;background:#6b7280;color:#fff;padding:12px 28px;border-radius:8px;text-decoration:none;font-weight:600;font-size:15px'>Go to Dashboard</a>");
    }

    private String buildRegistrationHtml(String name, String eventName, String location, String date, String ticketId) {
        return emailWrapper("You're Registered! 🎫",
            "<p style='margin:0 0 12px;color:#374151'>Hi <strong>" + esc(name) + "</strong>,</p>" +
            "<p style='margin:0 0 16px;color:#374151'>Your registration for <strong style='color:#3b5bff'>\"" + esc(eventName) + "\"</strong> is confirmed!</p>" +
            "<table style='width:100%;border-collapse:collapse;margin-bottom:20px'>" +
              "<tr><td style='padding:10px 14px;background:#f9fafb;border-radius:6px 6px 0 0;border:1px solid #e5e7eb;font-size:13px;color:#6b7280;font-weight:600'>📍 LOCATION</td><td style='padding:10px 14px;background:#f9fafb;border-radius:0 6px 0 0;border:1px solid #e5e7eb;border-left:none;font-size:14px;color:#111827'>" + esc(location) + "</td></tr>" +
              "<tr><td style='padding:10px 14px;background:#fff;border:1px solid #e5e7eb;border-top:none;font-size:13px;color:#6b7280;font-weight:600'>🗓 DATE &amp; TIME</td><td style='padding:10px 14px;background:#fff;border:1px solid #e5e7eb;border-top:none;border-left:none;font-size:14px;color:#111827'>" + esc(date) + "</td></tr>" +
              "<tr><td style='padding:10px 14px;background:#f9fafb;border-radius:0 0 0 6px;border:1px solid #e5e7eb;border-top:none;font-size:13px;color:#6b7280;font-weight:600'>🎫 TICKET ID</td><td style='padding:10px 14px;background:#f9fafb;border-radius:0 0 6px 0;border:1px solid #e5e7eb;border-top:none;border-left:none;font-size:14px;color:#3b5bff;font-weight:700;font-family:monospace'>" + esc(ticketId) + "</td></tr>" +
            "</table>" +
            "<a href='" + frontendUrl + "/student/my-registrations' style='display:inline-block;background:#3b5bff;color:#fff;padding:12px 28px;border-radius:8px;text-decoration:none;font-weight:600;font-size:15px'>View My Ticket →</a>" +
            "<p style='margin:20px 0 0;font-size:12px;color:#9ca3af'>Present the QR code on your ticket at the event entrance for check-in.</p>");
    }

    private String buildPaymentHtml(String name, String amount, String eventTitle) {
        return emailWrapper("Payment Confirmed ✅",
            "<p style='margin:0 0 12px;color:#374151'>Hi <strong>" + esc(name) + "</strong>,</p>" +
            "<p style='margin:0 0 16px;color:#374151'>Your payment of <strong style='color:#059669'>" + esc(amount) + "</strong> for <strong>\"" + esc(eventTitle) + "\"</strong> has been received and confirmed.</p>" +
            "<a href='" + frontendUrl + "/student/my-registrations' style='display:inline-block;background:#3b5bff;color:#fff;padding:12px 28px;border-radius:8px;text-decoration:none;font-weight:600;font-size:15px'>View My Ticket →</a>");
    }

    /** Wraps content in a branded email shell */
    private String emailWrapper(String headline, String body) {
        return "<!DOCTYPE html><html><body style='margin:0;padding:0;background:#f3f4f6;font-family:Inter,Helvetica,Arial,sans-serif'>" +
          "<table width='100%' cellpadding='0' cellspacing='0'><tr><td align='center' style='padding:40px 16px'>" +
          "<table width='560' cellpadding='0' cellspacing='0' style='max-width:560px;width:100%'>" +
            // Header
            "<tr><td style='background:linear-gradient(135deg,#1a237e,#3b5bff);border-radius:12px 12px 0 0;padding:28px 32px'>" +
              "<table width='100%'><tr>" +
                "<td><span style='display:inline-block;width:34px;height:34px;background:#fff;border-radius:8px;text-align:center;line-height:34px;font-size:16px;font-weight:900;color:#3b5bff'>E</span>" +
                "<span style='font-size:18px;font-weight:800;color:#fff;margin-left:10px;vertical-align:middle'>EventClub</span></td>" +
              "</tr></table>" +
            "</td></tr>" +
            // Body
            "<tr><td style='background:#fff;padding:32px;border-radius:0 0 12px 12px;border:1px solid #e5e7eb;border-top:none'>" +
              "<h2 style='margin:0 0 20px;font-size:22px;font-weight:800;color:#1d2939'>" + headline + "</h2>" +
              body +
            "</td></tr>" +
            // Footer
            "<tr><td style='padding:20px 0;text-align:center'>" +
              "<p style='margin:0;font-size:12px;color:#9ca3af'>© EventClub · College Event Management Platform</p>" +
              "<p style='margin:4px 0 0;font-size:12px;color:#9ca3af'>You received this email because you have an account on EventClub.</p>" +
            "</td></tr>" +
          "</table>" +
          "</td></tr></table></body></html>";
    }

    /** Escape HTML special characters for safe email interpolation */
    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
