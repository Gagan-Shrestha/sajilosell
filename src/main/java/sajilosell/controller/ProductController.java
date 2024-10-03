package sajilosell.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sajilosell.chat.ChatMessage;
import sajilosell.chat.ChatService;
import sajilosell.product.Product;
import sajilosell.product.ProductService;
import sajilosell.user.User;
import sajilosell.service.UserService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private ChatService chatService;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ProductController.class);


    // Display product dashboard for logged-in user with filters including price range and pagination
    @GetMapping({"", "/"})

    public String dashboardProduct(
            @RequestParam(value = "category", defaultValue = "") String category,
            @RequestParam(value = "query", defaultValue = "") String query,
            @RequestParam(value = "minPrice", defaultValue = "0") Integer minPrice,
            @RequestParam(value = "maxPrice", defaultValue = "1000000") Integer maxPrice,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "6") int size,
            Authentication authentication,
            Model model) {

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User loggedInUser = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        Page<Product> productPage = productService.findProductsByOtherUsersWithPriceRange(
                loggedInUser, category, query, minPrice, maxPrice, pageable);

        model.addAttribute("user", userDetails);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("query", query);
        model.addAttribute("category", category);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("size", size);

        return "products/dashboard";  // This maps to src/main/resources/templates/products/dashboard.html
    }


    // Display products for logged-in user with filters including price range and pagination
    @GetMapping("/index")
    public String getProducts(
            @RequestParam(value = "category", defaultValue = "") String category,
            @RequestParam(value = "query", defaultValue = "") String query,
            @RequestParam(value = "minPrice", defaultValue = "0") Integer minPrice,
            @RequestParam(value = "maxPrice", defaultValue = "1000000") Integer maxPrice,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "6") int size,
            Authentication authentication,
            Model model) {

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create a pageable object for pagination
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        // Fetch paginated products for the logged-in user with filters
        Page<Product> productPage = productService.findProductsByUserAndFiltersWithPriceRange(
                user, category, query, minPrice, maxPrice, pageable);

        // Add attributes to the model to render on the view
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("query", query);
        model.addAttribute("category", category);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("size", size);

        return "products/index";
    }
    // Show create form for new product
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new Product());
        return "products/create";
    }

    // Save new product
    @PostMapping("/save")
    public String saveProduct(
            @ModelAttribute Product product,
            @RequestParam("imageFile") MultipartFile imageFile,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            if (!imageFile.isEmpty()) {
                product.setImage(imageFile.getBytes());
            }
            productService.saveProduct(product, user);
            redirectAttributes.addFlashAttribute("message", "Product saved successfully!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("message", "Failed to save the product image.");
        }

        return "redirect:/products/index";
    }

    // Show edit form for existing product
    @GetMapping("/edit/{id}")
    public String showUpdateForm(
            @PathVariable("id") Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        Optional<Product> product = productService.getProductById(id);
        if (product.isPresent()) {
            model.addAttribute("product", product.get());
            return "products/update";
        } else {
            redirectAttributes.addFlashAttribute("message", "Product not found.");
            return "redirect:/products/index";
        }
    }

    // Update existing product
    @PostMapping("/update/{id}")
    public String updateProduct(
            @PathVariable("id") Long id,
            @ModelAttribute Product product,
            @RequestParam("imageFile") MultipartFile imageFile,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            if (!imageFile.isEmpty()) {
                product.setImage(imageFile.getBytes());
            }
            productService.updateProduct(id, product, user);
            redirectAttributes.addFlashAttribute("message", "Product updated successfully!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("message", "Failed to update the product image.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }

        return "redirect:/products/index";
    }

    // Delete product
    @GetMapping("/delete/{id}")
    public String deleteProduct(
            @PathVariable("id") Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            productService.deleteProduct(id, user);
            redirectAttributes.addFlashAttribute("message", "Product deleted successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }

        return "redirect:/products/index";
    }

    // Get product image
    @GetMapping("/image/{id}")
    @ResponseBody
    public byte[] getProductImage(@PathVariable("id") Long id) {
        Optional<Product> product = productService.getProductById(id);
        return product.map(Product::getImage).orElse(null);
    }

    // Show chat page for a specific product
    @GetMapping("/chat/{id}")
    public String showChatPage(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Optional<Product> productOptional = productService.getProductById(id);
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            User productOwner = product.getUser();

            if (productOwner == null) {
                model.addAttribute("error", "Product owner not found.");
                return "error/404";
            }

            model.addAttribute("product", product);
            model.addAttribute("productOwnerEmail", productOwner.getEmail());

            List<ChatMessage> chatMessages = chatService.getMessagesByProduct(product);
            model.addAttribute("chatMessages", chatMessages);

            return "products/chat";
        } else {
            return "error/404"; // Product not found
        }
    }

    @PostMapping("/chat/send/{id}")
    public String sendMessage(
            @PathVariable Long id,
            @RequestParam String message,
            @RequestParam(required = false) Long receiverId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        Optional<Product> productOptional = productService.getProductById(id);
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            User sender = userService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            User receiver = receiverId != null ? userService.findById(receiverId)
                    .orElseThrow(() -> new RuntimeException("Receiver not found"))
                    : product.getUser(); // Set receiver as the product owner if not specified

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setProduct(product);
            chatMessage.setSender(sender);
            chatMessage.setReceiver(receiver);
            chatMessage.setMessage(message);
            chatMessage.setTimestamp(LocalDateTime.now());
            chatService.saveChatMessage(chatMessage);

            // Add success message to be displayed on the /message page
            redirectAttributes.addFlashAttribute("message", "Message sent successfully!");
        } else {
            redirectAttributes.addFlashAttribute("message", "Product not found.");
        }

        // Redirect to the /message page after successfully sending the chat message
        return "redirect:/products/message";
    }

    /**
     * Displays chat messages for the logged-in user.
     *
     * @param model       The model to add attributes to.
     * @param userDetails The logged-in user details.
     * @return The view name for displaying messages.
     */

    @GetMapping("/message")
    public String viewMessages(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User loggedInUser = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get all distinct users the logged-in user has had conversations with
        List<User> messageUsers = chatService.getAllUsersWithMessages(loggedInUser);

        model.addAttribute("messageUsers", messageUsers);
        model.addAttribute("user", loggedInUser);
        return "products/message";
    }

    @GetMapping("/message/{senderId}")
    public String viewMessagesBySender(@PathVariable Long senderId, Model model,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        User loggedInUser = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        User sender = userService.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        // Fetch the conversation between logged-in user and the selected sender
        List<ChatMessage> messages = chatService.getMessagesBetweenUsers(loggedInUser, sender);

        model.addAttribute("messages", messages);
        model.addAttribute("user", loggedInUser);
        model.addAttribute("sender", sender);
        return "products/message_detail";
    }

    @PostMapping("/message/reply")
    public String sendReply(
            @RequestParam Long senderId,
            @RequestParam String replyMessage,
            @RequestParam Long productId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User loggedInUser = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        User sender = userService.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        Optional<Product> productOptional = productService.getProductById(productId);
        if (productOptional.isPresent()) {
            Product product = productOptional.get();

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setProduct(product);
            chatMessage.setSender(loggedInUser);
            chatMessage.setReceiver(sender);
            chatMessage.setMessage(replyMessage);
            chatMessage.setTimestamp(LocalDateTime.now());
            chatService.saveChatMessage(chatMessage);

            redirectAttributes.addFlashAttribute("successMessage", "Reply sent successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Product not found.");
        }

        // Redirect to the message detail page with senderId
        return "redirect:/products/message/" + senderId;
    }


    @PostMapping("/message/delete/{messageId}")
    public String deleteMessage(
            @PathVariable Long messageId,
            @RequestParam Long senderId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        // Check if the message exists
        Optional<ChatMessage> messageOptional = chatService.getMessageById(messageId);
        if (messageOptional.isPresent()) {
            ChatMessage chatMessage = messageOptional.get();

            // Check if the logged-in user is either the sender or the receiver
            User loggedInUser = userService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (chatMessage.getSender().equals(loggedInUser) || chatMessage.getReceiver().equals(loggedInUser)) {
                chatService.deleteChatMessage(chatMessage);
                redirectAttributes.addFlashAttribute("successMessage", "Message deleted successfully.");

                // Check if any messages remain from the sender
                List<ChatMessage> remainingMessagesFromSender = chatService.getMessagesByProductAndReceiver(chatMessage.getProduct(), chatMessage.getSender());
                List<ChatMessage> remainingMessagesFromReceiver = chatService.getMessagesByProductAndReceiver(chatMessage.getProduct(), chatMessage.getReceiver());

                // If there are no remaining messages for both sender and receiver
                if (remainingMessagesFromSender.isEmpty() && remainingMessagesFromReceiver.isEmpty()) {
                    // No remaining messages, redirect to the product dashboard
                    return "redirect:/products";
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "You are not authorized to delete this message.");
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Message not found.");
        }

        // If there are remaining messages, stay on the message detail page
        return "redirect:/products/message/" + senderId;
    }

}
