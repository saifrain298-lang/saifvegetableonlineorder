const fs = require('fs');
const path = require('path');

const DATA_DIR = path.join(__dirname, 'data');
const ORDERS_FILE = path.join(DATA_DIR, 'orders.json');

// Ensure data directory exists
if (!fs.existsSync(DATA_DIR)) {
  fs.mkdirSync(DATA_DIR, { recursive: true });
}

// Ensure orders.json exists
if (!fs.existsSync(ORDERS_FILE)) {
  fs.writeFileSync(ORDERS_FILE, JSON.stringify([], null, 2), 'utf-8');
}

function readOrders() {
  try {
    const raw = fs.readFileSync(ORDERS_FILE, 'utf-8');
    return JSON.parse(raw || '[]');
  } catch (err) {
    console.error('Error reading orders file:', err.message);
    return [];
  }
}

function writeOrders(orders) {
  try {
    const tmp = ORDERS_FILE + '.tmp';
    fs.writeFileSync(tmp, JSON.stringify(orders, null, 2), 'utf-8');
    fs.renameSync(tmp, ORDERS_FILE);
    return true;
  } catch (err) {
    console.error('Error writing orders file:', err.message);
    return false;
  }
}

const orderRateLimits = new Map();

function sanitize(str) {
  if (typeof str !== 'string') return '';
  return str.replace(/[<>'"&]/g, (tag) => ({
    '<': '&lt;',
    '>': '&gt;',
    "'": '&#39;',
    '"': '&quot;',
    '&': '&amp;'
  }[tag] || tag)).slice(0, 500);
}

function checkRateLimit(ip) {
  const now = Date.now();
  const windowMs = 60000; // 1 minute
  const maxRequests = 15;
  const timestamps = (orderRateLimits.get(ip) || []).filter(t => now - t < windowMs);
  if (timestamps.length >= maxRequests) {
    return false;
  }
  timestamps.push(now);
  orderRateLimits.set(ip, timestamps);
  return true;
}

function getOrders() {
  return readOrders();
}

function createOrder(data, clientIp = '127.0.0.1') {
  if (!checkRateLimit(clientIp)) {
    return { success: false, error: 'Rate limit exceeded. Please wait 1 minute before placing another order.' };
  }

  const name = sanitize(data.name || '').trim();
  const phone = (data.phone || '').trim().replace(/\D/g, '');
  const addr = sanitize(data.addr || '').trim();
  const area = sanitize(data.area || '').trim();
  const pin = (data.pin || '').trim().replace(/\D/g, '');
  const landmark = sanitize(data.landmark || '').trim();
  const email = sanitize(data.email || '').trim();
  const items = Array.isArray(data.items) ? data.items : [];
  const total = Number(data.total || 0);

  // Validation
  if (!name) {
    return { success: false, error: 'Customer full name is required.' };
  }
  if (!phone || phone.length !== 10) {
    return { success: false, error: 'Valid 10-digit mobile number is required.' };
  }
  if (!addr) {
    return { success: false, error: 'Complete delivery address is required.' };
  }
  if (!area) {
    return { success: false, error: 'Delivery area / locality in Mumbai is required.' };
  }
  if (!pin || pin.length !== 6 || !pin.startsWith('400')) {
    return { success: false, error: 'Valid 6-digit Mumbai pincode (400xxx) is required.' };
  }
  if (items.length === 0) {
    return { success: false, error: 'Cart is empty. Add at least 1 vegetable.' };
  }
  if (total <= 0) {
    return { success: false, error: 'Invalid order amount.' };
  }

  const orderId = data.id || ('SV-ORD-' + Date.now().toString().slice(-6));
  const newOrder = {
    id: orderId,
    timestamp: data.timestamp || Date.now(),
    date: data.date || new Date().toLocaleDateString('en-IN', { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' }),
    name,
    phone,
    addr,
    area,
    pin,
    landmark: (data.landmark || '').trim(),
    email: (data.email || '').trim(),
    pay: data.pay || 'UPI',
    items,
    subtotal: Number(data.subtotal || total - 10),
    delivery: Number(data.delivery || 10),
    total,
    status: data.status || 'Placed'
  };

  const orders = readOrders();
  // Check if order already exists (deduplication)
  const existingIdx = orders.findIndex(o => o.id === orderId);
  if (existingIdx !== -1) {
    orders[existingIdx] = newOrder;
  } else {
    orders.unshift(newOrder);
  }

  writeOrders(orders);
  return { success: true, order: newOrder, orderId };
}

function updateOrderStatus(orderId, status) {
  const orders = readOrders();
  const order = orders.find(o => o.id === orderId);
  if (!order) {
    return { success: false, error: 'Order not found.' };
  }
  order.status = status;
  writeOrders(orders);
  return { success: true, order };
}

module.exports = {
  getOrders,
  createOrder,
  updateOrderStatus
};
