const http = require('http');
const fs = require('fs');
const path = require('path');
const authService = require('./auth_service');
const ordersService = require('./orders_service');

let QRCode = null;
try {
  QRCode = require('qrcode');
} catch (e) {
  console.warn('Optional QRCode package not installed:', e.message);
}

// Global crash protection to ensure the server never dies unexpectedly
process.on('uncaughtException', (err) => {
  console.error('[UNCAUGHT EXCEPTION]', err && err.stack ? err.stack : err);
});
process.on('unhandledRejection', (reason) => {
  console.error('[UNHANDLED REJECTION]', reason);
});

const PORT = parseInt(process.env.APP_PORT || process.env.DEFAULT_APP_PORT || '3000', 10);
const PUBLIC_DIR = path.join(__dirname, 'public');

const MIME_TYPES = {
  '.html': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.webp': 'image/webp',
  '.svg': 'image/svg+xml',
  '.ico': 'image/x-icon',
  '.apk': 'application/vnd.android.package-archive',
  '.zip': 'application/zip',
  '.txt': 'text/plain; charset=utf-8',
  '.xml': 'application/xml; charset=utf-8'
};

function sendJson(res, statusCode, obj, extraHeaders = {}) {
  res.writeHead(statusCode, {
    'Content-Type': 'application/json; charset=utf-8',
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
    'Access-Control-Allow-Headers': 'Content-Type, Authorization, X-Requested-With',
    ...extraHeaders
  });
  res.end(JSON.stringify(obj));
}

function parseJsonBody(req) {
  return new Promise((resolve, reject) => {
    let body = '';
    req.on('data', chunk => {
      body += chunk;
      if (body.length > 1e6) { // 1MB limit for DoS prevention
        req.destroy();
        reject(new Error('Payload Too Large'));
      }
    });
    req.on('end', () => {
      try {
        resolve(body ? JSON.parse(body) : {});
      } catch (e) {
        reject(new Error('Invalid JSON format'));
      }
    });
  });
}

