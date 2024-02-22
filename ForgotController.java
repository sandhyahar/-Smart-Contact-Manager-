package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.EmailService;
import com.smart.entity.User;
import com.smart.helper.Message;
import com.smart.repo.UserRepository;

@Controller
public class ForgotController {

	Random random = new Random(1000);

	@Autowired
	private EmailService emailService;

	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private BCryptPasswordEncoder bcryptPassword;
	
	@RequestMapping("/forgot")
	public String openeEmailForm() {

		return "forgot-email-form";
	}

	@RequestMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email, HttpSession session) {

		int otp = random.nextInt(999999);

		String subject = "OTP From SMS";

		String message = "" + "<div style='boarder:1px solid #e2e2e2; padding 20px'>" + "<h1>" + "OTP is " + "<b>" + otp
				+ "</b>" + "<h1>" + "</div>";

		String to = email;

		// write code send otp to email.

		boolean flag = this.emailService.sendEmail(subject, message, to);

		if (flag) {

			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			return "verify-otp";

		} else {
			session.setAttribute("message", new Message("Check your	email id !!", "success"));

			return "forgot-email-form";

		}

	}
	
	@RequestMapping("/verify-otp")
	public String verifyOTP(@RequestParam("otp") int otp,HttpSession session) {
		
		int myOtp = (int) session.getAttribute("myotp");
		String email = (String) session.getAttribute("email");
		
		if(myOtp == otp) {
			
			User user = this.userRepo.getUserByUserName(email);
			
			if(user==null) {
				
				session.setAttribute("message", "User does not exits with this email !!");
				
				return "forgot-email-form";

				
			}
			
			
			return "password-change-form";
		}else {
			
			session.setAttribute("message", "you have entered wrong otp !!");
			return "verify-otp";
		}
		
		
		
	}
	
	@RequestMapping("/change-password")
	public String changePassword(@RequestParam("newpassword") String newpassword,HttpSession session) {
		
		String email = (String) session.getAttribute("email");
		User user = this.userRepo.getUserByUserName(email);
		user.setPassword(this.bcryptPassword.encode(newpassword));
		this.userRepo.save(user);
		
		
		return "redirect:/signin?change=password changed successfully..";
		
		
		

	}

}
