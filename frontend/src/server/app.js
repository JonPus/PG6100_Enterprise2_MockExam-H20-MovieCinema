const express = require('express');
const path = require('path');

const { createProxyMiddleware } = require('http-proxy-middleware');
const app = express();

if (process.env.DEV) {
    const proxiedServer = 'http://localhost:80';
    app.use(
        '/api/*',
        createProxyMiddleware({ target: proxiedServer, changeOrigin: true })
    );
}

app.use(express.static('public'));

app.use((req, res, next) => {
    res.sendFile(path.resolve(__dirname, '..', '..', 'public', 'index.html'));
});

module.exports = { app };
