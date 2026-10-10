package com.soumadeep.MoneyManager.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.soumadeep.MoneyManager.DTO.AuthDTO;
import com.soumadeep.MoneyManager.DTO.ProfileDTO;
import com.soumadeep.MoneyManager.Entity.ProfileEntity;
import com.soumadeep.MoneyManager.Repository.ProfileRepository;
import com.soumadeep.MoneyManager.Util.GoogleAuthUtil;
import com.soumadeep.MoneyManager.Util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final GoogleAuthUtil googleAuthUtil;

    @Value("${money.manager.frontend.url}")
    private String frontendUrl;

    public ProfileDTO registerProfile(ProfileDTO profileDTO){
        ProfileEntity newProfile = toEntity(profileDTO);
        newProfile.setActivationToken(UUID.randomUUID().toString());
        newProfile = profileRepository.save(newProfile);

        //Sent activation email
        String activationLink = frontendUrl + "/activate?token=" + newProfile.getActivationToken();
        String subject = "Activate Your Money Manager Account";
        String body = "Please click the following link to activate your account: " + activationLink;
        emailService.sendEmail(newProfile.getEmail(), subject, body);
        return toDTO(newProfile);
    }

    // Helper Method to convert DTO to Entity
    public ProfileEntity toEntity(ProfileDTO profileDTO){
        return ProfileEntity.builder()
                .fullName(profileDTO.getFullName())
                .email(profileDTO.getEmail())
                .password(passwordEncoder.encode(profileDTO.getPassword()))
                .profileImageUrl(profileDTO.getProfileImageUrl())
                .createdAt(profileDTO.getCreatedAt())
                .updatedAt(profileDTO.getUpdatedAt())
                .build();
    }

    // Helper Method to convert Entity to DTO
    public ProfileDTO toDTO(ProfileEntity profileEntity){
        return ProfileDTO.builder()
                .id(profileEntity.getId())
                .fullName(profileEntity.getFullName())
                .email(profileEntity.getEmail())
                .profileImageUrl(profileEntity.getProfileImageUrl())
                .createdAt(profileEntity.getCreatedAt())
                .updatedAt(profileEntity.getUpdatedAt())
                .build();
    }

    public String activateProfile(String activationToken) {
        return profileRepository.findByActivationToken(activationToken)
                .map(profile -> {
                    if (Boolean.TRUE.equals(profile.getIsActive())) {
                        return "ALREADY_ACTIVE";
                    }
                    profile.setIsActive(true);
                    profileRepository.save(profile);
                    return "ACTIVATED";
                })
                .orElse("NOT_FOUND");
    }

    public Boolean isEmailRegistered(String email) {
        return profileRepository.findByEmail(email).isPresent();
    }

    public boolean isAccountActive(String email) {
        return profileRepository.findByEmail(email)
                .map(ProfileEntity::getIsActive)
                .orElse(false);
    }

    public ProfileEntity getCurrentProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return profileRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email: " + authentication.getName()));
    }

    public ProfileDTO getPublicProfile(String email) {
        ProfileEntity currentUser=null;
        if(email==null) {
            currentUser = getCurrentProfile();
        } else {
            currentUser = profileRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email: " + email));
        }

        return ProfileDTO.builder()
                .id(currentUser.getId())
                .fullName(currentUser.getFullName())
                .email(currentUser.getEmail())
                .profileImageUrl(currentUser.getProfileImageUrl())
                .createdAt(currentUser.getCreatedAt())
                .updatedAt(currentUser.getUpdatedAt())
                .build();
    }

    public ProfileDTO updateProfileImage(String imageUrl) {
        ProfileEntity profile = getCurrentProfile();
        profile.setProfileImageUrl(imageUrl);
        profile = profileRepository.save(profile);
        return toDTO(profile);
    }

    public Map<String, Object> authenticateAndGenerateToken(AuthDTO authDTO) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authDTO.getEmail(), authDTO.getPassword()));

            //Generate JWT Token
            String token= jwtUtil.generateToken(authDTO.getEmail());

            return Map.of(
                    "token", token,
                    "user", getPublicProfile(authDTO.getEmail())
            );

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid email or password");
        }
    }

    public Map<String, Object> authenticateWithGoogle(String idTokenString) {
        try {
            GoogleIdToken.Payload payload = googleAuthUtil.verify(idTokenString);
            String email = payload.getEmail();
            String fullName = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");

            ProfileEntity profile = profileRepository.findByEmail(email)
                    .orElseGet(() -> {
                        ProfileEntity newProfile = ProfileEntity.builder()
                                .fullName(fullName)
                                .email(email)
                                .profileImageUrl(pictureUrl)
                                .isActive(true) // Google already verified this email
                                .build();
                        return profileRepository.save(newProfile);
                    });

            String token = jwtUtil.generateToken(email);
            return Map.of("token", token, "user", toDTO(profile));
        } catch (Exception e) {
            throw new RuntimeException("Google authentication failed: " + e.getMessage());
        }
    }
}