package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRespository;
	
	//Method for adding common data to response
	
	@ModelAttribute
	public void addCommonData(Model model,Principal principal)
	{
		String userName = principal.getName();
		System.out.println("USERNAME "+userName);
		
		//Get the username (Email)
		
		User user = userRepository.getUserByUserName(userName);
		System.out.println("USER "+user);
		
		
		model.addAttribute("user",user);
	}
	
	
	// Dashboard Home
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal)
	{
		model.addAttribute("title","User Dashboard");
		return "normal/user_dashboard";
	}
	
	
	//open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model)
	{
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());
		
		return "normal/add_contact_form";
	}
	
	//Processing at contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file,
			Principal principal,HttpSession session)
	{
		try {
		String name = principal.getName();
		User users = this.userRepository.getUserByUserName(name);
		
		
		//Processing and uploading file
		
		if(file.isEmpty())
		{
			
			//if file is empty then try our message
			System.out.println("File is Empty");
			contact.setImage("contact.png");
		}
		else {
			//update name to contact and file to folder
			contact.setImage(file.getOriginalFilename());
			
			File saveFile = new ClassPathResource("static/img").getFile();
			
			Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			System.out.println("Image is Uploaded");
		}
		
		
		
		contact.setUser(users);
		
		users.getContacts().add(contact);
		
		this.userRepository.save(users);

		System.out.println("DATA "+contact);
		
		System.out.println("Added to database");
		
		// message success
		
		session.setAttribute("message", new Message("Your contact is added !! Add more...","success") );
		
		}
		catch(Exception e)
		{
			
			
			System.out.println("ERROR "+e.getMessage());
			e.printStackTrace();
			
			//message error
			session.setAttribute("message", new Message("Something went wrong !! Try again..","danger") );
		}
		
		
		
		return "normal/add_contact_form";
	}
	
	//Show contact handler 
	// per page 5
	
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page,Model m,Principal principal)
	{
		m.addAttribute("title","Show User Contacts");
	
//		// send contacts list
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
//		List<Contact> contacts = user.getContacts();
		
		//Show contact handler 
		// per page 5
		
		Pageable pageable = PageRequest.of(page,5);
		
		Page<Contact> contacts = this.contactRespository.findContactsByUser(user.getId(),pageable);
		
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage",page);
		m.addAttribute("totalPages",contacts.getTotalPages());
		
		
		
		return "normal/show_contacts";
	}
	
	
	//Showing specific contact details
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId,Model model,Principal principal)
	{
		System.out.println("CID "+cId);
		
		Optional<Contact> contactOptional = this.contactRespository.findById(cId);
		Contact contact = contactOptional.get();
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		
		if(user.getId()==contact.getUser().getId())
		{
			model.addAttribute("contact",contact);
			model.addAttribute("title",contact.getName());
		}
		
		
		return "normal/contact_detail";
	}
	
	
	//Delete contact handler
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId , Model mode
				,HttpSession session,Principal principal)
	{
		Optional<Contact> contactOptional = this.contactRespository.findById(cId);
		Contact contact = contactOptional.get();
		
		//check.....
		
//		String userName = principal.getName();
//		User user = this.userRepository.getUserByUserName(userName);
		
		
//		if(user.getId()==contact.getUser().getId())
//		{
//				this.contactRespository.delete(contact);
//			
//		}
//	
		//Check Assignment
		
		
		System.out.println("Contact "+contact.getcId());
		//contact.setUser(null);
		

		User user = this.userRepository.getUserByUserName(principal.getName());
		user.getContacts().remove(contact);
		this.userRepository.save(user);
		
		
		System.out.println("Deleted");
		session.setAttribute("message", new Message("Contact Deleted Succesfully.....","success"));
		
		return "redirect:/user/show-contacts/0";
	}
	
	
	//Open Update Form Handler
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid,Model m)
	{
		m.addAttribute("title","Update Contact");
		
		Contact contact = this.contactRespository.findById(cid).get();
		m.addAttribute("contact",contact);
		return "normal/update_form";
	}
	
	
	//Update contact handler
	@RequestMapping(value = "/process-update",method=RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file
			,Model m,HttpSession session,Principal principal)
	{
		try{
			
			//old contact details
			
			Contact oldcontactDetail = this.contactRespository.findById(contact.getcId()).get();
			
			//image...
			
			if(!file.isEmpty())
			{
				
				//delete file
				
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1=new File(deleteFile,oldcontactDetail.getImage());
				file1.delete();
				
				
				
				//rewrite file
				
				File saveFile = new ClassPathResource("static/img").getFile();
				
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			
			}
			else {
				contact.setImage(oldcontactDetail.getImage());
			}
			
			
			User user=this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRespository.save(contact);
			session.setAttribute("message", new Message("Your contact is updated " , "success" ));
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		System.out.println("CONTACT NAME"+contact.getName());
		System.out.println("CONTACT ID"+contact.getcId());
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	
	//Your Profile Handler
	@GetMapping("/profile")
	public String yourProfile(Model model)
	{
		model.addAttribute("title","Profile Page");
		return "normal/profile";
	}
	
	
	//Open Setting Handler
	@GetMapping("/settings")
	public String openSettings()
	{
	return "normal/settings";	
	}
	
	
	//change password - handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword
			,@RequestParam("newPassword") String newPassword,Principal principal,HttpSession session)
	{
		System.out.println("OLD PASSWORD " +oldPassword );
		System.out.println("NEW PASSWORD " +newPassword );
		
		String userName = principal.getName();
		User currentUser = this.userRepository.getUserByUserName(userName);
		
		System.out.println(currentUser.getPassword());
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword()))
		{
			//change the password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message", new Message("Your password is successfully changed  !!","success"));
			
			
		}else {
			//error
			session.setAttribute("message", new Message("Please enter correct old password  !!","danger"));
			return "redirect:/user/settings";
		}
		
		return "redirect:/user/index";
	}
	
	
}
