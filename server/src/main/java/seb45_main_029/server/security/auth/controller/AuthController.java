package seb45_main_029.server.security.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import seb45_main_029.server.security.auth.service.AuthService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequestMapping("/auth")
@RequiredArgsConstructor
@RestController
public class AuthController {

    private final AuthService authService;

    @PostMapping("/reissue")
    public ResponseEntity reissue(HttpServletRequest request, HttpServletResponse response) throws IOException {

        return authService.reissue(request, response);
    }

    @DeleteMapping("/logout")
    public ResponseEntity logout(HttpServletRequest request) {

        return authService.logout(request);
    }
}
