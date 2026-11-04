const QRCode = require('qrcode');
const fs = require('fs');
const path = require('path');

const targets = [
  {
    name: 'qr_website_ios.png',
    text: 'https://ais-dev-y2nw5fuzaehiyzh3k2sxfw-712942562588.asia-east1.run.app',
    title: 'Website & iOS Live Storefront'
  },
  {
    name: 'qr_android_app.png',
    text: 'https://ais-dev-y2nw5fuzaehiyzh3k2sxfw-712942562588.asia-east1.run.app/saif-vegetable.apk',
    title: 'Android App Direct Download (APK)'
  },
  {
    name: 'qr_upi_pay.png',
    text: 'upi://pay?pa=8452953630@ptsbi&pn=Saif%20Vegetable&mc=5411&cu=INR',
    title: 'Saif Vegetable UPI Payment'
  },
  {
    name: 'qr_domain_custom.png',
    text: 'https://saifvegetable.in',
    title: 'Custom Domain Storefront'
  },
  {
    name: 'qr_domain_apk.png',
    text: 'https://saifvegetable.in/apk',
    title: 'Custom Domain Android APK Download'
  }
];

const publicImgDir = path.join(__dirname, 'public', 'images');
const androidResDir = path.join(__dirname, 'app', 'src', 'main', 'res', 'drawable');

if (!fs.existsSync(publicImgDir)) fs.mkdirSync(publicImgDir, { recursive: true });
if (!fs.existsSync(androidResDir)) fs.mkdirSync(androidResDir, { recursive: true });

async function run() {
  for (const t of targets) {
    const pubPath = path.join(publicImgDir, t.name);
    const appPath = path.join(androidResDir, t.name);

    await QRCode.toFile(pubPath, t.text, {
      width: 512,
      margin: 4, // ISO/IEC standard 4-module quiet zone required for instant camera detection
      color: {
        dark: '#000000', // Pure high-contrast black for 100% camera scanning accuracy
        light: '#FFFFFF'
      },
      errorCorrectionLevel: 'M'
    });

    fs.copyFileSync(pubPath, appPath);
    console.log(`Generated QR for ${t.title} [${t.text}] -> ${pubPath}`);
  }
}

run().catch(console.error);
