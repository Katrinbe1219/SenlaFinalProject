package org.example.notification_service.email;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@PropertySource("classpath:application.properties")
public class EmailService {

    @Value("${mail.from}")
    private String from;

    private JavaMailSender mailSender;

    private static final Logger logger = LogManager.getLogger(EmailService.class);

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    @Async("emailExecutor")
    public void sendPriceASync(String toEmail, String goodName, BigDecimal targetPrice, BigDecimal newPrice){

            try {SimpleMailMessage message = new SimpleMailMessage();
                System.out.println("ASKJDAKSJDh");
                message.setTo(toEmail);
                message.setSubject(goodName);
                message.setFrom(from);
                message.setText(
                        "Цена снизилась для: " + goodName +
                                "\nВы ставили цену: " + targetPrice +
                                "\nЦена товара сейчас: " + newPrice
                );
                System.out.println("aslkdjalksdjasd sended");
                mailSender.send(message);
                System.out.println("SENDED!!!!");
            }
            catch (MailException e){
                logger.error("MailException EmailService sendPriceASync: " + e.getMessage());
            }
    }

    @Async("emailExecutor")
    public void sendAvailableASync(String toEmail, String goodName, Long shopId, String shopName, String shopAddress){

        try {SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(goodName);
            message.setFrom(from);
            message.setText(
                    "Товар появился для: " + goodName +
                            "\n Магазин " + shopId + " " + shopName + ", адрес: " + shopAddress

            );
            mailSender.send(message);}
        catch (MailException e){
            logger.error("MailException EmailService sendAvailableASync: " + e.getMessage());
        }



    }

}