const server = http.createServer(async (req, res) => {
  // Normalize URL and detect client host
  const rawHost = (req.headers['x-forwarded-host'] || req.headers.host || 'localhost').toLowerCase().split(':')[0];
  const parsedUrl = new URL(req.url, `http://${rawHost}`);
  let pathname = decodeURIComponent(parsedUrl.pathname);

  // Handle global CORS preflight requests
  if (req.method === 'OPTIONS') {
    res.writeHead(204, {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type, Authorization, X-Requested-With',
      'Access-Control-Max-Age': '86400'
    });
    res.end();
    return;
  }

  // Health check endpoint
  if (pathname === '/healthz' || pathname === '/_health' || pathname === '/health') {
    res.writeHead(200, { 'Content-Type': 'text/plain' });
    res.end('OK');
    return;
  }

  // Subdomain & dedicated host route handling
  if (pathname === '/') {
    if (rawHost.startsWith('wholesale.')) {
      res.writeHead(302, { 'Location': '/#wholesale' });
      res.end();
      return;
    }
    if (rawHost.startsWith('mumbai.')) {
      res.writeHead(302, { 'Location': '/#mumbai' });
      res.end();
      return;
    }
    if (rawHost.startsWith('app.')) {
      res.writeHead(302, { 'Location': '/#app' });
      res.end();
      return;
    }
  }

  // Clean path aliases & single-page application entry points
  if (pathname === '/s' || pathname === '/go' || pathname === '/veg') {
    res.writeHead(302, { 'Location': '/#home' });
    res.end();
    return;
  }
  if (pathname === '/wholesale' || pathname === '/bulk') {
    res.writeHead(302, { 'Location': '/#wholesale' });
    res.end();
    return;
  }
  if (pathname === '/mumbai' || pathname === '/contact') {
    res.writeHead(302, { 'Location': '/#mumbai' });
    res.end();
    return;
  }
  if (pathname === '/app' || pathname === '/qr') {
    res.writeHead(302, { 'Location': '/#app' });
    res.end();
    return;
  }
  if (pathname === '/apk' || pathname === '/download') {
    res.writeHead(302, { 'Location': '/saif-vegetable.apk' });
    res.end();
    return;
  }
  if (pathname === '/pay' || pathname === '/upi') {
    res.writeHead(302, { 'Location': 'upi://pay?pa=8452953630@ptsbi&pn=Saif%20Vegetable&cu=INR' });
    res.end();
    return;
  }
  if (pathname === '/about' || pathname === '/domain' || pathname === '/domains') {
    res.writeHead(302, { 'Location': '/#about' });
    res.end();
    return;
  }
  if (pathname === '/orders') {
    res.writeHead(302, { 'Location': '/#orders' });
    res.end();
    return;
  }
  if (pathname === '/dashboard' || pathname === '/admin') {
    res.writeHead(302, { 'Location': '/#dashboard' });
    res.end();
    return;
  }

  // Dynamic sitemap adapting to whichever domain (custom domain or Cloud Run) requests it
  if (pathname === '/sitemap.xml') {
    const sitemapFile = path.join(PUBLIC_DIR, 'sitemap.xml');
    fs.readFile(sitemapFile, 'utf-8', (err, xmlContent) => {
      if (err) {
        res.writeHead(404, { 'Content-Type': 'text/plain' });
        res.end('Sitemap not found');
        return;
      }
      const proto = req.headers['x-forwarded-proto'] || 'https';
      const origin = `${proto}://${rawHost}`;
      const tailored = xmlContent
        .replace(/https:\/\/ais-[a-z0-9\-]+\.asia-east1\.run\.app/g, origin);
      res.writeHead(200, {
        'Content-Type': 'application/xml; charset=utf-8',
        'Cache-Control': 'public, max-age=3600'
      });
      res.end(tailored);
    });
    return;
  }

  // -------------------------------------------------------------
  // DYNAMIC QR CODE STREAMING ENDPOINT (/api/qr)
  // Generates 100% compliant ISO/IEC QR codes dynamically
  // -------------------------------------------------------------
  if (pathname === '/api/qr') {
    const type = parsedUrl.searchParams.get('type') || '';
    let text = parsedUrl.searchParams.get('text');
    if (!text) {
      const proto = req.headers['x-forwarded-proto'] || 'https';
      const origin = `${proto}://${rawHost}`;
      if (type === 'apk') {
        text = `${origin}/saif-vegetable.apk`;
      } else if (type === 'upi') {
        text = 'upi://pay?pa=8452953630@ptsbi&pn=Saif%20Vegetable&mc=5411&cu=INR';
      } else if (type === 'domain') {
        text = 'https://saifvegetableonline.we24.in';
      } else if (type === 'domain_apk') {
        text = 'https://saifvegetableonline.we24.in/apk';
      } else {
        text = (rawHost.includes('localhost') || rawHost.includes('127.0.0.1')) ? 'https://saifvegetableonline.we24.in' : origin;
      }
    }

    if (QRCode) {
      try {
        res.writeHead(200, {
          'Content-Type': 'image/png',
          'Cache-Control': 'public, max-age=3600',
          'Access-Control-Allow-Origin': '*'
        });
        QRCode.toFileStream(res, text, {
          width: 512,
          margin: 4,
          color: { dark: '#000000', light: '#FFFFFF' },
          errorCorrectionLevel: 'M'
        });
        return;
      } catch (err) {
        console.error('QRCode streaming error:', err.message);
      }
    }

    // Fallback: serve static pre-rendered QR PNG if available
    const fallbackPath = type === 'apk' 
      ? path.join(PUBLIC_DIR, 'images', 'qr_apk.png') 
      : path.join(PUBLIC_DIR, 'images', 'qr_website.png');
    if (fs.existsSync(fallbackPath)) {
      res.writeHead(200, {
        'Content-Type': 'image/png',
        'Cache-Control': 'public, max-age=3600',
        'Access-Control-Allow-Origin': '*'
      });
      fs.createReadStream(fallbackPath).pipe(res);
      return;
    }

    // Ultimate fallback: 1x1 transparent PNG
    const transparentPng = Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=', 'base64');
    res.writeHead(200, {
      'Content-Type': 'image/png',
      'Content-Length': transparentPng.length,
      'Access-Control-Allow-Origin': '*'
    });
    res.end(transparentPng);
    return;
  }

  // -------------------------------------------------------------
  // CUSTOMER ORDERING API ENDPOINTS (/api/orders/*)
  // Direct Guest Ordering Without Mandatory OTP
  // -------------------------------------------------------------
  if (pathname === '/api/orders' && (req.method === 'GET' || req.method === 'HEAD')) {
    const orders = ordersService.getOrders();
    sendJson(res, 200, { success: true, count: orders.length, orders });
    return;
  }

  if (pathname === '/api/orders' && req.method === 'POST') {
    try {
      const clientIp = req.headers['x-forwarded-for']?.split(',')[0]?.trim() || req.socket.remoteAddress || '127.0.0.1';
      const body = await parseJsonBody(req);
      const result = ordersService.createOrder(body, clientIp);
      sendJson(res, result.success ? 200 : 400, result);
    } catch (err) {
      sendJson(res, 400, { success: false, error: err.message });
    }
    return;
  }

  if (pathname === '/api/orders/status' && req.method === 'POST') {
    try {
      const body = await parseJsonBody(req);
      const result = ordersService.updateOrderStatus(body.orderId, body.status);
      sendJson(res, result.success ? 200 : 400, result);
    } catch (err) {
      sendJson(res, 400, { success: false, error: err.message });
    }
    return;
  }

  // Domain & Subdomain Configuration Info Endpoint
  if (pathname === '/api/domain/info' && (req.method === 'GET' || req.method === 'HEAD')) {
    const primaryCloudRun = (process.env.APP_URL ? new URL(process.env.APP_URL).host : 'ais-dev-y2nw5fuzaehiyzh3k2sxfw-712942562588.asia-east1.run.app');
    sendJson(res, 200, {
      status: 'ok',
      primaryCustomDomain: 'https://saifvegetableonline.we24.in',
      primaryLiveUrl: `https://${primaryCloudRun}`,
      allDomains: [
        { domain: 'saifvegetableonline.ai.studio', role: 'Official Google AI Studio Preferred Domain (Ready to Claim)', target: 'Primary Production Website & Store', ssl: 'Google Managed SSL' },
        { domain: 'saifvegetableonline.we24.in', role: 'Primary Official Custom Domain (Active)', target: 'Homepage & Fresh Produce Store', ssl: 'Cloudflare SSL / Managed' },
        { domain: 'saifvegetable.in', role: 'Main Official Website', target: 'Homepage / Shopping Catalogue', ssl: 'Auto-Managed' },
        { domain: 'saifvegetable.store', role: 'Main Vegetable Shopping Store', target: 'Full Catalogue & Cart', ssl: 'Auto-Managed' },
        { domain: 'saifveg.in', role: 'Short Website Address', target: 'Quick Mobile Link', ssl: 'Auto-Managed' },
        { domain: 'saifvegetable.ai', role: 'Official Brand & AI Mandi', target: 'Brand + Voice / AI Assistant', ssl: 'Auto-Managed' },
        { domain: 'wholesale.saifvegetable.in', role: 'Wholesale Mandi Section', target: 'Mandi Bulk Crates & B2B Rates', ssl: 'Auto-Managed' },
        { domain: 'mumbai.saifvegetable.in', role: 'Mumbai Delivery Section', target: 'City Coverage, Pincodes, Vidyavihar Store', ssl: 'Auto-Managed' },
        { domain: 'app.saifvegetable.in', role: 'Customer Application', target: 'Android APK (24MB) & iOS PWA Hub', ssl: 'Auto-Managed' },
        { domain: 'order.saifvegetable.in', role: 'Direct Customer Ordering Page', target: 'Fast Ordering & Cart Checkout', ssl: 'Auto-Managed' },
        { domain: primaryCloudRun, role: 'Google Cloud Run Live HTTPS', target: 'Active Deployed Application', ssl: 'Google Trust Services (Active)' }
      ],
      dnsConfiguration: {
        primaryDomain: {
          subdomain: 'saifvegetableonline',
          zone: 'we24.in',
          fullDomain: 'saifvegetableonline.we24.in',
          type: 'CNAME',
          target: primaryCloudRun,
          cloudflareDirectSetup: 'In Cloudflare DNS for we24.in: Add CNAME saifvegetableonline -> ' + primaryCloudRun
        },
        rootDomainARecords: [
          { host: '@ (saifvegetable.in)', type: 'A', value: '216.239.32.21' },
          { host: '@ (saifvegetable.store)', type: 'A', value: '216.239.32.21' },
          { host: '@ (saifveg.in)', type: 'A', value: '216.239.32.21' },
          { host: '@ (saifvegetable.ai)', type: 'A', value: '216.239.32.21' }
        ],
        subdomainCnameRecords: [
          { host: 'saifvegetableonline', domain: 'saifvegetableonline.we24.in', type: 'CNAME', value: primaryCloudRun },
          { host: 'wholesale', domain: 'wholesale.saifvegetable.in', type: 'CNAME', value: primaryCloudRun },
          { host: 'mumbai', domain: 'mumbai.saifvegetable.in', type: 'CNAME', value: primaryCloudRun },
          { host: 'app', domain: 'app.saifvegetable.in', type: 'CNAME', value: primaryCloudRun },
          { host: 'order', domain: 'order.saifvegetable.in', type: 'CNAME', value: primaryCloudRun },
          { host: 'www', domain: 'www.saifvegetable.in', type: 'CNAME', value: primaryCloudRun }
        ]
      }
    });
    return;
  }

  // -------------------------------------------------------------
  // SECURE AUTHENTICATION API ENDPOINTS (/api/auth/*)
  // -------------------------------------------------------------
  if (pathname.startsWith('/api/auth/')) {
    const clientIp = req.headers['x-forwarded-for']?.split(',')[0]?.trim() || req.socket.remoteAddress || '127.0.0.1';

    // 1. Check SMS Gateway status
    if (pathname === '/api/auth/provider-status' && req.method === 'GET') {
      const activeProvider = authService.getActiveSmsProvider();
      sendJson(res, 200, {
        status: 'ok',
        activeProvider,
        isSmsLive: activeProvider !== 'none',
        supportedGateways: [
          { id: 'fast2sms', name: 'Fast2SMS (Recommended for India Quick OTP)', envVar: 'FAST2SMS_API_KEY' },
          { id: 'twilio', name: 'Twilio SMS Gateway', envVar: 'TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN, TWILIO_PHONE_NUMBER' },
          { id: 'msg91', name: 'MSG91 Enterprise SMS', envVar: 'MSG91_AUTH_KEY, MSG91_TEMPLATE_ID' }
        ],
        instructions: activeProvider === 'none'
          ? 'To send actual SMS to customer phones, add FAST2SMS_API_KEY, TWILIO_ACCOUNT_SID & TWILIO_AUTH_TOKEN, or MSG91_AUTH_KEY in AI Studio Secrets panel.'
          : `Live SMS delivery active via ${activeProvider.toUpperCase()}.`
      });
      return;
    }

    // 2. Request OTP via SMS
    if (pathname === '/api/auth/send-otp' && req.method === 'POST') {
      try {
        const body = await parseJsonBody(req);
        const result = await authService.requestOtp(body.phone, body.name, clientIp);
        sendJson(res, result.success ? 200 : 400, result);
      } catch (err) {
        sendJson(res, 400, { success: false, error: err.message });
      }
      return;
    }

    // Direct OTP-Free Customer Registration
    if (pathname === '/api/auth/save-customer' && req.method === 'POST') {
      try {
        const body = await parseJsonBody(req);
        const phone = String(body.phone || '').trim().replace(/\D/g, '');
        const name = String(body.name || 'Customer').trim();
        const address = String(body.address || '').trim();
        const pincode = String(body.pincode || '400077').trim();

        if (phone.length !== 10) {
          sendJson(res, 400, { success: false, error: 'Please enter a valid 10-digit mobile number' });
          return;
        }

        const user = {
          id: `cust_${phone}`,
          phone,
          name,
          address,
          pincode,
          verified: true,
          isLoggedIn: true,
          savedAt: new Date().toISOString()
        };
        sendJson(res, 200, { success: true, user });
      } catch (err) {
        sendJson(res, 400, { success: false, error: err.message });
      }
      return;
    }

    // 3. Verify OTP
    if (pathname === '/api/auth/verify-otp' && req.method === 'POST') {
      try {
        const body = await parseJsonBody(req);
        const result = authService.verifyOtp(body.phone, body.otp, body.name);
        const extraHeaders = {};
        if (result.success && result.token) {
          extraHeaders['Set-Cookie'] = `saif_session=${result.token}; Path=/; Max-Age=2592000; SameSite=Lax; HttpOnly`;
        }
        sendJson(res, result.success ? 200 : 400, result, extraHeaders);
      } catch (err) {
        sendJson(res, 400, { success: false, error: err.message });
      }
      return;
    }

    // 4. Session Verification
    if (pathname === '/api/auth/session' && req.method === 'GET') {
      const authHeader = req.headers['authorization'];
      const bearerToken = authHeader?.startsWith('Bearer ') ? authHeader.slice(7).trim() : null;
      const cookieToken = (req.headers.cookie || '').split(';').find(c => c.trim().startsWith('saif_session='))?.split('=')[1]?.trim();
      const token = bearerToken || cookieToken;
      const session = authService.getSession(token);

      if (session) {
        sendJson(res, 200, { authenticated: true, user: session });
      } else {
        sendJson(res, 200, { authenticated: false });
      }
      return;
    }

    // 5. Logout
    if (pathname === '/api/auth/logout' && req.method === 'POST') {
      const authHeader = req.headers['authorization'];
      const bearerToken = authHeader?.startsWith('Bearer ') ? authHeader.slice(7).trim() : null;
      const cookieToken = (req.headers.cookie || '').split(';').find(c => c.trim().startsWith('saif_session='))?.split('=')[1]?.trim();
      const token = bearerToken || cookieToken;
      authService.removeSession(token);
      sendJson(res, 200, { success: true }, {
        'Set-Cookie': 'saif_session=; Path=/; Max-Age=0; HttpOnly'
      });
      return;
    }

    // 6. Test SMS Gateway (Live carrier delivery test)
    if (pathname === '/api/auth/test-sms-gateway' && req.method === 'POST') {
      try {
        const body = await parseJsonBody(req);
        const result = await authService.testSmsGateway(body.phone);
        sendJson(res, result.success ? 200 : 400, result);
      } catch (err) {
        sendJson(res, 400, { success: false, error: err.message });
      }
      return;
    }

    sendJson(res, 404, { success: false, error: 'Endpoint not found' });
    return;
  }

  // Ensure any unmatched /api/ request returns JSON 404, never index.html
  if (pathname.startsWith('/api/')) {
    sendJson(res, 404, { success: false, error: 'API route not found' });
    return;
  }

  // Map to filesystem path
  let relativePath = pathname === '/' ? 'index.html' : pathname.replace(/^\/+/, '');
  let filePath = path.join(PUBLIC_DIR, relativePath);

  // Restrict website source ZIP file downloads strictly to verified owner (Saif Hakim Rain - saifrain298@gmail.com)
  if (pathname === '/saif-vegetable-website.zip') {
    const authQuery = parsedUrl.searchParams.get('auth') || parsedUrl.searchParams.get('key');
    const authHeader = req.headers['x-owner-auth'];
    const cookies = req.headers.cookie || '';
    const isAuthorized = authQuery === 'saifrain298' || 
                         authQuery === 'saifrain298@gmail.com' ||
                         authHeader === 'saifrain298@gmail.com' || 
                         cookies.includes('saif_owner_auth=true') ||
                         cookies.includes('saif_owner=saifrain298@gmail.com');

    if (!isAuthorized) {
      res.writeHead(403, { 
        'Content-Type': 'text/html; charset=utf-8',
        'Access-Control-Allow-Origin': '*'
      });
      res.end(`<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>403 Forbidden • Owner Restricted</title>
  <style>
    body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; background: #F8FAF7; color: #1B2E1E; padding: 40px 20px; text-align: center; }
    .card { max-width: 500px; margin: 40px auto; background: white; border-radius: 16px; padding: 30px; box-shadow: 0 4px 20px rgba(0,0,0,0.08); border: 1px solid #E2E8E0; }
    h1 { font-size: 1.3rem; color: #C62828; margin-bottom: 12px; }
    p { font-size: 0.88rem; color: #607262; line-height: 1.5; margin-bottom: 20px; }
    .btn { display: inline-block; background: #1E6F3D; color: white; padding: 10px 22px; border-radius: 8px; text-decoration: none; font-weight: bold; font-size: 0.88rem; }
  </style>
</head>
<body>
  <div class="card">
    <div style="font-size: 3rem; margin-bottom: 12px;">🔒</div>
    <h1>403 Forbidden: Owner Access Only</h1>
    <p>The complete website source archive is private and restricted strictly to <strong>Saif Hakim Rain (saifrain298@gmail.com)</strong>.</p>
    <p>Please log in through your Owner Dashboard to unlock authorized access and download this archive.</p>
    <a href="/#dashboard" class="btn">Go to Owner Dashboard Login</a>
  </div>
</body>
</html>`);
      return;
    }
  }

  // Prevent path traversal
  if (!filePath.startsWith(PUBLIC_DIR)) {
    res.writeHead(403, { 'Content-Type': 'text/plain' });
    res.end('403 Forbidden');
    return;
  }

  // Check if file exists
  fs.stat(filePath, (err, stats) => {
    if (err || !stats.isFile()) {
      // If requested file doesn't exist, fallback to index.html for SPA routes
      filePath = path.join(PUBLIC_DIR, 'index.html');
      fs.stat(filePath, (indexErr, indexStats) => {
        if (indexErr || !indexStats.isFile()) {
          res.writeHead(404, { 'Content-Type': 'text/plain' });
          res.end('404 Not Found');
          return;
        }
        serveFile(req, res, filePath, indexStats);
      });
      return;
    }

    serveFile(req, res, filePath, stats);
  });
});

