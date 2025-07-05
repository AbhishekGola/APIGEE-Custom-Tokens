// TokenRevoker.js - Marks a token as revoked (in-memory, for demo only)
// In production, use a persistent store or KVM for revoked tokens
var revokedTokens = context.getVariable('revoked.tokens') || {};
var token = context.getVariable('request.formparam.token');
if (token) {
    revokedTokens[token] = true;
    context.setVariable('revoked.tokens', revokedTokens);
    context.setVariable('revoke.success', true);
} else {
    context.setVariable('revoke.success', false);
    context.setVariable('revoke.error', 'No token provided');
}
