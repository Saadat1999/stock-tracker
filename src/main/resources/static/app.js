const delay = ms => new Promise(r => setTimeout(r, ms));

async function fetchJson(url, opts = {}) {
    const res = await fetch(url, opts);
    const raw = await res.text();
    let data;
    try { data = raw ? JSON.parse(raw) : null; } catch { data = raw; }
    if (!res.ok) {
        const msg = typeof data === 'string' ? data : data?.message || data?.error || raw || `Error ${res.status}`;
        throw new Error(msg);
    }
    return data;
}

function fmt(v) { const n = Number(v); return Number.isFinite(n) ? `$${n.toFixed(2)}` : '—'; }
function fmtLarge(v) {
    const n = Number(v);
    if (!Number.isFinite(n) || n <= 0) return '—';
    return new Intl.NumberFormat('en-US', { notation: 'compact', maximumFractionDigits: 2 }).format(n);
}
function fmtVol(v) { return new Intl.NumberFormat('en-US').format(v); }

function setStatus(id, msg, tone = '') {
    const el = document.getElementById(id);
    if (!el) return;
    el.className = 'status' + (tone ? ' ' + tone : '');
    el.innerHTML = `<span class="status-dot"></span>${msg}`;
}

// ── Navigation ──
const navItems = document.querySelectorAll('.nav-item');
const pages = document.querySelectorAll('.page');
navItems.forEach(btn => {
    btn.addEventListener('click', () => {
        navItems.forEach(b => b.classList.remove('active'));
        pages.forEach(p => p.classList.remove('active'));
        btn.classList.add('active');
        document.getElementById('page-' + btn.dataset.page).classList.add('active');
        if (btn.dataset.page === 'favorites') loadFavorites();
    });
});

// ── Enter key helpers ──
function onEnter(inputId, btnId) {
    document.getElementById(inputId).addEventListener('keydown', e => {
        if (e.key === 'Enter') document.getElementById(btnId).click();
    });
}
onEnter('dash-symbol', 'dash-load-btn');
onEnter('hist-symbol', 'hist-load-btn');
onEnter('ov-symbol', 'ov-load-btn');
onEnter('fav-symbol', 'fav-save-btn');

// ── Sparkline ──
function buildChart(history) {
    const line = document.getElementById('history-line');
    const area = document.getElementById('chart-area');
    const badge = document.getElementById('dash-chart-badge');
    if (!line) return;
    if (!history.length) { line.setAttribute('points', ''); area.setAttribute('points', ''); return; }

    const closes = history.map(d => Number(d.close)).filter(Number.isFinite);
    if (!closes.length) return;

    const min = Math.min(...closes), max = Math.max(...closes);
    const W = 600, H = 130, pad = 10;
    const pts = closes.map((v, i) => {
        const x = closes.length === 1 ? W / 2 : (i / (closes.length - 1)) * W;
        const y = max === min ? H / 2 : H - ((v - min) / (max - min)) * (H - pad * 2) - pad;
        return `${x.toFixed(1)},${y.toFixed(1)}`;
    });

    line.setAttribute('points', pts.join(' '));
    const areaPoints = [`0,${H}`, ...pts, `${W},${H}`].join(' ');
    area.setAttribute('points', areaPoints);
    if (badge) { badge.textContent = `${history.length} sessions`; badge.className = 'badge green'; }
}

// ── History table ──
function renderTable(history) {
    const tb = document.getElementById('history-table');
    if (!tb) return;
    if (!history.length) { tb.innerHTML = '<tr><td class="empty-row" colspan="6">No data.</td></tr>'; return; }
    tb.innerHTML = history.slice(0, 30).map(d => {
        const chg = Number(d.close) - Number(d.open);
        const cls = chg >= 0 ? 'up' : 'down';
        return `<tr>
      <td>${d.date}</td>
      <td>${fmt(d.open)}</td>
      <td class="${cls}">${fmt(d.close)}</td>
      <td class="up">${fmt(d.high)}</td>
      <td class="down">${fmt(d.low)}</td>
      <td>${fmtVol(d.volume)}</td>
    </tr>`;
    }).join('');
}

// ── Dashboard ──
document.getElementById('dash-load-btn').addEventListener('click', async () => {
    const sym = document.getElementById('dash-symbol').value.trim().toUpperCase();
    if (!sym) return;
    const btn = document.getElementById('dash-load-btn');
    btn.disabled = true;
    setStatus('dash-status', `Loading ${sym}… (rate-limited, may take a moment)`);
    try {
        const quote = await fetchJson(`/api/v1/stocks/${sym}`);
        document.getElementById('dash-price').textContent = fmt(quote.price);
        document.getElementById('dash-symbol-label').textContent = quote.symbol;
        document.getElementById('dash-trading-day').textContent = `Trading day: ${quote.tradingDay}`;

        await delay(1100);
        const history = await fetchJson(`/api/v1/stocks/${sym}/history?days=30`);
        buildChart(history);

        if (history.length) {
            const highDay = history.reduce((b, c) => c.high > b.high ? c : b);
            const lowDay = history.reduce((b, c) => c.low < b.low ? c : b);
            document.getElementById('dash-high').textContent = fmt(highDay.high);
            document.getElementById('dash-high-date').textContent = `On ${highDay.date}`;
            document.getElementById('dash-low').textContent = fmt(lowDay.low);
            document.getElementById('dash-low-date').textContent = `On ${lowDay.date}`;
        }
        setStatus('dash-status', `${sym} loaded successfully.`, 'success');
    } catch (e) {
        setStatus('dash-status', e.message, 'error');
    } finally {
        btn.disabled = false;
    }
});

