package com.example.cosmocats.service;

import com.example.cosmocats.domain.Product;
import com.example.cosmocats.dto.product.ProductDto;
import com.example.cosmocats.dto.product.ProductListDto;
import com.example.cosmocats.dto.product.ProductUpdateDto;
import com.example.cosmocats.exception.ProductAlreadyExistsException;
import com.example.cosmocats.exception.ProductNotFoundException;
import com.example.cosmocats.repository.ProductRepository;
import com.example.cosmocats.service.mapper.ProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Product Service Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private UUID productId;
    private Product product;
    private ProductDto productDto;
    private ProductUpdateDto productUpdateDto;

    @BeforeEach
    void setUp() {
        productId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        
        product = Product.builder()
                .productId(productId)
                .category("Electronics")
                .name("Quantum Phone X1")
                .description("Advanced smartphone with quantum processor")
                .price(999.99f)
                .build();

        productDto = ProductDto.builder()
                .productId(productId)
                .category("Electronics")
                .name("Quantum Phone X1")
                .description("Advanced smartphone with quantum processor")
                .price(999.99f)
                .build();

        productUpdateDto = ProductUpdateDto.builder()
                .category("Electronics")
                .name("Quantum Phone X1")
                .description("Advanced smartphone with quantum processor")
                .price(999.99f)
                .build();
    }

    @Test
    @DisplayName("Should create product when product does not exist")
    void createProduct_ShouldReturnProductDto_WhenProductDoesNotExist() {
        // Arrange
        when(productRepository.existsByName(anyString())).thenReturn(false);
        when(productMapper.toProduct(any(ProductUpdateDto.class))).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toProductDto(any(Product.class))).thenReturn(productDto);

        // Act
        ProductDto result = productService.createProduct(productUpdateDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(result.getName()).isEqualTo("Quantum Phone X1");
        assertThat(result.getCategory()).isEqualTo("Electronics");
        assertThat(result.getDescription()).isEqualTo("Advanced smartphone with quantum processor");
        assertThat(result.getPrice()).isEqualTo(999.99f);
        
        verify(productRepository).existsByName("Quantum Phone X1");
        verify(productRepository).save(product);
        verify(productMapper).toProductDto(product);
    }

    @Test
    @DisplayName("Should throw exception when product already exists")
    void createProduct_ShouldThrowException_WhenProductAlreadyExists() {
        // Arrange
        when(productRepository.existsByName(anyString())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> productService.createProduct(productUpdateDto))
                .isInstanceOf(ProductAlreadyExistsException.class)
                .hasMessageContaining("Product already exists with name: Quantum Phone X1");
        
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should return product by ID when product exists")
    void getProductById_ShouldReturnProductDto_WhenProductExists() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productMapper.toProductDto(any(Product.class))).thenReturn(productDto);

        // Act
        ProductDto result = productService.getProductById(productId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(result.getName()).isEqualTo("Quantum Phone X1");
        assertThat(result.getCategory()).isEqualTo("Electronics");
        assertThat(result.getDescription()).isEqualTo("Advanced smartphone with quantum processor");
        assertThat(result.getPrice()).isEqualTo(999.99f);
        
        verify(productRepository).findById(productId);
        verify(productMapper).toProductDto(product);
    }

    @Test
    @DisplayName("Should throw exception when product not found by ID")
    void getProductById_ShouldThrowException_WhenProductNotFound() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.getProductById(productId))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found with id: " + productId);
        
        verify(productRepository).findById(productId);
        verify(productMapper, never()).toProductDto(any(Product.class));
    }

    @Test
    @DisplayName("Should return all products when products exist")
    void getAllProducts_ShouldReturnProductListDto_WhenProductsExist() {
        // Arrange
        List<Product> products = Arrays.asList(product);
        ProductListDto productListDto = ProductListDto.builder()
                .products(Arrays.asList(productDto))
                .build();
        
        when(productRepository.findAll()).thenReturn(products);
        when(productMapper.toProductListDto(products)).thenReturn(productListDto);

        // Act
        ProductListDto result = productService.getAllProducts();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProducts()).hasSize(1);
        assertThat(result.getProducts().get(0).getProductId()).isEqualTo(productId);
        assertThat(result.getProducts().get(0).getName()).isEqualTo("Quantum Phone X1");
        assertThat(result.getProducts().get(0).getCategory()).isEqualTo("Electronics");
        
        verify(productRepository).findAll();
        verify(productMapper).toProductListDto(products);
    }

    @Test
    @DisplayName("Should return empty list when no products exist")
    void getAllProducts_ShouldReturnEmptyList_WhenNoProductsExist() {
        // Arrange
        List<Product> emptyProducts = List.of();
        ProductListDto emptyProductListDto = ProductListDto.builder()
                .products(List.of())
                .build();
        
        when(productRepository.findAll()).thenReturn(emptyProducts);
        when(productMapper.toProductListDto(emptyProducts)).thenReturn(emptyProductListDto);

        // Act
        ProductListDto result = productService.getAllProducts();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProducts()).isEmpty();
        
        verify(productRepository).findAll();
        verify(productMapper).toProductListDto(emptyProducts);
    }

    @Test
    @DisplayName("Should update product when product exists and name is unique")
    void updateProduct_ShouldReturnUpdatedProductDto_WhenProductExistsAndNameIsUnique() {
        // Arrange
        Product updatedProduct = product.toBuilder()
                .name("Updated Quantum Phone")
                .description("Updated description with cosmic features")
                .price(899.99f)
                .build();
        
        ProductDto updatedProductDto = productDto.toBuilder()
                .name("Updated Quantum Phone")
                .description("Updated description with cosmic features")
                .price(899.99f)
                .build();
        
        ProductUpdateDto updateDto = ProductUpdateDto.builder()
                .category("Electronics")
                .name("Updated Quantum Phone")
                .description("Updated description with cosmic features")
                .price(899.99f)
                .build();

        when(productRepository.existsById(productId)).thenReturn(true);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.existsByName(anyString())).thenReturn(false);
        when(productMapper.toProductWithId(eq(productId), any(ProductUpdateDto.class))).thenReturn(updatedProduct);
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);
        when(productMapper.toProductDto(updatedProduct)).thenReturn(updatedProductDto);

        // Act
        ProductDto result = productService.updateProduct(productId, updateDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Quantum Phone");
        assertThat(result.getDescription()).isEqualTo("Updated description with cosmic features");
        assertThat(result.getPrice()).isEqualTo(899.99f);
        assertThat(result.getCategory()).isEqualTo("Electronics");
        
        verify(productRepository).existsById(productId);
        verify(productRepository).findById(productId);
        verify(productRepository).existsByName("Updated Quantum Phone");
        verify(productRepository).save(updatedProduct);
        verify(productMapper).toProductDto(updatedProduct);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent product")
    void updateProduct_ShouldThrowException_WhenProductNotFound() {
        // Arrange
        when(productRepository.existsById(productId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> productService.updateProduct(productId, productUpdateDto))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found with id: " + productId);
        
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw exception when product name already exists during update")
    void updateProduct_ShouldThrowException_WhenProductNameAlreadyExists() {
        // Arrange
        when(productRepository.existsById(productId)).thenReturn(true);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.existsByName(anyString())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> productService.updateProduct(productId, productUpdateDto))
                .isInstanceOf(ProductAlreadyExistsException.class)
                .hasMessageContaining("Product already exists with name: Quantum Phone X1");
        
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should delete product when product exists")
    void deleteProduct_ShouldDeleteProduct_WhenProductExists() {
        // Arrange
        when(productRepository.existsById(productId)).thenReturn(true);
        doNothing().when(productRepository).deleteById(productId);

        // Act
        productService.deleteProduct(productId);

        // Assert
        verify(productRepository).existsById(productId);
        verify(productRepository).deleteById(productId);
    }

    @Test
    @DisplayName("Should handle deleting non-existent product gracefully")
    void deleteProduct_ShouldNotThrowException_WhenProductDoesNotExist() {
        // Arrange
        when(productRepository.existsById(productId)).thenReturn(false);

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> productService.deleteProduct(productId));

        // Assert
        verify(productRepository).existsById(productId);
        verify(productRepository, never()).deleteById(productId);
    }

    @Test
    @DisplayName("Should return filtered products by category")
    void getProductsByCategory_ShouldReturnFilteredProducts_WhenCategoryExists() {
        // Arrange
        Product product2 = Product.builder()
                .productId(UUID.fromString("550e8400-e29b-41d4-a716-446655440002"))
                .category("Books")
                .name("Space Book")
                .description("Book about space exploration")
                .price(29.99f)
                .build();
        
        List<Product> allProducts = Arrays.asList(product, product2);
        List<Product> electronicsProducts = List.of(product);
        
        ProductListDto electronicsProductListDto = ProductListDto.builder()
                .products(List.of(productDto))
                .build();

        when(productRepository.findAll()).thenReturn(allProducts);
        when(productMapper.toProductListDto(electronicsProducts)).thenReturn(electronicsProductListDto);

        // Act
        ProductListDto result = productService.getProductsByCategory("Electronics");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProducts()).hasSize(1);
        assertThat(result.getProducts().get(0).getCategory()).isEqualTo("Electronics");
        assertThat(result.getProducts().get(0).getName()).isEqualTo("Quantum Phone X1");
        assertThat(result.getProducts().get(0).getPrice()).isEqualTo(999.99f);
        
        verify(productRepository).findAll();
        verify(productMapper).toProductListDto(electronicsProducts);
    }

    @Test
    @DisplayName("Should return empty list when no products in category")
    void getProductsByCategory_ShouldReturnEmptyList_WhenNoProductsInCategory() {
        // Arrange
        List<Product> allProducts = Arrays.asList(product);
        List<Product> emptyProducts = List.of();
        
        ProductListDto emptyProductListDto = ProductListDto.builder()
                .products(List.of())
                .build();

        when(productRepository.findAll()).thenReturn(allProducts);
        when(productMapper.toProductListDto(emptyProducts)).thenReturn(emptyProductListDto);

        // Act
        ProductListDto result = productService.getProductsByCategory("NonExistentCategory");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProducts()).isEmpty();
        
        verify(productRepository).findAll();
        verify(productMapper).toProductListDto(emptyProducts);
    }

    @Test
    @DisplayName("Should handle case insensitive category filtering")
    void getProductsByCategory_ShouldBeCaseInsensitive() {
        // Arrange
        List<Product> allProducts = Arrays.asList(product);
        List<Product> electronicsProducts = List.of(product);
        
        ProductListDto electronicsProductListDto = ProductListDto.builder()
                .products(List.of(productDto))
                .build();

        when(productRepository.findAll()).thenReturn(allProducts);
        when(productMapper.toProductListDto(electronicsProducts)).thenReturn(electronicsProductListDto);

        // Act
        ProductListDto result = productService.getProductsByCategory("electronics");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProducts()).hasSize(1);
        assertThat(result.getProducts().get(0).getCategory()).isEqualTo("Electronics");
        assertThat(result.getProducts().get(0).getName()).isEqualTo("Quantum Phone X1");
        
        verify(productRepository).findAll();
        verify(productMapper).toProductListDto(electronicsProducts);
    }
}