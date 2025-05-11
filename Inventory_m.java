// Project: Simple Inventory Management System
// This project demonstrates OOP concepts, data structures, file I/O, and exception handling

import java.io.*;
import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// Product class representing inventory items
class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String name;
    private double price;
    private int quantity;
    private LocalDate expiryDate;
    
    public Product(String id, String name, double price, int quantity, LocalDate expiryDate) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
    }
    
    // Getters and setters
    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public LocalDate getExpiryDate() { return expiryDate; }
    
    public void setPrice(double price) { this.price = price; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    // Check if product is expired
    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }
    
    // Days until expiration
    public long daysUntilExpiry() {
        return expiryDate.toEpochDay() - LocalDate.now().toEpochDay();
    }
    
    // Calculate total value of this product in inventory
    public double getTotalValue() {
        return price * quantity;
    }
    
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return String.format("%-5s | %-20s | $%-8.2f | %-5d | %-10s | %s", 
            id, name, price, quantity, 
            formatter.format(expiryDate),
            isExpired() ? "EXPIRED" : daysUntilExpiry() + " days left");
    }
}

// Custom exception for inventory operations
class InventoryException extends Exception {
    private static final long serialVersionUID = 1L;
    
    public InventoryException(String message) {
        super(message);
    }
}

// InventoryManager handles all operations on the inventory
class InventoryManager {
    private Map<String, Product> inventory;
    private final String INVENTORY_FILE = "inventory.dat";
    
    public InventoryManager() {
        inventory = new HashMap<>();
        loadInventory();
    }
    
    // Add a new product to inventory
    public void addProduct(String id, String name, double price, int quantity, LocalDate expiryDate) 
            throws InventoryException {
        if (inventory.containsKey(id)) {
            throw new InventoryException("Product with ID " + id + " already exists.");
        }
        
        if (price <= 0) {
            throw new InventoryException("Price must be greater than zero.");
        }
        
        if (quantity < 0) {
            throw new InventoryException("Quantity cannot be negative.");
        }
        
        if (expiryDate.isBefore(LocalDate.now())) {
            throw new InventoryException("Cannot add expired product.");
        }
        
        Product product = new Product(id, name, price, quantity, expiryDate);
        inventory.put(id, product);
        System.out.println("Product added successfully: " + product.getName());
        saveInventory();
    }
    
    // Update product quantity (add or remove)
    public void updateQuantity(String id, int change) throws InventoryException {
        Product product = getProductById(id);
        
        int newQuantity = product.getQuantity() + change;
        if (newQuantity < 0) {
            throw new InventoryException("Not enough inventory. Current quantity: " + product.getQuantity());
        }
        
        product.setQuantity(newQuantity);
        System.out.println("Updated quantity for " + product.getName() + ": " + product.getQuantity());
        saveInventory();
    }
    
    // Update product price
    public void updatePrice(String id, double newPrice) throws InventoryException {
        if (newPrice <= 0) {
            throw new InventoryException("Price must be greater than zero.");
        }
        
        Product product = getProductById(id);
        product.setPrice(newPrice);
        System.out.println("Updated price for " + product.getName() + ": $" + product.getPrice());
        saveInventory();
    }
    
    // Get product by ID
    public Product getProductById(String id) throws InventoryException {
        Product product = inventory.get(id);
        if (product == null) {
            throw new InventoryException("Product with ID " + id + " not found.");
        }
        return product;
    }
    
    // Remove product from inventory
    public void removeProduct(String id) throws InventoryException {
        Product product = getProductById(id);
        inventory.remove(id);
        System.out.println("Removed product: " + product.getName());
        saveInventory();
    }
    
    // Display all products
    public void displayAllProducts() {
        if (inventory.isEmpty()) {
            System.out.println("Inventory is empty.");
            return;
        }
        
        System.out.println("\n==== CURRENT INVENTORY ====");
        System.out.println("ID    | Name                 | Price     | Qty   | Expiry     | Status");
        System.out.println("------------------------------------------------------------------------------");
        
        List<Product> productList = new ArrayList<>(inventory.values());
        productList.sort(Comparator.comparing(Product::getName));
        
        for (Product product : productList) {
            System.out.println(product);
        }
        System.out.println("------------------------------------------------------------------------------");
        System.out.println("Total Items: " + productList.size());
        System.out.println("Total Value: $" + calculateTotalInventoryValue());
    }
    
