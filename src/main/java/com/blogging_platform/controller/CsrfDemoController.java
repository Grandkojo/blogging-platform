package com.blogging_platform.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blogging_platform.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Demo endpoints for CSRF (Cross-Site Request Forgery) token mechanism.
 * Used only to demonstrate how CSRF protection works for form-based or session-based flows.
 * The main API uses stateless JWT and does not use CSRF.
 */
@RestController
@RequestMapping("/demo")
@Tag(name = "CSRF Demo", description = "Demonstration of CSRF token for form/session-based flows (not used by main JWT API)")
public class CsrfDemoController {

    /**
     * Returns the current CSRF token. Call this first to obtain the token; use the same
     * session (cookie) for the subsequent POST. Send the token in the next request
     * as header {@code X-CSRF-TOKEN} or as form parameter {@code _csrf}.
     */
    @GetMapping("/csrf-token")
    @Operation(summary = "Get CSRF token", description = "Returns the CSRF token for the current session. Use X-CSRF-TOKEN header or _csrf parameter when posting to /demo/csrf-submit.")
    public ResponseEntity<Map<String, String>> getCsrfToken(HttpServletRequest request) {
        request.getSession(true);
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (token == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "CSRF token not available"));
        }
        return ResponseEntity.ok(Map.of(
                "token", token.getToken(),
                "headerName", token.getHeaderName(),
                "parameterName", token.getParameterName()
        ));
    }

    /**
     * Form-style submit protected by CSRF. Requires a valid CSRF token in the request
     * (header X-CSRF-TOKEN or parameter _csrf) and the same session as when the token was obtained.
     */
    @PostMapping("/csrf-submit")
    @Operation(summary = "Submit with CSRF", description = "Accepts a POST only if the request includes a valid CSRF token (X-CSRF-TOKEN header or _csrf parameter). Demonstrates CSRF protection.")
    public ResponseEntity<ApiResponse<Object>> csrfSubmit(@RequestBody(required = false) Map<String, String> body) {
        String message = body != null && body.containsKey("message") ? body.get("message") : "received";
        return ResponseEntity.ok(ApiResponse.success(org.springframework.http.HttpStatus.OK, Map.of("message", message), "CSRF-protected submit accepted"));
    }
}