// ── History page ──
document.getElementById('hist-load-btn').addEventListener('click', async () => {
    const sym = document.getElementById('hist-symbol').value.trim().toUpperCase();
    if (!sym) return;
    const btn = document.getElementById('hist-load-btn');
    btn.disabled = true;
    setStatus('hist-status', `Loading history for ${sym}…`);
    try {
        const history = await fetchJson(`/api/v1/stocks/${sym}/history?days=30`);
        renderTable(history);
        setStatus('hist-status', `${history.length} sessions loaded for ${sym}.`, 'success');
    } catch (e) {
        setStatus('hist-status', e.message, 'error');
    } finally {
        btn.disabled = false;
    }
});

// ── Overview page ──
document.getElementById('ov-load-btn').addEventListener('click', async () => {
    const sym = document.getElementById('ov-symbol').value.trim().toUpperCase();
    if (!sym) return;
    const btn = document.getElementById('ov-load-btn');
    btn.disabled = true;
    setStatus('ov-status', `Loading overview for ${sym}…`);
    try {
        const ov = await fetchJson(`/api/v1/stocks/${sym}/overview`);
        document.getElementById('ov-name').textContent = ov.name || ov.symbol || 'Unknown';
        document.getElementById('ov-sector').textContent = ov.sector || '—';
        document.getElementById('ov-asset').textContent = ov.asset || '—';
        document.getElementById('ov-desc').textContent = ov.description || 'No description available.';
        document.getElementById('ov-industry').textContent = ov.industry || '—';
        document.getElementById('ov-mktcap').textContent = fmtLarge(ov.marketCapt);
        document.getElementById('ov-div-yield').textContent = ov.dividendYield || '—';
        document.getElementById('ov-div-date').textContent = ov.dividendDate || '—';
        document.getElementById('ov-sym-stat').textContent = ov.symbol || sym;
        document.getElementById('ov-sector-stat').textContent = ov.sector || '—';
        document.getElementById('ov-asset-stat').textContent = ov.asset || '—';
        setStatus('ov-status', `Overview loaded for ${sym}.`, 'success');
    } catch (e) {
        setStatus('ov-status', e.message, 'error');
    } finally {
        btn.disabled = false;
    }
});

// ── Favorites ──
function favCardHTML(stock) {
    return `<div class="fav-card">
    <div class="fav-eyebrow">Watchlist</div>
    <div class="fav-symbol">${stock.symbol}</div>
    <div class="fav-price">${fmt(stock.price)}</div>
    <div class="fav-meta">Trading day: ${stock.tradingDay}</div>
  </div>`;
}

async function loadFavorites() {
    const list = document.getElementById('favorites-list');
    setStatus('fav-status', 'Loading favorites…');
    try {
        const favs = await fetchJson('/api/v1/stocks/favorites');
        const badge = document.getElementById('fav-badge');
        if (Array.isArray(favs) && favs.length > 0) {
            list.innerHTML = favs.map(favCardHTML).join('');
            badge.textContent = favs.length;
            badge.classList.add('show');
            setStatus('fav-status', `${favs.length} favorite${favs.length !== 1 ? 's' : ''} loaded.`, 'success');
        } else {
            list.innerHTML = `<div class="fav-empty"><h3>No favorites yet</h3><p>Add a ticker symbol above to build your watchlist.</p></div>`;
            badge.classList.remove('show');
            setStatus('fav-status', 'No favorites saved yet.');
        }
    } catch (e) {
        setStatus('fav-status', e.message, 'error');
    }
}

document.getElementById('fav-save-btn').addEventListener('click', async () => {
    const sym = document.getElementById('fav-symbol').value.trim().toUpperCase();
    if (!sym) return;
    const btn = document.getElementById('fav-save-btn');
    btn.disabled = true;
    setStatus('fav-status', `Saving ${sym} to favorites…`);
    try {
        await fetchJson('/api/v1/stocks/favorites', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ symbol: sym })
        });
        document.getElementById('fav-symbol').value = '';
        setStatus('fav-status', `${sym} added to favorites.`, 'success');
        await loadFavorites();
    } catch (e) {
        setStatus('fav-status', e.message, 'error');
    } finally {
        btn.disabled = false;
    }
});

document.getElementById('fav-refresh-btn').addEventListener('click', async () => {
    const btn = document.getElementById('fav-refresh-btn');
    btn.disabled = true;
    try { await loadFavorites(); } catch (e) { setStatus('fav-status', e.message, 'error'); }
    finally { btn.disabled = false; }
});

// Load favorites count on init
(async () => {
    try {
        const favs = await fetchJson('/api/v1/stocks/favorites');
        if (Array.isArray(favs) && favs.length > 0) {
            const badge = document.getElementById('fav-badge');
            badge.textContent = favs.length;
            badge.classList.add('show');
        }
    } catch {}
})();