    // Find soon to expire products (within days)
    public List<Product> findSoonToExpireProducts(int days) {
        List<Product> soonToExpire = new ArrayList<>();
        LocalDate threshold = LocalDate.now().plusDays(days);
        
        for (Product product : inventory.values()) {
            if (!product.isExpired() && product.getExpiryDate().isBefore(threshold)) {
                soonToExpire.add(product);
            }
        }
        
        return soonToExpire;
    }
    
    // Find expired products
    public List<Product> findExpiredProducts() {
        List<Product> expired = new ArrayList<>();
        
        for (Product product : inventory.values()) {
            if (product.isExpired()) {
                expired.add(product);
            }
        }
        
        return expired;
    }
    
    // Calculate total inventory value
    public double calculateTotalInventoryValue() {
        double total = 0;
        for (Product product : inventory.values()) {
            total += product.getTotalValue();
        }
        return total;
    }
    
    // Search products by name keyword
    public List<Product> searchProductsByName(String keyword) {
        List<Product> results = new ArrayList<>();
        String lowercaseKeyword = keyword.toLowerCase();
        
        for (Product product : inventory.values()) {
            if (product.getName().toLowerCase().contains(lowercaseKeyword)) {
                results.add(product);
            }
        }
        
        return results;
    }
    
    // Save inventory to file
    @SuppressWarnings("unchecked")
    private void saveInventory() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(INVENTORY_FILE))) {
            oos.writeObject(new HashMap<>(inventory));
            System.out.println("Inventory saved to file.");
        } catch (IOException e) {
            System.err.println("Error saving inventory: " + e.getMessage());
        }
    }
    
    // Load inventory from file
    @SuppressWarnings("unchecked")
    private void loadInventory() {
        File file = new File(INVENTORY_FILE);
        if (!file.exists()) {
            System.out.println("No existing inventory file found. Starting with empty inventory.");
            return;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            inventory = (Map<String, Product>) ois.readObject();
            System.out.println("Inventory loaded from file: " + inventory.size() + " products.");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading inventory: " + e.getMessage());
            inventory = new HashMap<>(); // Start with empty inventory on error
        }
    }
}

