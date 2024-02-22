package com.smart.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smart.entity.Contact;
import com.smart.entity.User;
import com.smart.repo.ContactRepository;
import com.smart.repo.UserRepository;

@RestController
public class SearchController {

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private ContactRepository contactRepo;

	// search handler

	@RequestMapping("/search/{query}")
	public ResponseEntity<?> search(@PathVariable("query") String query, Principal principal) {

		User user = this.userRepo.getUserByUserName(principal.getName());
		
		List<Contact> contacts = this.contactRepo.findByContactNameContainingAndUser(query, user);

		return ResponseEntity.ok(contacts);

	}

}
