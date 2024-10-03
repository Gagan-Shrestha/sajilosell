package sajilosell.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sajilosell.repository.ProductRepository;
import sajilosell.user.User;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // Retrieve products for a specific user
    public List<Product> findProductsByUser(User user) {
        return productRepository.findByUser(user);
    }

    // Retrieve a product by ID
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    // Retrieve products based on category, query, and price range, excluding the logged-in user's products (with pagination)
    public Page<Product> findProductsByOtherUsersWithPriceRange(User user, String category, String query, Integer minPrice, Integer maxPrice, Pageable pageable) {
        return productRepository.findByUserNotAndCategoryContainingIgnoreCaseAndNameContainingIgnoreCaseAndPriceBetween(
                user, category, query, minPrice, maxPrice, pageable);
    }

    // Retrieve products based on user, category, query, and price range (with pagination)
    public Page<Product> findProductsByUserAndFiltersWithPriceRange(User user, String category, String query, Integer minPrice, Integer maxPrice, Pageable pageable) {
        return productRepository.findByUserAndCategoryContainingIgnoreCaseAndNameContainingIgnoreCaseAndPriceBetween(
                user, category, query, minPrice, maxPrice, pageable);
    }

    // Retrieve products based on category, query, and price range (without pagination)
    public List<Product> findProductsByCategoryAndFiltersWithPriceRange(String category, String query, Integer minPrice, Integer maxPrice) {
        return productRepository.findByCategoryContainingIgnoreCaseAndNameContainingIgnoreCaseAndPriceBetween(
                category, query, minPrice, maxPrice);
    }

    // Save a product with an associated user
    @Transactional
    public void saveProduct(Product product, User user) {
        product.setUser(user);
        productRepository.save(product);
    }

    // Update an existing product
    @Transactional
    public void updateProduct(Long id, Product updatedProduct, User user) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // Ensure the logged-in user is authorized to update the product
        if (!existingProduct.getUser().equals(user)) {
            throw new SecurityException("Unauthorized update attempt");
        }

        // Update product details
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setBrand(updatedProduct.getBrand());
        existingProduct.setCategory(updatedProduct.getCategory());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setDescription(updatedProduct.getDescription());

        // Only update the image if a new one is provided
        if (updatedProduct.getImage() != null) {
            existingProduct.setImage(updatedProduct.getImage());
        }

        existingProduct.setContact(updatedProduct.getContact());
        existingProduct.setAddress(updatedProduct.getAddress());

        productRepository.save(existingProduct);
    }

    // Delete a product
    @Transactional
    public void deleteProduct(Long id, User user) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // Ensure the logged-in user is authorized to delete the product
        if (!existingProduct.getUser().equals(user)) {
            throw new SecurityException("Unauthorized deletion attempt");
        }

        productRepository.deleteById(id);
    }
}
