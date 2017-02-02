var config = {};

// Port
config.port = process.env.PORT || 8080;

// Secrets
config.sessionSecret = 'SESSION_SECRET';
config.tokenSecret = 'TOKEN_SECRET';
config.jwtSecret = 'JWT_SECRET';

// Steam Passport
config.steamReturnUrl = 'STEAM_RETURN_URL';
config.steamRealm = 'STEAM_REALM';
config.steamApiKey = 'STEAM_API_KEY';

// MongoDB
config.dbUrl = 'mongodb://localhost:27017';

module.exports = config;