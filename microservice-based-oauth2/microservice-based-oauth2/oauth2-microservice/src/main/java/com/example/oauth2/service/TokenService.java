@Service
@RequiredArgsConstructor
public class TokenService {
  private final AccessTokenRepository accessRepo;
  private final RefreshTokenRepository refreshRepo;
  @Value("${jwt.secret}") String jwtSecret;
  @Value("${jwt.issuer}") String issuer;
  @Value("${jwt.access-token-validity}") long accessValidity;
  @Value("${jwt.refresh-token-validity}") long refreshValidity;

  public Map<String, Object> generateTokens(String clientId, String scope) {
    Instant now = Instant.now();

    Algorithm algo = Algorithm.HMAC256(jwtSecret);
    String jwt = JWT.create()
      .withIssuer(issuer)
      .withSubject(clientId)
      .withClaim("scope", scope)
      .withIssuedAt(Date.from(now))
      .withExpiresAt(Date.from(now.plusSeconds(accessValidity)))
      .sign(algo);

    String refresh = UUID.randomUUID().toString();

    AccessToken at = new AccessToken();
    at.setTokenId(jwt);
    at.setClientId(clientId);
    at.setScope(scope);
    at.setIssuedAt(now);
    at.setExpiresAt(now.plusSeconds(accessValidity));
    at.setTokenValue(jwt);
    at.setRevoked(false);
    accessRepo.save(at);

    RefreshToken rt = new RefreshToken();
    rt.setRefreshToken(refresh);
    rt.setClientId(clientId);
    rt.setIssuedAt(now);
    rt.setExpiresAt(now.plusSeconds(refreshValidity));
    rt.setRevoked(false);
    refreshRepo.save(rt);

    return Map.of(
      "access_token", jwt,
      "refresh_token", refresh,
      "token_type", "Bearer",
      "expires_in", accessValidity
    );
  }

  public boolean validateToken(String token) {
    try {
      Algorithm algo = Algorithm.HMAC256(jwtSecret);
      JWT.require(algo).withIssuer(issuer).build().verify(token);
      Optional<AccessToken> stored = accessRepo.findById(token);
      return stored.isPresent() && !stored.get().isRevoked()
          && stored.get().getExpiresAt().isAfter(Instant.now());
    } catch (Exception e) {
      return false;
    }
  }

  public Map<String, Object> refreshToken(String refreshToken, String clientId) {
    Optional<RefreshToken> stored = refreshRepo.findById(refreshToken);
    if (stored.isEmpty() || stored.get().isRevoked()
        || stored.get().getExpiresAt().isBefore(Instant.now())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
    }

    refreshRepo.save(new RefreshToken(refreshToken, clientId,
        stored.get().getIssuedAt(), stored.get().getExpiresAt(), true));

    return generateTokens(clientId, "read write");
  }

  public void revokeToken(String token) {
    accessRepo.findById(token).ifPresent(t -> {
      t.setRevoked(true);
      accessRepo.save(t);
    });
  }
}