function serveFile(req, res, filePath, stats) {
  const ext = path.extname(filePath).toLowerCase();
  const contentType = MIME_TYPES[ext] || 'application/octet-stream';

  const headers = {
    'Content-Type': contentType,
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Methods': 'GET, HEAD, OPTIONS',
    'Access-Control-Allow-Headers': '*',
    'Accept-Ranges': 'bytes',
    'X-Content-Type-Options': 'nosniff',
    'X-Frame-Options': 'SAMEORIGIN',
    'X-XSS-Protection': '1; mode=block',
    'Referrer-Policy': 'strict-origin-when-cross-origin'
  };

  if (ext === '.apk') {
    headers['Content-Disposition'] = 'attachment; filename="saif-vegetable.apk"';
    headers['Cache-Control'] = 'public, max-age=3600';
  } else if (ext === '.zip') {
    headers['Content-Disposition'] = 'attachment; filename="saif-vegetable-website.zip"';
    headers['Cache-Control'] = 'private, no-cache, no-store';
    headers['Set-Cookie'] = 'saif_owner_auth=true; Path=/; Max-Age=86400; SameSite=Lax';
  } else if (ext === '.html') {
    headers['Cache-Control'] = 'no-cache, no-store, must-revalidate';
  } else {
    headers['Cache-Control'] = 'public, max-age=86400';
  }

  if (req.method === 'HEAD') {
    headers['Content-Length'] = stats.size;
    res.writeHead(200, headers);
    res.end();
    return;
  }

  // Range request handling (crucial for smooth APK downloads on mobile)
  const range = req.headers.range;
  if (range) {
    const parts = range.replace(/bytes=/, '').split('-');
    const start = parseInt(parts[0], 10);
    const end = parts[1] ? parseInt(parts[1], 10) : stats.size - 1;

    if (start >= stats.size || end >= stats.size) {
      headers['Content-Range'] = `bytes */${stats.size}`;
      res.writeHead(416, headers);
      res.end();
      return;
    }

    const chunksize = end - start + 1;
    headers['Content-Range'] = `bytes ${start}-${end}/${stats.size}`;
    headers['Content-Length'] = chunksize;
    res.writeHead(206, headers);
    fs.createReadStream(filePath, { start, end }).pipe(res);
  } else {
    headers['Content-Length'] = stats.size;
    res.writeHead(200, headers);
    fs.createReadStream(filePath).pipe(res);
  }
}

server.listen(PORT, '0.0.0.0', () => {
  console.log(`Saif Vegetable Web Server running at http://0.0.0.0:${PORT}/`);
  console.log(`Serving files from: ${PUBLIC_DIR}`);
});