// Main inventory application
public class InventoryApp {
    private static Scanner scanner = new Scanner(System.in);
    private static InventoryManager manager = new InventoryManager();
    private static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public static void main(String[] args) {
        boolean running = true;
        
        while (running) {
            displayMenu();
            int choice = getIntInput("Enter your choice: ");
            
            try {
                switch (choice) {
                    case 1: // Add product
                        addProductWorkflow();
                        break;
                    case 2: // Update quantity
                        updateQuantityWorkflow();
                        break;
                    case 3: // Update price
                        updatePriceWorkflow();
                        break;
                    case 4: // Remove product
                        removeProductWorkflow();
                        break;
                    case 5: // View all products
                        manager.displayAllProducts();
                        break;
                    case 6: // Search by name
                        searchProductsWorkflow();
                        break;
                    case 7: // View expiring soon
                        viewExpiringSoonWorkflow();
                        break;
                    case 8: // View expired
                        viewExpiredWorkflow();
                        break;
                    case 0: // Exit
                        running = false;
                        System.out.println("Thank you for using the Inventory Management System!");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (InventoryException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage());
            }
            
            // Pause before showing menu again
            if (running) {
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }
        
        scanner.close();
    }
    
    private static void displayMenu() {
        System.out.println("\n==== INVENTORY MANAGEMENT SYSTEM ====");
        System.out.println("1. Add new product");
        System.out.println("2. Update product quantity");
        System.out.println("3. Update product price");
        System.out.println("4. Remove product");
        System.out.println("5. View all products");
        System.out.println("6. Search products by name");
        System.out.println("7. View products expiring soon");
        System.out.println("8. View expired products");
        System.out.println("0. Exit");
    }
    
    private static void addProductWorkflow() throws InventoryException {
        System.out.println("\n-- Add New Product --");
        
        String id = getStringInput("Enter product ID: ");
        String name = getStringInput("Enter product name: ");
        double price = getDoubleInput("Enter price: $");
        int quantity = getIntInput("Enter quantity: ");
        LocalDate expiryDate = getDateInput("Enter expiry date (yyyy-MM-dd): ");
        
        manager.addProduct(id, name, price, quantity, expiryDate);
    }
    
    private static void updateQuantityWorkflow() throws InventoryException {
        System.out.println("\n-- Update Product Quantity --");
        
        String id = getStringInput("Enter product ID: ");
        System.out.println("Current product: " + manager.getProductById(id));
        
        System.out.println("Enter quantity change:");
        System.out.println("(positive number to add, negative number to remove)");
        int change = getIntInput("Quantity change: ");
        
        manager.updateQuantity(id, change);
    }
    
    private static void updatePriceWorkflow() throws InventoryException {
        System.out.println("\n-- Update Product Price --");
        
        String id = getStringInput("Enter product ID: ");
        System.out.println("Current product: " + manager.getProductById(id));
        
        double newPrice = getDoubleInput("Enter new price: $");
        
        manager.updatePrice(id, newPrice);
    }
    
    private static void removeProductWorkflow() throws InventoryException {
        System.out.println("\n-- Remove Product --");
        
        String id = getStringInput("Enter product ID to remove: ");
        System.out.println("Product to remove: " + manager.getProductById(id));
        
        String confirm = getStringInput("Are you sure you want to remove this product? (y/n): ");
        if (confirm.equalsIgnoreCase("y")) {
            manager.removeProduct(id);
        } else {
            System.out.println("Product removal cancelled.");
        }
    }
    
    private static void searchProductsWorkflow() {
        System.out.println("\n-- Search Products --");
        
        String keyword = getStringInput("Enter search keyword: ");
        List<Product> results = manager.searchProductsByName(keyword);
        
        if (results.isEmpty()) {
            System.out.println("No products found matching: " + keyword);
        } else {
            System.out.println("\n==== SEARCH RESULTS FOR: " + keyword + " ====");
            System.out.println("ID    | Name                 | Price     | Qty   | Expiry     | Status");
            System.out.println("------------------------------------------------------------------------------");
            
            for (Product product : results) {
                System.out.println(product);
            }
        }
    }
    
    private static void viewExpiringSoonWorkflow() {
        System.out.println("\n-- Products Expiring Soon --");
        
        int days = getIntInput("Enter number of days to check: ");
        List<Product> expiringSoon = manager.findSoonToExpireProducts(days);
        
        if (expiringSoon.isEmpty()) {
            System.out.println("No products expiring within the next " + days + " days.");
        } else {
            System.out.println("\n==== PRODUCTS EXPIRING WITHIN " + days + " DAYS ====");
            System.out.println("ID    | Name                 | Price     | Qty   | Expiry     | Status");
            System.out.println("------------------------------------------------------------------------------");
            
            // Sort by expiry date (soonest first)
            expiringSoon.sort(Comparator.comparing(Product::getExpiryDate));
            
            for (Product product : expiringSoon) {
                System.out.println(product);
            }
        }
    }
    
    private static void viewExpiredWorkflow() {
        System.out.println("\n-- Expired Products --");
        
        List<Product> expired = manager.findExpiredProducts();
        
        if (expired.isEmpty()) {
            System.out.println("No expired products found.");
        } else {
            System.out.println("\n==== EXPIRED PRODUCTS ====");
            System.out.println("ID    | Name                 | Price     | Qty   | Expiry     | Status");
            System.out.println("------------------------------------------------------------------------------");
            
            // Sort by expiry date (oldest first)
            expired.sort(Comparator.comparing(Product::getExpiryDate));
            
            for (Product product : expired) {
                System.out.println(product);
            }
        }
    }
    
    // Helper methods for input handling
    private static String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
    
    private static int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }
    
    private static double getDoubleInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine();
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }
    
    private static LocalDate getDateInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine();
                return LocalDate.parse(input, dateFormatter);
            } catch (Exception e) {
                System.out.println("Invalid date format. Please use yyyy-MM-dd.");
            }
        }
    }
}
