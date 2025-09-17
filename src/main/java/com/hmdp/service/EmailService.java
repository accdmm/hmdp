package com.hmdp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service // 标记为 Spring 管理的 Bean
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender; // 自动注入 JavaMailSender 实例

    /**
     * 发送简单的文本邮件
     *
     * @param to 收件人邮箱地址
     * @param subject 邮件主题
     * @param text 邮件正文内容
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("366169536@qq.com"); // 发件人邮箱，必须与 application.yml 中配置的一致
        message.setTo(to); // 收件人邮箱
        message.setSubject(subject); // 邮件主题
        message.setText(text); // 邮件正文

        // 调用 JavaMailSender 发送邮件
        javaMailSender.send(message);
    }

    /**
     * 生成并发送验证码邮件
     *
     * @param to 收件人邮箱地址
     * @return 生成的验证码字符串
     */
    public String sendVerificationCode(String to) {
        // 生成一个6位的随机数字验证码
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));

        // 设置邮件主题和正文
        String subject = "【Accdmm】登录验证码";
        String text = "亲爱的飞舞，\n\n您的登录验证码是：\n\n" + code + "\n\n请在3分钟内输入，不然小鸣人就不等你了哦！";

        // 调用方法发送邮件
        sendSimpleEmail(to, subject, text);

        // 返回生成的验证码，以便后续业务逻辑（如存入Redis）使用
        return code;
    }
}