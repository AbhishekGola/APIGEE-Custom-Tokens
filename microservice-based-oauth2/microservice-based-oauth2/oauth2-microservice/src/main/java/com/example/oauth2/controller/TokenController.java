@RestController
@RequestMapping("/token")
@RequiredArgsConstructor
public class TokenController {
  private final TokenService service;

  @PostMapping("/generate")
  public ResponseEntity<?> generate(@RequestParam String client_id,
                                    @RequestParam(defaultValue = "read write") String scope) {
    return ResponseEntity.ok(service.generateTokens(client_id, scope));
  }

  @PostMapping("/validate")
  public ResponseEntity<?> validate(@RequestHeader("Authorization") String auth) {
    String token = auth.replace("Bearer ", "").trim();
    boolean valid = service.validateToken(token);
    return valid ? ResponseEntity.ok(Map.of("valid", true))
                 : ResponseEntity.status(401).body(Map.of("error", "invalid_token"));
  }

  @PostMapping("/refresh")
  public ResponseEntity<?> refresh(@RequestParam String refresh_token,
                                   @RequestParam String client_id) {
    return ResponseEntity.ok(service.refreshToken(refresh_token, client_id));
  }

  @PostMapping("/revoke")
  public ResponseEntity<?> revoke(@RequestParam String token) {
    service.revokeToken(token);
    return ResponseEntity.ok(Map.of("revoked", true));
  }
}
