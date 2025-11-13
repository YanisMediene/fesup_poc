package com.fesup.config;

import com.fesup.entity.Role;
import com.fesup.entity.User;
import com.fesup.repository.RoleRepository;
import com.fesup.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Créer les rôles s'ils n'existent pas
        Role superAdminRole = roleRepository.findByName("ROLE_SUPERADMIN")
                .orElseGet(() -> {
                    Role role = new Role("ROLE_SUPERADMIN");
                    return roleRepository.save(role);
                });
        
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> {
                    Role role = new Role("ROLE_ADMIN");
                    return roleRepository.save(role);
                });
        
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                    Role role = new Role("ROLE_USER");
                    return roleRepository.save(role);
                });
        
        // Créer le super-administrateur par défaut s'il n'existe pas
        if (!userRepository.existsByEmail("superadmin@fesup.fr")) {
            User superAdmin = new User();
            superAdmin.setEmail("superadmin@fesup.fr");
            superAdmin.setPassword(passwordEncoder.encode("superadmin123"));
            superAdmin.setNom("Super Admin");
            superAdmin.setPrenom("FESUP");
            superAdmin.setEnabled(true);
            
            Set<Role> superRoles = new HashSet<>();
            superRoles.add(superAdminRole);
            superRoles.add(adminRole); // SuperAdmin a aussi le rôle Admin
            superAdmin.setRoles(superRoles);
            
            userRepository.save(superAdmin);
            System.out.println("✅ Super-administrateur par défaut créé : superadmin@fesup.fr / superadmin123");
        }
        
        // Créer l'administrateur par défaut s'il n'existe pas
        if (!userRepository.existsByEmail("admin@fesup.fr")) {
            User admin = new User();
            admin.setEmail("admin@fesup.fr");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNom("Admin");
            admin.setPrenom("FESUP");
            admin.setEnabled(true);
            
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            admin.setRoles(roles);
            
            userRepository.save(admin);
            System.out.println("✅ Administrateur par défaut créé : admin@fesup.fr / admin123");
        }
    }
}
