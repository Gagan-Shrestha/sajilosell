package sajilosell.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sajilosell.UserRegistration.UserRegistration;
import sajilosell.service.UserService;

import javax.validation.Valid;


@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute("user")
    public UserRegistration userRegistration() {
        return new UserRegistration();
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String showRegistrationForm() {
        return "registration";
    }

    @PostMapping("/registration")
    public String registerUserAccount(@ModelAttribute("user") @Valid UserRegistration userRegistration,
                                      BindingResult result,
                                      org.springframework.ui.Model model, RedirectAttributes redirAttrs) {

        if (result.hasErrors()) {
            if (result.hasFieldErrors("email")) {
                model.addAttribute("emailError", "Your email format is not complete or invalid.");
                redirAttrs.addFlashAttribute("error", "The error XYZ occurred.");
            }
            return "registration"; // Stay on the same page for error display
        }

        try {
            userService.save(userRegistration);
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("emailExistsError", "Email already exists, please try another.");
            return "registration"; // Stay on the same page for error display
        } catch (Exception e) {
            model.addAttribute("unexpectedError", "An unexpected error occurred. Please try again.");
            return "registration";
        }

        model.addAttribute("successMessage", "Registration successful! Please login.");
        redirAttrs.addFlashAttribute("success", "Everything went just fine.");
        return "registration";
    }

}
