package com.soumadeep.MoneyManager.Controller;

import com.soumadeep.MoneyManager.DTO.AuthDTO;
import com.soumadeep.MoneyManager.DTO.ProfileDTO;
import com.soumadeep.MoneyManager.Service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping("/register")
    public ResponseEntity<ProfileDTO> registerProfile(@RequestBody ProfileDTO profileDTO){
        ProfileDTO registeredProfile = profileService.registerProfile(profileDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredProfile);
    }

    @GetMapping("/activate")
    public ResponseEntity<Map<String, String>> activateProfile(@RequestParam("token") String activationToken) {
        String result = profileService.activateProfile(activationToken);
        return switch (result) {
            case "ACTIVATED" -> ResponseEntity.ok(Map.of("status", "ACTIVATED", "message", "Profile activated successfully."));
            case "ALREADY_ACTIVE" -> ResponseEntity.ok(Map.of("status", "ALREADY_ACTIVE", "message", "Your account is already activated."));
            default -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status", "NOT_FOUND", "message", "Activation token not found or invalid."));
        };
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String,Object>> login(@RequestBody AuthDTO authDTO) {
        try {
            if(!profileService.isEmailRegistered(authDTO.getEmail())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "message", "Email not registered. Please register first."));
            }

            if(!profileService.isAccountActive(authDTO.getEmail())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "message", "Account is not active. Please activate your account via the email sent to you."));
            }
            Map<String,Object> response = profileService.authenticateAndGenerateToken(authDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/profile/image")
    public ResponseEntity<ProfileDTO> updateProfileImage(@RequestBody Map<String, String> body) {
        ProfileDTO updated = profileService.updateProfileImage(body.get("profileImageUrl"));
        return ResponseEntity.status(HttpStatus.OK).body(updated);
    }

    @PostMapping("/auth/google")
    public ResponseEntity<Map<String, Object>> googleLogin(@RequestBody AuthDTO authDTO) {
        try {
            Map<String, Object> response = profileService.authenticateWithGoogle(authDTO.getToken());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
}
