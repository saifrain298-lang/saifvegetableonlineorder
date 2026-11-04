const crypto = require('crypto');
const https = require('https');

// Persistent server-side salt for HMAC hashing (never exposed to client)
const SECRET_SALT = process.env.OTP_SECRET_SALT || crypto.randomBytes(32).toString('hex');

// In-memory security tables (with automatic expiry cleanup)
const activeOtps = new Map();       // phone -> { hash, salt, expiresAt, attempts, lastRequestedAt, name }
const phoneCooldowns = new Map();   // phone -> lastSentTimestamp
const requestCounters = new Map();  // phone -> [timestamps] (sliding 15-min window)
const ipCounters = new Map();       // ip -> [timestamps] (sliding 1-min window)
const lockouts = new Map();         // phone -> lockedUntilTimestamp
const sessions = new Map();         // token -> { phone, name, createdAt, expiresAt }

// Configuration limits
const OTP_EXPIRY_MS = 5 * 60 * 1000;         // 5 minutes
const RESEND_COOLDOWN_MS = 30 * 1000;        // 30 seconds
const MAX_ATTEMPTS = 5;                       // Max 5 attempts before account lock
const MAX_REQUESTS_PER_15MIN = 10;           // Max 10 requests in 15 minutes per phone
const MAX_REQUESTS_PER_MIN_IP = 15;          // Max 15 requests per minute per IP
const LOCKOUT_DURATION_MS = 15 * 60 * 1000;  // 15 minutes lockout

// Cleanup expired items every 60 seconds
setInterval(() => {
  const now = Date.now();
  for (const [phone, record] of activeOtps.entries()) {
    if (now > record.expiresAt) activeOtps.delete(phone);
  }
  for (const [phone, lockUntil] of lockouts.entries()) {
    if (now > lockUntil) lockouts.delete(phone);
  }
  for (const [token, session] of sessions.entries()) {
    if (now > session.expiresAt) sessions.delete(token);
  }
}, 60000).unref();

function sanitizeIndianPhone(raw) {
  if (!raw) return null;
  const digits = String(raw).replace(/\D/g, '');
  if (digits.length === 10 && /^[6-9]\d{9}$/.test(digits)) {
    return digits;
  }
  if (digits.length === 12 && digits.startsWith('91') && /^[6-9]\d{9}$/.test(digits.slice(2))) {
    return digits.slice(2);
  }
  return null;
}

function hashOtp(otp, salt) {
  return crypto.createHmac('sha256', SECRET_SALT + salt).update(String(otp)).digest('hex');
}

function getActiveSmsProvider() {
  if (process.env.FAST2SMS_API_KEY) return 'fast2sms';
  if (process.env.TWILIO_ACCOUNT_SID && process.env.TWILIO_AUTH_TOKEN && process.env.TWILIO_PHONE_NUMBER) return 'twilio';
  if (process.env.MSG91_AUTH_KEY) return 'msg91';
  return 'none';
}

function sendHttpsRequest(urlStr, options, postData) {
  return new Promise((resolve, reject) => {
    const url = new URL(urlStr);
    const reqOptions = {
      hostname: url.hostname,
      port: url.port || 443,
      path: url.pathname + url.search,
      method: options.method || 'GET',
      headers: options.headers || {}
    };

    const req = https.request(reqOptions, (res) => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        try {
          resolve({ statusCode: res.statusCode, data: JSON.parse(data), raw: data });
        } catch (_) {
          resolve({ statusCode: res.statusCode, data: null, raw: data });
        }
      });
    });

    req.on('error', reject);
    req.setTimeout(10000, () => {
      req.destroy();
      reject(new Error('SMS Gateway connection timed out'));
    });

    if (postData) {
      req.write(postData);
    }
    req.end();
  });
}

