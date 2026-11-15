package com.example.webshop.config;

import com.example.webshop.entity.Cart;
import com.example.webshop.entity.Category;
import com.example.webshop.entity.Product;
import com.example.webshop.entity.User;
import com.example.webshop.enums.Role;
import com.example.webshop.repository.CartRepository;
import com.example.webshop.repository.CategoryRepository;
import com.example.webshop.repository.ProductRepository;
import com.example.webshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Seeds the initial ADMIN account and a demo catalog when the database is empty.
 * Enabled with app.seed.enabled=true. The same data is available in database/data.sql.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private static final String IMG_ELECTRONICS = "/images/categories/electronics.svg";
    private static final String IMG_BOOKS = "/images/categories/books.svg";
    private static final String IMG_HOME = "/images/categories/home.svg";
    private static final String IMG_SPORT = "/images/categories/sport.svg";

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() == 0) {
            createUser("Admin", "Administrator", "admin", "admin123", Role.ADMIN);
            createUser("Petar", "Petrović", "kupac", "kupac123", Role.CUSTOMER);
            log.info("Seed: created initial accounts (admin / admin123, kupac / kupac123)");
        }
        if (categoryRepository.count() == 0) {
            seedCatalog();
            log.info("Seed: created demo categories and products");
        }
    }

    private void seedCatalog() {
        Category electronics = category("ELE", "Elektronika", "Slušalice, pametni satovi i računarska oprema.");
        Category books = category("KNJ", "Knjige", "Klasici domaće književnosti i priručnici.");
        Category home = category("KUC", "Kuća i bašta", "Sve što čini dom prijatnijim.");
        Category sport = category("SPO", "Sport", "Oprema za trening i rekreaciju.");

        product("ELE-001", "Bežične slušalice Aurora",
                "Bluetooth slušalice sa aktivnim poništavanjem buke i baterijom do 30 sati.",
                IMG_ELECTRONICS, "7990.00", 25, electronics, true);
        product("ELE-002", "Pametni sat Puls 2",
                "Praćenje pulsa, sna i treninga, vodootporan do 50 metara.",
                IMG_ELECTRONICS, "14990.00", 12, electronics, true);
        product("ELE-003", "Mehanička tastatura Škljoc",
                "Kompaktna mehanička tastatura sa pozadinskim osvetljenjem i srpskim rasporedom.",
                IMG_ELECTRONICS, "9490.00", 8, electronics, false);
        product("KNJ-001", "Na Drini ćuprija",
                "Roman Ive Andrića, tvrdi povez.",
                IMG_BOOKS, "1199.00", 40, books, true);
        product("KNJ-002", "Derviš i smrt",
                "Roman Meše Selimovića, džepno izdanje.",
                IMG_BOOKS, "1099.00", 30, books, false);
        product("KNJ-003", "Rečnik srpskog jezika",
                "Jednotomni rečnik sa više od 85.000 odrednica.",
                IMG_BOOKS, "4500.00", 5, books, false);
        product("KUC-001", "Bakarna džezva",
                "Ručno kovana džezva za četiri šoljice kafe.",
                IMG_HOME, "1890.00", 50, home, true);
        product("KUC-002", "Set šolja Šumadija",
                "Šest keramičkih šolja sa tradicionalnim motivima.",
                IMG_HOME, "2450.00", 18, home, false);
        product("KUC-003", "Baštenske makaze",
                "Makaze za orezivanje sa čeličnim sečivom i ergonomskom drškom.",
                IMG_HOME, "1350.00", 0, home, false);
        product("SPO-001", "Fudbalska lopta Đerdap",
                "Lopta veličine 5 za travnate i veštačke terene.",
                IMG_SPORT, "3290.00", 22, sport, false);
        product("SPO-002", "Prostirka za jogu",
                "Neklizajuća prostirka debljine 6 mm sa trakom za nošenje.",
                IMG_SPORT, "2190.00", 35, sport, true);
        product("SPO-003", "Čelična flašica 0,75 l",
                "Termo flašica koja održava temperaturu pića do 12 sati.",
                IMG_SPORT, "1490.00", 60, sport, false);
    }

    private void createUser(String firstName, String lastName, String username, String rawPassword, Role role) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setEnabled(true);
        userRepository.save(user);

        Cart cart = new Cart();
        cart.setUser(user);
        cartRepository.save(cart);
    }

    private Category category(String code, String name, String description) {
        Category category = new Category();
        category.setCode(code);
        category.setName(name);
        category.setDescription(description);
        category.setActive(true);
        return categoryRepository.save(category);
    }

    private void product(String code, String name, String description, String image, String price,
                         int stock, Category category, boolean featured) {
        Product product = new Product();
        product.setCode(code);
        product.setName(name);
        product.setDescription(description);
        product.setImage(image);
        product.setPrice(new BigDecimal(price));
        product.setStockQuantity(stock);
        product.setCategory(category);
        product.setActive(true);
        product.setFeatured(featured);
        productRepository.save(product);
    }
}
