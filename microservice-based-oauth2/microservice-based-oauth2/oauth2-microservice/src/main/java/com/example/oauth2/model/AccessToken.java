@Table("oauth_tokens")
public class AccessToken {
  @PrimaryKey private String tokenId;
  private String clientId;
  private String scope;
  private Instant issuedAt;
  private Instant expiresAt;
  private String tokenType = "Bearer";
  private String tokenValue;
  private boolean revoked;
  // Getters and setters...
}
