// GenerateToken.js - Generates JWT and refresh token
print('Starting GenerateToken.js execution');
var jwt = require('jsonwebtoken');

var clientId = context.getVariable('request.queryparam.client_id');
print('client_id: ' + clientId);
var scope = context.getVariable('request.queryparam.scope');
print('scope: ' + scope);
var now = Math.floor(Date.now() / 1000);
var expiry = now + 3600;

var secret = 'super-secret'; // Replace with secure KVM in production
print('Generating JWT...');
var token = jwt.sign({
    iss: 'https://api.example.com',
    sub: clientId,
    scope: scope,
    iat: now,
    exp: expiry
}, secret);
print('JWT generated');

print('Generating refresh token...');
var refreshToken = require('uuid').v4();
print('Refresh token generated');

context.setVariable('generated.token', token);
context.setVariable('generated.refresh_token', refreshToken);
context.setVariable('generated.expires_in', 3600);
print('Token and refresh token set in context');
