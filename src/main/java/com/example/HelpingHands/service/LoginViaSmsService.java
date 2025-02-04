package com.example.HelpingHands.service;

import com.example.HelpingHands.entity.OtpEntity;
import com.example.HelpingHands.entity.UserEntity;
import com.example.HelpingHands.repository.OtpRepository;
import com.example.HelpingHands.repository.UserRepository;
import com.example.HelpingHands.request.LoginEmailRequest;
import com.example.HelpingHands.request.LoginSmsRequest;
import com.example.HelpingHands.request.VerifyMailOtp;
import com.example.HelpingHands.request.VerifySmsOtp;
import com.example.HelpingHands.response.PortalResponse;
import com.example.HelpingHands.utility.MailUtility;
import com.example.HelpingHands.utility.PhoneUtility;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Date;

@Service
public class LoginViaSmsService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    OtpRepository otpRepository;

    @Autowired
    PhoneUtility phoneUtility;

    public PortalResponse sendOtp(@RequestBody LoginSmsRequest req){

        UserEntity userEntity1 = userRepository.findByPhone(req.getPhone());
        OtpEntity otpEntity = new OtpEntity();
        PortalResponse portalResponse = new PortalResponse();

        if (userEntity1 != null) {
            String otp = "";
            String val = "1234567890";
            for(int i = 0; i < 4; i++){
                int z = (int)(Math.random() * val.length());
                otp = otp + z;
            }

            String smsDesc = "Your one time password is " + otp;

            phoneUtility.sendSms(req.getPhone(),smsDesc);

            otpEntity.setEmail(userEntity1.getEmail());
            otpEntity.setPhone(userEntity1.getPhone());
            otpEntity.setOtp(otp);
            otpRepository.save(otpEntity);

            portalResponse.setMessage("Sms send");
            portalResponse.setStatusCode("200");
        } else {
            portalResponse.setMessage("You need to register First");
            portalResponse.setStatusCode("202");
        }

        return portalResponse;
    }

    //=================================================VERIFY OTP=======================================================

    public PortalResponse verifyOtp(@RequestBody VerifySmsOtp req){
        PortalResponse portalResponse = new PortalResponse();
        UserEntity userEntity1 = userRepository.findByPhone(req.getPhone());
        OtpEntity otpEntity1 = otpRepository.findByPhone(req.getPhone());
        String otp1 = otpEntity1.getOtp();
        if(otp1.equals(req.getOtp())){

            String str = "";
            String val = "1234567890";
            for(int i = 0; i < 4; i++){
                int z = (int)(Math.random() * val.length());
                str = str + z;
            }

            String token= Jwts.builder()
                    .setId(userEntity1.getEmail())
                    .setIssuedAt(new java.util.Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis()+1000*100))
                    .signWith(SignatureAlgorithm.HS256,str)
                    .compact();

            userEntity1.setToken(token);
            userEntity1.setSigningKey(str);
            userRepository.save(userEntity1);

            portalResponse.setMessage("Login successful");
            portalResponse.setStatusCode("200");
            portalResponse.setToken(token);
        } else {
            portalResponse.setMessage("Invalid OTP");
            portalResponse.setStatusCode("202");
        }
        return portalResponse;
    }
}
