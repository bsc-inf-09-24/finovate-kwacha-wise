// ---------------------------------------------------------------
// Kwacha Wize landing page — plain JS, no build step, no deps.
// ---------------------------------------------------------------

// ---- 1. Ledger ticker (signature "SMS → tagged transaction" strip) ----
// Real message shapes the app actually parses, paired with the tag a
// user would apply. Duplicated once so the CSS scroll-loop is seamless.
const LEDGER_ITEMS = [
  {
    sms: 'Cash In from 205586-STANLEY MKUMBA. Amt: 5,000.00MWK. Bal: 6,153.82MWK',
    tag: 'biz',
    label: '💼 Business',
    amount: '+K5,000.00'
  },
  {
    sms: 'You have received MK 355.82 from MONEY TRUST INTEREST. Bal: MK368.58',
    tag: 'per',
    label: '🏠 Personal',
    amount: '+K355.82'
  },
  {
    sms: 'Manual entry · Market stall, no SMS trail',
    tag: 'biz',
    label: '💼 Business',
    amount: '+K3,000.00'
  },
  {
    sms: 'Airtel Money: New Balance: MWK45,200.00',
    tag: 'per',
    label: '🏠 Personal',
    amount: '−K1,800.00'
  }
];

function renderTicker() {
  const track = document.getElementById('tickerTrack');
  if (!track) return;

  const buildItem = (item) => {
    const el = document.createElement('div');
    el.className = 'ticker-item';
    el.innerHTML = `
      <span class="ticker-sms">${item.sms}</span>
      <span class="ticker-arrow">→</span>
      <span class="ticker-tag ${item.tag}">${item.label}</span>
      <span class="ticker-amount">${item.amount}</span>
    `;
    return el;
  };

  // render twice back-to-back so the CSS "translateX(-50%)" loop is seamless
  [...LEDGER_ITEMS, ...LEDGER_ITEMS].forEach(item => {
    track.appendChild(buildItem(item));
  });
}

// ---- 2. Screenshot tabs ----
const SCREENS = {
  'home': {
    src: 'assets/screens/home.png',
    alt: 'Kwacha Wize home screen showing available balance and quick actions',
    caption: 'Balance, pending reviews and every action, one tap from the top.'
  },
  'transactions': {
    src: 'assets/screens/transactions.png',
    alt: 'Kwacha Wize transactions list filtered by All, Business, Personal, Unsorted',
    caption: 'Every transaction, business or personal, in one filterable list.'
  },
  'add-cash': {
    src: 'assets/screens/add-cash.png',
    alt: 'Kwacha Wize add cash entry form with amount, description, type and category',
    caption: 'Cash sales and purchases get logged and tagged in one form.'
  },
  'paste-sms': {
    src: 'assets/screens/paste-sms.png',
    alt: 'Kwacha Wize paste SMS screen for manually parsing a transaction message',
    caption: 'No live SMS on hand? Paste the message text and parse it directly.'
  }
};

function initScreenTabs() {
  const tabs = document.querySelectorAll('.screens-tab');
  const img = document.getElementById('screensImg');
  const caption = document.getElementById('screensCaption');
  if (!tabs.length || !img || !caption) return;

  tabs.forEach(tab => {
    tab.addEventListener('click', () => {
      const key = tab.dataset.target;
      const data = SCREENS[key];
      if (!data) return;

      tabs.forEach(t => {
        t.classList.remove('is-active');
        t.setAttribute('aria-selected', 'false');
      });
      tab.classList.add('is-active');
      tab.setAttribute('aria-selected', 'true');

      img.style.opacity = '0';
      window.setTimeout(() => {
        img.src = data.src;
        img.alt = data.alt;
        caption.textContent = data.caption;
        img.style.opacity = '1';
      }, 120);
    });
  });
}

document.addEventListener('DOMContentLoaded', () => {
  renderTicker();
  initScreenTabs();
});
