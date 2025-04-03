package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.AddressDTO;
import com.hcmute.devhire.DTOs.JobDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmailService implements IEmailService {
    private final JavaMailSender javaMailSender;

    @Override
    public void sendEmail(String to, String subject, String text) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, true);

        javaMailSender.send(message);
    }

    @Override
    public void sendEmail(String to, String subject, List<JobDTO> jobs) throws MessagingException {
        // Create a new MimeMessage
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        // Set the recipient and subject of the email
        helper.setTo(to);
        helper.setSubject(subject);

        // Create the HTML content for the email
        StringBuilder emailContent = new StringBuilder();
        emailContent.append("<html>")
                .append("<head>")
                .append("<style>")
                .append("table { width: 100%; border-collapse: collapse; }")
                .append("th, td { padding: 10px; border: 1px solid #ddd; text-align: left; }")
                .append("th { background-color: #f2f2f2; }")
                .append("tr:nth-child(even) { background-color: #f9f9f9; }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<h3>Here are some new job opportunities for you:</h3>")
                .append("<table>")
                .append("<tr><th>Job Title</th><th>Company</th><th>Salary</th><th>Location</th><th>Deadline</th></tr>");
        DecimalFormat salaryFormat = new DecimalFormat("#,###");
        // Iterate through the jobs list and add them to the table
        for (JobDTO job : jobs) {
            String salaryStart = job.getSalaryStart() != null ? salaryFormat.format(job.getSalaryStart()) : "N/A";
            String salaryEnd = job.getSalaryEnd() != null ? salaryFormat.format(job.getSalaryEnd()) : "N/A";

            emailContent.append("<tr>")
                    .append("<td>").append(job.getTitle()).append("</td>")
                    .append("<td>").append(job.getCompany().getName()).append("</td>")
                    .append("<td>").append(salaryStart).append(" - ").append(salaryEnd).append("</td>")
                    .append("<td>").append(
                            job.getAddresses().stream()
                                    .map(AddressDTO::getCity)
                                    .filter(Objects::nonNull)
                                    .distinct()
                                    .collect(Collectors.joining(", "))
                    ).append("</td>")
                    .append("<td>").append(job.getDeadline()).append("</td>")
                    .append("</tr>");
        }

        emailContent.append("</table>")
                .append("<p>Click <a href='http://localhost:3000/jobs'>here</a> to view more details.</p>")
                .append("</body>")
                .append("</html>");

        // Set the HTML content of the email
        helper.setText(emailContent.toString(), true);

        // Send the email
        javaMailSender.send(message);
    }
}
