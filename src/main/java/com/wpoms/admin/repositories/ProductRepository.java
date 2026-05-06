package com.wpoms.admin.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wpoms.admin.models.entities.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

        // Find products by manufacturer ID
        List<Product> findByManufacturerId(int manufacturerId);

        // Check if product exists for a manufacturer
        boolean existsByProductNameAndManufacturerId(String productName, int manufacturerId);

        // Find product by ID and manufacturer ID
        Optional<Product> findByProductIdAndManufacturerId(int productId, int manufacturerId);

        // Search + Filter + Active products (Vendor view)
        // @Query("SELECT p FROM Product p JOIN Manufacturer m ON p.manufacturerId =
        // m.manufacturerId WHERE " +
        // "(:search IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :search,
        // '%')) " +
        // " OR LOWER(m.manufacturerName) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
        // "(:category IS NULL OR p.category = :category) AND " +
        // "(:warranty IS NULL OR p.warrantyType = :warranty) AND " +
        // "(:manufacturerId IS NULL OR p.manufacturerId = :manufacturerId) AND " +
        // "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
        // "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
        // "p.isActive = true")

        // List<Product> searchActiveProducts(
        // @Param ("search") String search,
        // @Param("category") String category,
        // @Param("warranty") String warranty,
        // @Param("manufacturerId") Integer manufacturerId,
        // @Param("minPrice") Double minPrice,
        // @Param("maxPrice") Double maxPrice
        // );

        @Query("SELECT p FROM Product p JOIN p.manufacturer m WHERE " +
        "(:search IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :search, '%')) " +
        "   OR LOWER(m.companyName) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
        "(:category IS NULL OR p.category = :category) AND " +
        "(:warranty IS NULL OR p.warrantyType = :warranty) AND " +
        "(:manufacturerId IS NULL OR m.manufacturerId = :manufacturerId) AND " +
        "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
        "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
        "p.isActive = true")
List<Product> searchActiveProducts(
        @Param("search") String search,
        @Param("category") String category,
        @Param("warranty") String warranty,
        @Param("manufacturerId") Integer manufacturerId,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice
);

        @Query("SELECT p FROM Product p WHERE " +
                        "p.manufacturerId = :manufacturerId AND " +
                        "(:isActive IS NULL OR p.isActive = :isActive)")
        List<Product> findByManufacturerWithStatus(
                        @Param("manufacturerId") Integer manufacturerId,
                        @Param("isActive") Boolean isActive);

}