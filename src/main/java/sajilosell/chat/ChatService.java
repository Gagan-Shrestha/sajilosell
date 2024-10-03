package sajilosell.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sajilosell.product.Product;
import sajilosell.user.User;

import java.util.List;
import java.util.Optional;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    @Autowired
    private ChatRepository chatRepository;

    // Fetch messages by product and receiver, ordered by timestamp in ascending order
    public List<ChatMessage> getMessagesByProductAndReceiver(Product product, User receiver) {
        logger.debug("Fetching messages for product: {} and receiver: {}", product.getId(), receiver.getId());
        return chatRepository.findByProductAndReceiverOrderByTimestampAsc(product, receiver);
    }

    // Fetch a chat message by its ID
    public Optional<ChatMessage> getMessageById(Long id) {
        logger.debug("Fetching message with ID: {}", id);
        return chatRepository.findById(id);
    }

    // Fetch messages by product, ordered by timestamp in ascending order
    public List<ChatMessage> getMessagesByProduct(Product product) {
        logger.debug("Fetching messages for product: {}", product.getId());
        return chatRepository.findByProductOrderByTimestampAsc(product);
    }

    // Fetch messages where the logged-in user is the receiver, ordered by timestamp in ascending order
    public List<ChatMessage> getMessagesByReceiver(User receiver) {
        logger.debug("Fetching messages for receiver: {}", receiver.getId());
        return chatRepository.findByReceiverOrderByTimestampAsc(receiver);
    }

    // Fetch messages where the user is either the sender or receiver, ordered by timestamp in ascending order
    public List<ChatMessage> getMessagesByUser(User user) {
        logger.debug("Fetching messages for user: {}", user.getId());
        return chatRepository.findBySenderOrReceiverOrderByTimestampAsc(user, user);
    }

    // Fetch all distinct users who have exchanged messages with the logged-in user
    public List<User> getAllUsersWithMessages(User loggedInUser) {
        logger.debug("Fetching all users with messages for logged-in user: {}", loggedInUser.getId());
        return chatRepository.findDistinctUsersWithMessages(loggedInUser);
    }

    // Fetch messages between two users, ordered by timestamp in ascending order
    public List<ChatMessage> getMessagesBetweenUsers(User loggedInUser, User sender) {
        logger.debug("Fetching messages between user: {} and sender: {}", loggedInUser.getId(), sender.getId());
        return chatRepository.findMessagesBetweenUsers(loggedInUser, sender);
    }

    // Save a chat message to the repository
    public void saveChatMessage(ChatMessage chatMessage) {
        try {
            logger.debug("Saving chat message: {}", chatMessage);
            chatRepository.save(chatMessage);
        } catch (Exception e) {
            logger.error("Error saving chat message: {}", chatMessage, e);
            throw e; // Rethrow or handle exception based on your error handling strategy
        }
    }

    // Delete a chat message by ID
    public void deleteChatMessage(Long messageId) {
        try {
            logger.debug("Deleting chat message with ID: {}", messageId);
            chatRepository.deleteById(messageId);
        } catch (Exception e) {
            logger.error("Error deleting chat message with ID: {}", messageId, e);
            throw e; // Rethrow or handle exception based on your error handling strategy
        }
    }

    // Overloaded method to delete by ChatMessage object
    public void deleteChatMessage(ChatMessage chatMessage) {
        deleteChatMessage(chatMessage.getId());
    }
}
