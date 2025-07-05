// RefreshTokenHandler.js - Issues new access/refresh tokens if refresh token is valid (in-memory demo)
var jwt = require('jsonwebtoken');
var uuid = require('uuid');

var refreshToken = context.getVariable('request.formparam.refresh_token');
var clientId = context.getVariable('request.formparam.client_id');
var validRefreshTokens = context.getVariable('valid.refresh.tokens') || {};

if (!refreshToken || !validRefreshTokens[refreshToken]) {
    context.setVariable('refresh.valid', false);
    context.setVariable('refresh.error', 'Invalid or expired refresh token');
} else {
    // Revoke old refresh token
    delete validRefreshTokens[refreshToken];
    context.setVariable('valid.refresh.tokens', validRefreshTokens);

    var now = Math.floor(Date.now() / 1000);
    var accessExpiry = now + 3600;
    var refreshExpiry = now + 604800;
    var secret = 'super-secret';
    var newAccessToken = jwt.sign({
        iss: 'https://api.example.com',
        sub: clientId,
        scope: 'read write',
        iat: now,
        exp: accessExpiry
    }, secret);
    var newRefresh = uuid.v4();
    validRefreshTokens[newRefresh] = true;
    context.setVariable('valid.refresh.tokens', validRefreshTokens);
    context.setVariable('refresh.valid', true);
    context.setVariable('new.access_token', newAccessToken);
    context.setVariable('new.refresh_token', newRefresh);
    context.setVariable('new.expires_in', 3600);
}
