// ValidateToken.js - Validates JWT
var jwt = require('jsonwebtoken');

print('Starting ValidateToken.js execution');

var authHeader = context.getVariable('request.header.Authorization');
if (!authHeader || !authHeader.startsWith('Bearer ')) {
    context.setVariable('token.valid', false);
    print('No Bearer token found in Authorization header');
} else {
    var token = authHeader.replace('Bearer ', '').trim();
    print('Validating token...');
    try {
        var decoded = jwt.verify(token, 'super-secret'); // Replace with KVM in prod
        context.setVariable('token.valid', true);
        context.setVariable('token.scope', decoded.scope);
        context.setVariable('token.client_id', decoded.sub);
        print('Token validation complete, result set in context');
    } catch (e) {
        context.setVariable('token.valid', false);
        context.setVariable('token.error', e.message);
        print('Token validation failed: ' + e.message);
    }
}