async function dispatchSmsViaGateway(phone, otp) {
  const provider = getActiveSmsProvider();
  const formattedPhone = `+91${phone}`;

  if (provider === 'fast2sms') {
    const postBody = JSON.stringify({
      route: 'otp',
      variables_values: otp,
      numbers: phone
    });

    const res = await sendHttpsRequest('https://www.fast2sms.com/dev/bulkV2', {
      method: 'POST',
      headers: {
        'authorization': process.env.FAST2SMS_API_KEY,
        'Content-Type': 'application/json',
        'Content-Length': Buffer.byteLength(postBody)
      }
    }, postBody);

    if (res.statusCode >= 200 && res.statusCode < 300 && res.data && res.data.return) {
      return { success: true, provider: 'fast2sms', messageId: res.data.request_id || 'sent' };
    }
    throw new Error(`Fast2SMS Error: ${res.data?.message?.[0] || res.raw || 'Gateway failed'}`);
  }

  if (provider === 'twilio') {
    const accountSid = process.env.TWILIO_ACCOUNT_SID;
    const authToken = process.env.TWILIO_AUTH_TOKEN;
    const fromPhone = process.env.TWILIO_PHONE_NUMBER;
    const authHeader = 'Basic ' + Buffer.from(`${accountSid}:${authToken}`).toString('base64');

    const params = new URLSearchParams();
    params.append('To', formattedPhone);
    params.append('From', fromPhone);
    params.append('Body', `Your Saif Vegetable verification OTP is ${otp}. Valid for 5 minutes. Do not share this code.`);

    const postData = params.toString();
    const twilioUrl = `https://api.twilio.com/2010-04-01/Accounts/${accountSid}/Messages.json`;

    const res = await sendHttpsRequest(twilioUrl, {
      method: 'POST',
      headers: {
        'Authorization': authHeader,
        'Content-Type': 'application/x-www-form-urlencoded',
        'Content-Length': Buffer.byteLength(postData)
      }
    }, postData);

    if (res.statusCode >= 200 && res.statusCode < 300 && res.data && res.data.sid) {
      return { success: true, provider: 'twilio', messageId: res.data.sid };
    }
    throw new Error(`Twilio Error: ${res.data?.message || res.raw || 'SMS delivery failed'}`);
  }

  if (provider === 'msg91') {
    const authKey = process.env.MSG91_AUTH_KEY;
    const templateId = process.env.MSG91_TEMPLATE_ID || '';
    const msg91Url = `https://control.msg91.com/api/v5/otp?template_id=${templateId}&mobile=91${phone}&authkey=${authKey}&otp=${otp}`;

    const res = await sendHttpsRequest(msg91Url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' }
    });

    if (res.statusCode >= 200 && res.statusCode < 300) {
      return { success: true, provider: 'msg91', messageId: res.data?.message || 'sent' };
    }
    throw new Error(`MSG91 Error: ${res.data?.message || res.raw || 'Gateway rejected request'}`);
  }

  return { success: false, provider: 'none', error: 'No SMS gateway configured in environment' };
}

async function requestOtp(rawPhone, rawName, clientIp) {
  const now = Date.now();
  const phone = sanitizeIndianPhone(rawPhone);
  if (!phone) {
    return { success: false, error: 'Invalid mobile number. Please enter a valid 10-digit Indian phone number starting with 6-9.' };
  }

  // 1. Check IP rate limit (max 10 requests / min)
  if (clientIp) {
    const ipHistory = (ipCounters.get(clientIp) || []).filter(ts => (now - ts) < 60000);
    if (ipHistory.length >= MAX_REQUESTS_PER_MIN_IP) {
      return { success: false, error: 'Too many requests from your network. Please wait a minute before trying again.' };
    }
    ipHistory.push(now);
    ipCounters.set(clientIp, ipHistory);
  }

  // 2. Check Lockout state
  const lockedUntil = lockouts.get(phone);
  if (lockedUntil && now < lockedUntil) {
    const remainingMins = Math.ceil((lockedUntil - now) / 60000);
    return { success: false, error: `Account locked due to multiple failed verification attempts. Please retry in ${remainingMins} minutes.` };
  } else if (lockedUntil) {
    lockouts.delete(phone);
  }

  // 3. Check Cooldown (60 seconds)
  const lastRequested = phoneCooldowns.get(phone) || 0;
  if ((now - lastRequested) < RESEND_COOLDOWN_MS) {
    const waitSec = Math.ceil((RESEND_COOLDOWN_MS - (now - lastRequested)) / 1000);
    return { success: false, error: `Please wait ${waitSec} seconds before requesting a new OTP.` };
  }

  // 4. Check Per-Phone Rate Limit (Max 4 per 15 min)
  const phoneHistory = (requestCounters.get(phone) || []).filter(ts => (now - ts) < (15 * 60 * 1000));
  if (phoneHistory.length >= MAX_REQUESTS_PER_15MIN) {
    return { success: false, error: 'Too many OTP requests for this mobile number. Please try again after 15 minutes.' };
  }

  // 5. Generate secure 6-digit OTP using crypto.randomInt
  const otpNumber = crypto.randomInt(100000, 1000000);
  const otpStr = String(otpNumber);

  // 6. Generate cryptographic salt and hash
  const salt = crypto.randomBytes(16).toString('hex');
  const hash = hashOtp(otpStr, salt);

  // 7. Attempt SMS Dispatch via configured provider if available
  let smsResult = { provider: 'none' };
  let smsDispatched = false;
  let smsError = null;

  try {
    smsResult = await dispatchSmsViaGateway(phone, otpStr);
    if (smsResult && smsResult.success) {
      smsDispatched = true;
    }
  } catch (err) {
    smsError = err.message;
  }

  // 8. Commit to state (works for both SMS and Normal Website OTP)
  phoneHistory.push(now);
  requestCounters.set(phone, phoneHistory);
  phoneCooldowns.set(phone, now);

  activeOtps.set(phone, {
    hash,
    salt,
    expiresAt: now + OTP_EXPIRY_MS,
    attempts: 0,
    lastRequestedAt: now,
    name: (rawName || 'Customer').trim()
  });

  const maskedPhone = `+91 ${phone.slice(0, 2)}••••••${phone.slice(-2)}`;

  if (smsDispatched) {
    return {
      success: true,
      isSms: true,
      isNormalOtp: false,
      message: `6-digit OTP has been sent via SMS to ${maskedPhone}.`,
      maskedPhone,
      cooldownSeconds: 60,
      expiresInSeconds: 300,
      provider: smsResult.provider,
      messageId: smsResult.messageId
    };
  }

  // SMS not supported or not configured in environment: Generate Normal OTP on website
  return {
    success: true,
    isSms: false,
    isNormalOtp: true,
    normalOtp: otpStr,
    maskedPhone,
    phone,
    cooldownSeconds: 30,
    expiresInSeconds: 300,
    provider: 'normal_web',
    message: `Normal OTP generated on website: ${otpStr}`,
    notice: smsError
      ? `SMS delivery returned: ${smsError}. Generated normal OTP on website.`
      : 'SMS gateway not configured in environment. Generated normal OTP on website for instant verification.'
  };
}

