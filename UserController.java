package com.smart.controller;

import java.io.File;
import java.lang.StackWalker.Option;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.razorpay.*;


import javax.servlet.http.HttpSession;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.smart.entity.Contact;
import com.smart.entity.User;
import com.smart.helper.Message;
import com.smart.repo.ContactRepository;
import com.smart.repo.UserRepository;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private BCryptPasswordEncoder bryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepo;

	@Autowired
	private ContactRepository contactRepo;

	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println("USERNAME " + userName);

		User user = userRepo.getUserByUserName(userName);

		model.addAttribute("user", user);

	}

	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {

		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}

	@RequestMapping("/add-contact")
	public String openAddContactForm(Model model) {

		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());

		return "normal/add_contact_form";
	}

	@RequestMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {
		try {
			String name = principal.getName();
			User user = this.userRepo.getUserByUserName(name);

			// Processing and uploading images

			if (file.isEmpty()) {

				System.out.println("File is empty");
				contact.setImage("contact.png");

			} else {

				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}

			contact.setUser(user);

			user.getContacts().add(contact);

			this.userRepo.save(user);

			// message success
			session.setAttribute("message", new Message("Your contact is added !! And More..", "success"));

		} catch (Exception e) {
			session.setAttribute("message", new Message("Some went wrong", "danger"));

			e.printStackTrace();
		}

		return "normal/add_contact_form";
	}

	@RequestMapping("/show_contacts/{page}")
	public String showContacts(@PathVariable("page") int page, Model m, Principal principal) {
		m.addAttribute("title", "View Contact List");

		String userName = principal.getName();

		User user = this.userRepo.getUserByUserName(userName);

		Pageable pageable = PageRequest.of(page, 5);

		Page<Contact> contacts = this.contactRepo.findContactsByUser(user.getId(), pageable);

		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());

		return "normal/show_contacts";
	}

	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") int cId, Model model, Principal principal) {

		Optional<Contact> contactOptional = this.contactRepo.findById(cId);

		Contact contact = contactOptional.get();

		String userName = principal.getName();

		User user = this.userRepo.getUserByUserName(userName);

		if (user.getId() == contact.getUser().getId()) {

			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getContactName());

		}

		return "normal/contact_details";

	}

	// Delete Records

	@RequestMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") int cId, HttpSession session) {

		Contact contact = this.contactRepo.findById(cId).get();

		// contact.setUser(null);

		this.contactRepo.delete(contact);

		session.setAttribute("message", new Message("Contact Delete Succesfully..", "success"));

		return "redirect:/user/show_contacts/0";

	}

	// Update Records

	@RequestMapping("/update-contact/{cId}")
	public String updateContact(@PathVariable("cId") int cId, Model m) {

		m.addAttribute("title", "Update Contact");

		Contact contact = this.contactRepo.findById(cId).get();

		m.addAttribute("contact", contact);
		return "normal/update_form";

	}

	@RequestMapping(value = "/process-update", method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Model m, HttpSession session, Principal principal) {

		try {

			// old contact details
			Contact oldContactDetails = this.contactRepo.findById(contact.getcId()).get();

			if (!file.isEmpty()) {

				// delete old image

				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deleteFile, oldContactDetails.getImage());
				file1.delete();

				// update image
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				contact.setImage(file.getOriginalFilename());

			} else {

				contact.setImage(oldContactDetails.getImage());

			}

			User user = this.userRepo.getUserByUserName(principal.getName());

			contact.setUser(user);

			this.contactRepo.save(contact);

			session.setAttribute("message", new Message("your contact is updated...", "success"));

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e);
		}

		return "redirect:/user/" + contact.getcId() + "/contact";
	}

	// Your profile
	@RequestMapping("/profile")
	public String yourProfile(Model model) {

		model.addAttribute("title", "Profile Page");
		return "normal/profile";
	}

	// open setting handler
	@RequestMapping("/settings")
	public String OpenSetting(Model model) {

		model.addAttribute("title", "Setting Page");
		return "normal/settings";
	}

	// Change Password
	@RequestMapping("/change-password")
	public String ChangePassord(@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword, Principal principal,HttpSession session) {

		String UserName = principal.getName();

		User CurrentUser = this.userRepo.getUserByUserName(UserName);
		
		if(this.bryptPasswordEncoder.matches(oldPassword, CurrentUser.getPassword())) {
			
			CurrentUser.setPassword(this.bryptPasswordEncoder.encode(newPassword));
			this.userRepo.save(CurrentUser);	
			session.setAttribute("message", new Message("your password  is successfully changed..", "success"));

			
		}else {
			
			session.setAttribute("message", new Message("Please enter correct old Password !!", "danger"));
			return "redirect:/user/settings";

			
		}
		

		return "redirect:/user/index";
	}
	
	//creating order
	
	@RequestMapping("/create_order")
	@ResponseBody
	public String CreateOrder(@RequestBody Map<String, Object> data) throws RazorpayException {
		
		int amt = Integer.parseInt(data.get("amount").toString());
		

		    RazorpayClient client =new RazorpayClient("rzp_test_UUf8pwOaaJmK7X", "usHWNtINm2bL3PWI3paNsiAI");
		    
		    JSONObject ob = new JSONObject();
		    ob.put("amount", amt*100);
			ob.put("currency", "INR");
			ob.put("receipt", "txt_123456");
			
			Order order = client.Orders.create(ob);
			
			System.out.println(order);
		
		
		return order.toString();
	}

}
