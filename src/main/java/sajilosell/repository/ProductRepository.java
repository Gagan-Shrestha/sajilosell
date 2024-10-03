package sajilosell.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sajilosell.product.Product;
import sajilosell.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find products by category, query, and price range (used for general search functionality with price filtering)
    List<Product> findByCategoryContainingIgnoreCaseAndNameContainingIgnoreCaseAndPriceBetween(
            String category, String query, Integer minPrice, Integer maxPrice);

    // Find products for a specific user, filtered by category, query, and price range, with pagination
    Page<Product> findByUserAndCategoryContainingIgnoreCaseAndNameContainingIgnoreCaseAndPriceBetween(
            User user, String category, String name, Integer minPrice, Integer maxPrice, Pageable pageable);

    // Find products by a specific user
    List<Product> findByUser(User user);

    // Find products excluding a specific user, filtered by category, query, and price range
    Page<Product> findByUserNotAndCategoryContainingIgnoreCaseAndNameContainingIgnoreCaseAndPriceBetween(
            User user, String category, String query, Integer minPrice, Integer maxPrice, Pageable pageable);

    // Optionally, find a product by its ID
    Optional<Product> findById(Long id);
}
