// ValidateToken.js - Validates JWT
var jwt = require('jsonwebtoken');

var authHeader = context.getVariable('request.header.Authorization');
if (!authHeader || !authHeader.startsWith('Bearer ')) {
    context.setVariable('token.valid', false);
} else {
    var token = authHeader.replace('Bearer ', '').trim();
    try {
        var decoded = jwt.verify(token, 'super-secret'); // Replace with KVM in prod
        context.setVariable('token.valid', true);
        context.setVariable('token.scope', decoded.scope);
        context.setVariable('token.client_id', decoded.sub);
    } catch (e) {
        context.setVariable('token.valid', false);
        context.setVariable('token.error', e.message);
    }
}
