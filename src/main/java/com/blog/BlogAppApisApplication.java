package com.blog;

import java.util.Set;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.blog.config.AppConstants;
import com.blog.config.AppProperties;
import com.blog.entities.Role;
import com.blog.entities.User;
import com.blog.repositories.RoleRepo;
import com.blog.repositories.UserRepo;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class BlogAppApisApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(BlogAppApisApplication.class);

	private final PasswordEncoder passwordEncoder;
	private final RoleRepo roleRepo;
	private final UserRepo userRepo;

	public BlogAppApisApplication(PasswordEncoder passwordEncoder, RoleRepo roleRepo, UserRepo userRepo) {
		this.passwordEncoder = passwordEncoder;
		this.roleRepo = roleRepo;
		this.userRepo = userRepo;
	}

	public static void main(String[] args) {
		SpringApplication.run(BlogAppApisApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

	/** Seeds the two roles and a default admin user the first time the app starts. */
	@Override
	public void run(String... args) {
		seedRole(AppConstants.ADMIN_USER, "ROLE_ADMIN");
		seedRole(AppConstants.NORMAL_USER, "ROLE_USER");
		seedAdminUser();
	}

	private void seedRole(Integer id, String name) {
		if (!roleRepo.existsById(id)) {
			Role role = new Role();
			role.setId(id);
			role.setName(name);
			roleRepo.save(role);
			log.info("Seeded role {}", name);
		}
	}

	private void seedAdminUser() {
		String adminEmail = "admin@blog.com";
		if (userRepo.findByEmail(adminEmail).isPresent()) {
			return;
		}
		Role adminRole = roleRepo.findById(AppConstants.ADMIN_USER).orElseThrow();

		User admin = new User();
		admin.setName("Administrator");
		admin.setEmail(adminEmail);
		admin.setAbout("Default administrator account");
		admin.setPassword(passwordEncoder.encode("Admin@123"));
		admin.setRoles(Set.of(adminRole));
		userRepo.save(admin);
		log.info("Seeded default admin user '{}' (change the password immediately)", adminEmail);
	}
}
