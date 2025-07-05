// TokenRevoker.js - Marks a token as revoked (in-memory, for demo only)
// In production, use a persistent store or KVM for revoked tokens
print('Starting TokenRevoker.js execution');
var revokedTokens = context.getVariable('revoked.tokens') || {};
var token = context.getVariable('request.formparam.token');
if (token) {
    print('Revoking token...');
    revokedTokens[token] = true;
    context.setVariable('revoked.tokens', revokedTokens);
    context.setVariable('revoke.success', true);
    print('Token revoked and response set');
} else {
    context.setVariable('revoke.success', false);
    context.setVariable('revoke.error', 'No token provided');
    print('No token provided, revocation failed');
}
