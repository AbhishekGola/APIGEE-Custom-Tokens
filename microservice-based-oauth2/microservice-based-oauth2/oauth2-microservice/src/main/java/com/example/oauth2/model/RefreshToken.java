@Table("refresh_tokens")
public class RefreshToken {
  @PrimaryKey private String refreshToken;
  private String clientId;
  private Instant issuedAt;
  private Instant expiresAt;
  private boolean revoked;
  // Getters and setters...
}
