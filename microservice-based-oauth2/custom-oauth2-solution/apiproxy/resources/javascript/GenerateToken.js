// GenerateToken.js - Generates JWT and refresh token
var jwt = require('jsonwebtoken');

var clientId = context.getVariable('request.queryparam.client_id');
var scope = context.getVariable('request.queryparam.scope');
var now = Math.floor(Date.now() / 1000);
var expiry = now + 3600;

var secret = 'super-secret'; // Replace with secure KVM in production
var token = jwt.sign({
    iss: 'https://api.example.com',
    sub: clientId,
    scope: scope,
    iat: now,
    exp: expiry
}, secret);

var refreshToken = require('uuid').v4();

context.setVariable('generated.token', token);
context.setVariable('generated.refresh_token', refreshToken);
context.setVariable('generated.expires_in', 3600);
