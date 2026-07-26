# Kwacha Wize — Download Site

A single-page, plain HTML/CSS/JS landing site for **Kwacha Wize**, an
Android app that turns mobile money and bank SMS alerts into clean,
tagged financial records for Malawian micro-entrepreneurs.

No build step, no framework, no dependencies. Open `index.html` in a
browser or deploy the folder as-is to any static host (GitHub Pages,
Netlify, Vercel, Cloudflare Pages, S3, etc).

## Structure

```
.
├── index.html          # all page content/markup
├── style.css            # design tokens + layout
├── script.js             # ledger ticker + screenshot tab switcher
└── assets/
    ├── logo/
    │   ├── icon.png       # app icon (favicon + download badge)
    │   └── lockup.png      # icon + wordmark, used in nav/footer
    └── screens/
        ├── home.png
        ├── transactions.png
        ├── add-cash.png
        └── paste-sms.png
```

## Before you deploy

1. **Wire up the real download.** The "Download for Android" / "Download
   APK" buttons in `index.html` (`#download-link`, and the CTA in the
   hero) currently point to `#`. Point them at your actual APK — a
   GitHub Releases asset URL is the easiest option for a sideloaded app.
2. **Update the source links.** `#source-link` and the footer GitHub
   link point at a placeholder `https://github.com/` — swap in the real
   repo URL.
3. **APK size / version.** The `#apkSize` note in the download section
   is a placeholder (`~12 MB`) — update it once you have a built APK.

## Local preview

Any static server works, e.g.:

```bash
python3 -m http.server 8000
# then open http://localhost:8000
```

## Team

* **Mike Prosper Kamanga** — UI/UX Designer and Developer
* **Patrick Solomon** — Developer
* **Thokozani Mofolo** — Project Manager / Communicator (Group Leader)
* **Denis Decal** — Business Strategist
* **Dominic Smith** — Researcher

Built in a 48-hour hackathon.
