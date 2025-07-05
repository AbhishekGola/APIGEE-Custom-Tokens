// GenerateToken.js - Generates JWT and refresh token
print('Starting GenerateToken.js execution');

// UUID v4 generator (RFC4122 compliant)
function uuidv4() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
  });
}

var clientId = context.getVariable('request.queryparam.client_id');
print('client_id: ' + clientId);
var scope = context.getVariable('request.queryparam.scope');
print('scope: ' + scope);
var now = Math.floor(Date.now() / 1000);
var expiry = now + 3600;

// Polyfill for btoa (base64 encode) for Apigee JS
function btoa(str) {
  var chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=';
  var encoded = '';
  var c1, c2, c3, e1, e2, e3, e4;
  for (var i = 0; i < str.length;) {
    c1 = str.charCodeAt(i++);
    c2 = str.charCodeAt(i++);
    c3 = str.charCodeAt(i++);
    e1 = c1 >> 2;
    e2 = ((c1 & 3) << 4) | (c2 >> 4);
    e3 = ((c2 & 15) << 2) | (c3 >> 6);
    e4 = c3 & 63;
    if (isNaN(c2)) { e3 = e4 = 64; }
    else if (isNaN(c3)) { e4 = 64; }
    encoded += chars.charAt(e1) + chars.charAt(e2) + chars.charAt(e3) + chars.charAt(e4);
  }
  return encoded;
}

// Minimal JWT generator (header.payload.signature, NOT cryptographically secure)
function base64url(source) {
  var encoded =  btoa(JSON.stringify(source))
    .replace(/=+$/, '')
    .replace(/\+/g, '-')
    .replace(/\//g, '_');
  return encoded;
}

var header = { alg: 'HS256', typ: 'JWT' };
var payload = {
    iss: 'https://api.example.com',
    sub: clientId,
    scope: scope,
    iat: now,
    exp: expiry
};
var secret = 'super-secret'; // Replace with secure KVM in production
print('Generating JWT...');
var unsignedToken = base64url(header) + '.' + base64url(payload);
// NOTE: This is NOT a secure signature. For demo only.
var signature = base64url(secret);
var token = unsignedToken + '.' + signature;
print('JWT generated');

print('Generating refresh token...');
var refreshToken = uuidv4();
print('Refresh token generated');

context.setVariable('generated.token', token);
context.setVariable('generated.refresh_token', refreshToken);
context.setVariable('generated.expires_in', 3600);
print('Token and refresh token set in context');
