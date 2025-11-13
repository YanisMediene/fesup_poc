package com.fesup.controller;

import com.fesup.entity.Role;
import com.fesup.entity.User;
import com.fesup.repository.RoleRepository;
import com.fesup.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/superadmin/admins")
@PreAuthorize("hasRole('SUPERADMIN')")
public class AdminManagementController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Récupère tous les utilisateurs avec rôle ADMIN ou SUPERADMIN
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllAdmins() {
        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();
        Role superAdminRole = roleRepository.findByName("ROLE_SUPERADMIN").orElseThrow();
        
        List<User> admins = userRepository.findAll().stream()
                .filter(user -> user.getRoles().contains(adminRole) || user.getRoles().contains(superAdminRole))
                .collect(Collectors.toList());
        
        List<Map<String, Object>> result = admins.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Récupère un admin par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAdminById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            return ResponseEntity.ok(convertToDTO(user.get()));
        }
        return ResponseEntity.notFound().build();
    }
    
    /**
     * Crée un nouvel admin
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createAdmin(@RequestBody Map<String, Object> adminData) {
        String email = (String) adminData.get("email");
        
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cet email existe déjà"));
        }
        
        User newAdmin = new User();
        newAdmin.setEmail(email);
        newAdmin.setPassword(passwordEncoder.encode((String) adminData.get("password")));
        newAdmin.setNom((String) adminData.get("nom"));
        newAdmin.setPrenom((String) adminData.get("prenom"));
        newAdmin.setEnabled(true);
        
        // Assigner les rôles
        Set<Role> roles = new HashSet<>();
        Boolean isSuperAdmin = (Boolean) adminData.get("isSuperAdmin");
        
        if (Boolean.TRUE.equals(isSuperAdmin)) {
            Role superAdminRole = roleRepository.findByName("ROLE_SUPERADMIN").orElseThrow();
            roles.add(superAdminRole);
        }
        
        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();
        roles.add(adminRole);
        
        newAdmin.setRoles(roles);
        
        User savedAdmin = userRepository.save(newAdmin);
        return ResponseEntity.ok(convertToDTO(savedAdmin));
    }
    
    /**
     * Met à jour un admin
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateAdmin(@PathVariable Long id, @RequestBody Map<String, Object> adminData) {
        Optional<User> userOpt = userRepository.findById(id);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        User admin = userOpt.get();
        
        // Mise à jour des informations de base
        if (adminData.containsKey("nom")) {
            admin.setNom((String) adminData.get("nom"));
        }
        if (adminData.containsKey("prenom")) {
            admin.setPrenom((String) adminData.get("prenom"));
        }
        if (adminData.containsKey("email")) {
            String newEmail = (String) adminData.get("email");
            if (!newEmail.equals(admin.getEmail()) && userRepository.existsByEmail(newEmail)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Cet email existe déjà"));
            }
            admin.setEmail(newEmail);
        }
        
        // Mise à jour du mot de passe si fourni
        if (adminData.containsKey("password") && !((String) adminData.get("password")).isEmpty()) {
            admin.setPassword(passwordEncoder.encode((String) adminData.get("password")));
        }
        
        // Mise à jour des rôles
        if (adminData.containsKey("isSuperAdmin")) {
            Set<Role> roles = new HashSet<>();
            Boolean isSuperAdmin = (Boolean) adminData.get("isSuperAdmin");
            
            if (Boolean.TRUE.equals(isSuperAdmin)) {
                Role superAdminRole = roleRepository.findByName("ROLE_SUPERADMIN").orElseThrow();
                roles.add(superAdminRole);
            }
            
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();
            roles.add(adminRole);
            
            admin.setRoles(roles);
        }
        
        User updatedAdmin = userRepository.save(admin);
        return ResponseEntity.ok(convertToDTO(updatedAdmin));
    }
    
    /**
     * Supprime un admin
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    private Map<String, Object> convertToDTO(User user) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", user.getId());
        dto.put("email", user.getEmail());
        dto.put("nom", user.getNom());
        dto.put("prenom", user.getPrenom());
        dto.put("enabled", user.isEnabled());
        
        boolean isSuperAdmin = user.getRoles().stream()
                .anyMatch(role -> "ROLE_SUPERADMIN".equals(role.getName()));
        dto.put("isSuperAdmin", isSuperAdmin);
        
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        dto.put("roles", roleNames);
        
        return dto;
    }
}
