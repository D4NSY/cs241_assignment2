package com.group6.assignment2.controllers.admin;

import com.group6.assignment2.config.Link;
import com.group6.assignment2.config.RedirectionConfig;
import com.group6.assignment2.controllers.EmailController;
import com.group6.assignment2.entity.*;
import com.group6.assignment2.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Controller
public class PostAdminController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private TeacherRepository teacherRepository;
    @Autowired
    private EmailRepository emailRepository;
    @Autowired
    private InviteLinkRepository inviteLinkRepository;
    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    static List<Link> sideNavLinks = new ArrayList<>();

    public PostAdminController() {
        sideNavLinks = Link.addLinks("admin");
    }

    // Handle form submission to invite teachers
    @PostMapping("/admin/invite-user")
    public String inviteTeacher(RedirectAttributes redirectAttributes, @RequestParam("personalEmail") String personalEmail, @RequestParam("password") String password, @RequestParam("fName") String fName, @RequestParam("role") Role role, @RequestParam("lName") String lName, Model model, @AuthenticationPrincipal UserDetails userDetails) {

        String id;
        User user;
        String emailString;
        String passwordString = passwordEncoder.encode(password);

        switch (role){
            case TEACHER:
                id = generateId("T");
                emailString = id + "@teacher.com";
                user = new Teacher(id, emailString, personalEmail, passwordString, fName, lName);
                break;
            case STUDENT:
                id = generateId("S");
                emailString = id + "@student.com";
                user = new Student(id, fName, lName, emailString, personalEmail, passwordString);
                break;
            case ADMIN:
                id = generateId("A");
                emailString = id + "@admin.com";
                user = new Admin(id, fName, lName, emailString, personalEmail, passwordString);
                break;

            default:
                id = generateId("U");
                emailString = id + "@user.com";
                user = new Student(id, fName, lName, emailString, personalEmail, passwordString);
                break;
        }
        userRepository.save(user);

        String inviteLinkCode = createInviteLink(user);
        sendNotification(userDetails, user);
        sendEmail(id, password, personalEmail, inviteLinkCode);

        redirectAttributes.addFlashAttribute("toastMessage", "User successfully invited");
        redirectAttributes.addFlashAttribute("toastType", "success");  // You can send 'success', 'error', etc.

        redirectAttributes.addFlashAttribute("inviteLink", "/invite/" + inviteLinkCode);
        redirectAttributes.addFlashAttribute("successMessage", "The account was created and an Invite was sent");
        return "redirect:/admin/invite-user";
    }

    @PostMapping("/admin/invite-parent")
    public String inviteParent(@RequestParam("personalEmail") String personalEmail, @RequestParam("password") String password, @RequestParam("studentId") String studentId, @RequestParam("fName") String fName, @RequestParam("lName") String lName, Model model, @AuthenticationPrincipal UserDetails userDetails) {

        String id = generateId("P");
        String emailString = id + "@parent.com";
        String passwordString = passwordEncoder.encode(password);

        Parent user = new Parent(id, fName, lName, emailString, personalEmail, passwordString);
        Student student = studentRepository.findByStudentId(studentId);
        user.setStudent(student);
        userRepository.save(user);

        String inviteLinkCode = createInviteLink(user);
        sendNotification(userDetails, user);
        sendEmail(id, password, personalEmail, inviteLinkCode);

        model.addAttribute("inviteLink", "/invite/" + inviteLinkCode);

        return "redirect:/admin/invite-parent";
    }

    private void sendEmail(String username, String unhashedPassword, String personalEmail, String inviteLinkCode) {
        String header = "Hey there! Welcome to our Attendance Tracker";
        String body = "Use this link to verify your email: <a href='localhost:8080/auth/invite" + inviteLinkCode + "'>Invite Link</a> " +
                "<p>Cant see the code? Copy paste this into your browser http://localhost:8080/auth/invite/" + inviteLinkCode + "</p>" +
                "<p>Your credentials are -> Username: " + username + " Password: " + unhashedPassword + "(change after login) </p>";
        String subject = "Invitation Link";

        Email email = new Email(header, body, subject, personalEmail);
        emailRepository.save(email);
        EmailController.SendAutomatedEmail(email);
    }

    private void sendNotification(UserDetails userDetails, User user) {
        String message = "You have been invited to join us on our attendance management system";
        String title = "Welcome!!";
        Notification.NotificationType notificationType = Notification.NotificationType.INFO;
        User sender =  userRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        ;
        Notification notification = new Notification(message, title, notificationType, sender, user);
        notificationRepository.save(notification);
    }

    private String createInviteLink(User user) {
        String inviteLinkCode = UUID.randomUUID().toString().replace("-", "").substring(0, 32);  // Generate a 32-character random string
        InviteLink inviteLink = new InviteLink(inviteLinkCode);
        inviteLink.setUser(user);
        inviteLinkRepository.save(inviteLink);
        return inviteLinkCode;
    }


    private String generateId(String type) {
        Random random = new Random();
        StringBuilder teacherId = new StringBuilder(type);
        for (int i = 0; i < 8; i++) {
            teacherId.append(random.nextInt(9) + 1);  // Ensuring numbers are between 1 and 9
        }

        String tId = teacherId.toString();

        // Check if the ID already exists, if it does, recursively generate a new one
        if (teacherRepository.findByTeacherId(tId) != null) {
            return generateId(type);  // Return the result from the recursive call
        } else {
            return tId;  // Return the generated ID if it's unique
        }
    }

}

