package edu.ngd.service;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class SmsService {

    @Value("${sms.aliyun.enabled:false}")
    private boolean enabled;

    @Value("${sms.aliyun.access-key-id:}")
    private String accessKeyId;

    @Value("${sms.aliyun.access-key-secret:}")
    private String accessKeySecret;

    @Value("${sms.aliyun.sign-name:}")
    private String signName;

    @Value("${sms.aliyun.template-code:}")
    private String templateCode;

    private DefaultAcsClient client;

    @PostConstruct
    public void init() {
        if (enabled) {
            try {
                DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
                this.client = new DefaultAcsClient(profile);
                log.info("Aliyun SMS service initialized for sign: {}", signName);
            } catch (Exception e) {
                log.error("Failed to initialize Aliyun SMS client: {}", e.getMessage());
                this.enabled = false;
            }
        } else {
            log.info("Aliyun SMS is disabled, verification codes will be logged to console only");
        }
    }

    public void sendVerificationCode(String phone, String code) {
        if (!enabled || client == null) {
            log.info("[DEV MODE] Verification code for {}: {}", phone, code);
            return;
        }

        try {
            SendSmsRequest request = new SendSmsRequest();
            request.setPhoneNumbers(phone);
            request.setSignName(signName);
            request.setTemplateCode(templateCode);
            request.setTemplateParam("{\"code\":\"" + code + "\"}");

            SendSmsResponse response = client.getAcsResponse(request);
            log.info("SMS sent to {}: code={}, requestId={}", phone, code, response.getRequestId());
        } catch (ClientException e) {
            log.error("Failed to send SMS to {}: {}", phone, e.getMessage());
            throw new RuntimeException("短信发送失败: " + e.getErrMsg());
        }
    }
}
