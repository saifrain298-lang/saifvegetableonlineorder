# Saif Vegetable — Standalone Deployment & Setup Guide

**Proprietor:** Saif Hakim Rain  
**Location:** Premier Exotica 2, Vidyavihar, Mumbai, Maharashtra  
**Service:** 39 Farm-Fresh Vegetables from Vashi APMC Mandi, Doorstep Delivery across Mumbai  

---

## 1. Quick Start (Run Locally)

This package contains everything required to run the full Saif Vegetable website with zero external dependencies. The built-in Node.js server uses native modules (`http`, `fs`, `path`).

### Prerequisites
- Node.js 18+ installed on your computer ([https://nodejs.org](https://nodejs.org))

### Instructions
1. Extract this ZIP archive to a folder.
2. Open your terminal / command prompt in the extracted folder.
3. Start the server:
   ```bash
   node server.js
   ```
   *(Or run `npm start`)*
4. Open your browser and navigate to:
   ```
   http://localhost:3000
   ```

---

## 2. Deploy to Google Cloud Run (1-Command Deployment)

If you have the Google Cloud SDK (`gcloud`) installed:
```bash
# Set your GCP project
gcloud config set project YOUR_PROJECT_ID

# Deploy directly from source to Cloud Run
gcloud run deploy saif-vegetable \
  --source . \
  --platform managed \
  --region asia-east1 \
  --allow-unauthenticated \
  --port 3000
```
Cloud Run will output your live public URL with full HTTPS enabled.

---

## 3. Deploy to Firebase Hosting / Vercel / Netlify

### Firebase Hosting:
```bash
npm install -g firebase-tools
firebase login
firebase init hosting # Set public directory to 'public'
firebase deploy --only hosting
```

### Vercel / Netlify:
- Simply drag and drop the `public` folder onto Netlify Drop ([app.netlify.com/drop](https://app.netlify.com/drop)) or import this repository into Vercel.

---

## 4. Key Features Included

- **39 Farm-Fresh Vegetables:** High-resolution photos, prices, unit selection (500g, 1kg, 2kg), and Mumbai address validation.
- **Stock Management:** Real-time stock tracking with atomic deduction, oversell prevention, and owner replenishment.
- **P&L Accounting Engine:** Gross sales, COGS, delivery fees, operating expenses, and net profit with date filtering.
- **Saif AI Assistant:** Customer chatbot answering price, stock, store hours, and delivery queries.
- **Owner Dashboard:** PIN-protected (Default PIN: `1234`, changeable inside dashboard).
- **SEO & Google Search Console:** Verified meta tag `<meta name="google-site-verification" content="Sird896XygwAdRLM-rO1zEhJVfMb4ShnnMVBNZye-hg" />`, `sitemap.xml`, and `robots.txt`.
