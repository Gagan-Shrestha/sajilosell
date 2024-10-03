package sajilosell.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import sajilosell.product.Product;
import sajilosell.user.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<ChatMessage, Long> {

    // Fetch messages by product and receiver, ordered by timestamp
    List<ChatMessage> findByProductAndReceiverOrderByTimestampAsc(Product product, User receiver);

    // Fetch all messages for a product, ordered by timestamp
    List<ChatMessage> findByProductOrderByTimestampAsc(Product product);

    // Fetch all messages where a specific user is the receiver, ordered by timestamp
    List<ChatMessage> findByReceiverOrderByTimestampAsc(User receiver);

    // Fetch messages by sender and receiver, ordered by timestamp
    List<ChatMessage> findBySenderAndReceiverOrderByTimestampAsc(User sender, User receiver);

    // Optional: Fetch a single message by its ID
    Optional<ChatMessage> findById(Long id);

    // Fetch messages where the user is the sender or receiver, ordered by timestamp
    List<ChatMessage> findBySenderOrReceiverOrderByTimestampAsc(User sender, User receiver);

    // Fetch distinct users with whom the logged-in user has exchanged messages
    @Query("SELECT DISTINCT m.sender FROM ChatMessage m WHERE m.receiver = :loggedInUser " +
            "UNION SELECT DISTINCT m.receiver FROM ChatMessage m WHERE m.sender = :loggedInUser")
    List<User> findDistinctUsersWithMessages(User loggedInUser);

    // Fetch messages between two specific users, ordered by timestamp
    @Query("SELECT m FROM ChatMessage m WHERE (m.sender = :loggedInUser AND m.receiver = :sender) " +
            "OR (m.sender = :sender AND m.receiver = :loggedInUser) ORDER BY m.timestamp ASC")
    List<ChatMessage> findMessagesBetweenUsers(User loggedInUser, User sender);
}