function verifyOtp(rawPhone, rawOtp, clientName) {
  const now = Date.now();
  const phone = sanitizeIndianPhone(rawPhone);
  if (!phone) {
    return { success: false, error: 'Invalid phone number.' };
  }

  const otp = String(rawOtp || '').trim().replace(/\D/g, '');
  if (otp.length !== 6) {
    return { success: false, error: 'Please enter a valid 6-digit OTP code.' };
  }

  // Check lockout
  const lockedUntil = lockouts.get(phone);
  if (lockedUntil && now < lockedUntil) {
    const mins = Math.ceil((lockedUntil - now) / 60000);
    return { success: false, error: `Account temporarily locked due to failed attempts. Try again in ${mins} minutes.` };
  }

  const record = activeOtps.get(phone);
  if (!record) {
    return { success: false, error: 'No active OTP found for this number or OTP has expired. Please request a new OTP.' };
  }

  if (now > record.expiresAt) {
    activeOtps.delete(phone);
    return { success: false, error: 'OTP has expired. Please request a fresh OTP code.' };
  }

  record.attempts += 1;

  // Compute timing-safe hash comparison
  const computedHash = hashOtp(otp, record.salt);
  const isMatch = crypto.timingSafeEqual(Buffer.from(computedHash), Buffer.from(record.hash));

  if (!isMatch) {
    const remaining = MAX_ATTEMPTS - record.attempts;
    if (remaining <= 0) {
      activeOtps.delete(phone);
      lockouts.set(phone, now + LOCKOUT_DURATION_MS);
      return {
        success: false,
        error: 'Too many incorrect attempts. For security, this phone number has been locked for 15 minutes.'
      };
    }
    return {
      success: false,
      error: `Incorrect OTP. ${remaining} attempt${remaining === 1 ? '' : 's'} remaining.`
    };
  }

  // Successful verification!
  activeOtps.delete(phone); // Single-use replay protection
  lockouts.delete(phone);

  const sessionToken = crypto.randomBytes(32).toString('hex');
  const sessionUser = {
    phone,
    name: clientName || record.name || 'Valued Customer',
    verified: true,
    verifiedAt: now
  };

  sessions.set(sessionToken, {
    ...sessionUser,
    createdAt: now,
    expiresAt: now + (30 * 24 * 60 * 60 * 1000) // 30 days
  });

  return {
    success: true,
    token: sessionToken,
    user: sessionUser
  };
}

function getSession(token) {
  if (!token) return null;
  const session = sessions.get(token);
  if (!session) return null;
  if (Date.now() > session.expiresAt) {
    sessions.delete(token);
    return null;
  }
  return session;
}

function removeSession(token) {
  if (token) sessions.delete(token);
}

async function testSmsGateway(rawPhone) {
  const provider = getActiveSmsProvider();
  if (provider === 'none') {
    return {
      success: false,
      provider: 'none',
      error: 'No SMS gateway configured. Please configure FAST2SMS_API_KEY, TWILIO_ACCOUNT_SID & TWILIO_AUTH_TOKEN, or MSG91_AUTH_KEY in AI Studio Secrets.'
    };
  }
  const phone = sanitizeIndianPhone(rawPhone);
  if (!phone) {
    return { success: false, error: 'Please enter a valid 10-digit Indian test mobile number.' };
  }
  const testOtp = crypto.randomInt(100000, 1000000);
  try {
    const result = await dispatchSmsViaGateway(phone, String(testOtp));
    return {
      success: true,
      provider,
      messageId: result.messageId,
      message: `Test SMS successfully dispatched to +91 ${phone} via ${provider.toUpperCase()} (Message ID: ${result.messageId}). Check your mobile inbox!`
    };
  } catch (err) {
    return {
      success: false,
      provider,
      error: `SMS Gateway Delivery Failed: ${err.message}`
    };
  }
}

module.exports = {
  requestOtp,
  verifyOtp,
  getSession,
  removeSession,
  testSmsGateway,
  getActiveSmsProvider,
  sanitizeIndianPhone
};
