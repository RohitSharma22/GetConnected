package com.smart.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {
	
	Random random = new Random(1000);

	@Autowired
	private EmailService emailService;

	
	
	//email id form open handler
	@RequestMapping("/forget")
	public String openEmailForm()
	{
		return "forget_email_form";
	}
	
	
	//email id form open handler
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email,HttpSession session)
	{
		System.out.println("Email "+email);
		
		//Generating otp of 4digit number
		
		
		int otp = random.nextInt(999999);
		System.out.println("OTP "+otp);
	
		//Code for sending otp to email..
		
		String subject="OTP from SCM";
		String message="<h1> OTP = "+otp+"</h1>";
		String to=email;
		boolean flag = this.emailService.sendEmail(subject, message, to);
		
		if(flag)
		{
			
			return "verify_otp";
			
			
			
		}
		else {
			
			session.setAttribute("message", "Check your email id");
			
			return "forgot_email_form";
		}
		

	}
	
	
}
