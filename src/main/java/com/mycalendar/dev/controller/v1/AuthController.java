package com.mycalendar.dev.controller.v1;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.mycalendar.dev.payload.request.GoogleSignInRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/google")
    public ResponseEntity<?> authenticateWithGoogle(@RequestBody GoogleSignInRequest request) {
        String idToken = request.getIdToken();

        GoogleIdToken.Payload payload = verifyGoogleToken(idToken);
        if (payload == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid ID token.");
        }

        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        // TODO: เช็คใน database, สร้าง user ถ้ายังไม่มี

        // TODO: สร้าง JWT / Session ส่งกลับไป
        return ResponseEntity.ok(Map.of(
                "email", email,
                "name", name,
                "picture", picture
        ));
    }

    private GoogleIdToken.Payload verifyGoogleToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    JacksonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList("356083374793-mcr38v6de8h922c5q4l96884drkosskl.apps.googleusercontent.com")) // ใช้ webClientId ของคุณ
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                return idToken.getPayload();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
