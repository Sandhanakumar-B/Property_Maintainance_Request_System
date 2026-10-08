/**
 * Global application utilities, API client, and session management.
 */

const API_BASE = '/api';

// Current session storage helper
const Auth = {
    getUser() {
        const stored = localStorage.getItem('currentUser');
        if (!stored) {
            // Default to USER1 if nothing stored yet
            const defaultUser = { id: 1, name: 'USER1', phone: '0771234567', email: 'user1@property.lk', role: 'USER', status: 'ACTIVE' };
            localStorage.setItem('currentUser', JSON.stringify(defaultUser));
            return defaultUser;
        }
        return JSON.parse(stored);
    },

    setUser(user) {
        localStorage.setItem('currentUser', JSON.stringify(user));
    },

    isAdmin() {
        const user = this.getUser();
        return user && user.role === 'ADMINISTRATOR';
    }
};

// API Fetch helper with JSON error extraction
async function apiFetch(endpoint, options = {}) {
    const defaultHeaders = {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    };

    options.headers = { ...defaultHeaders, ...options.headers };

    try {
        const response = await fetch(API_BASE + endpoint, options);
        const data = await response.json();

        if (!response.ok || !data.success) {
            const errorMsg = data.message || `Request failed with status ${response.status}`;
            throw new Error(errorMsg);
        }

        return data.data;
    } catch (err) {
        showToast(err.message, 'error');
        throw err;
    }
}

// Toast notification helper
function showToast(message, type = 'info') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transition = 'opacity 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// Render dynamic navigation bar
function renderNavbar(activePage = '') {
    const navContainer = document.getElementById('navbar');
    if (!navContainer) return;

    const user = Auth.getUser();
    const isAdmin = Auth.isAdmin();

    let linksHtml = '';
    if (isAdmin) {
        linksHtml = `
            <li><a href="admin-dashboard.html" class="nav-link ${activePage === 'admin-dashboard' ? 'active' : ''}">Admin Dashboard</a></li>
            <li><a href="admin-requests.html" class="nav-link ${activePage === 'admin-requests' ? 'active' : ''}">Manage Requests</a></li>
        `;
    } else {
        linksHtml = `
            <li><a href="user-dashboard.html" class="nav-link ${activePage === 'user-dashboard' ? 'active' : ''}">Dashboard</a></li>
            <li><a href="create-request.html" class="nav-link ${activePage === 'create-request' ? 'active' : ''}">Submit Request</a></li>
            <li><a href="my-requests.html" class="nav-link ${activePage === 'my-requests' ? 'active' : ''}">My Requests</a></li>
        `;
    }

    navContainer.innerHTML = `
        <div class="navbar">
            <a href="${isAdmin ? 'admin-dashboard.html' : 'user-dashboard.html'}" class="nav-brand">
                <span class="brand-icon">🔧</span>
                <span>Property Maintenance</span>
            </a>
            <ul class="nav-links">
                ${linksHtml}
            </ul>
            <div style="display: flex; align-items: center; gap: 1rem;">
                <div class="nav-user-badge">
                    <span>👤 <strong>${user ? user.name : 'Guest'}</strong></span>
                    <span class="user-role-tag ${isAdmin ? 'role-admin' : 'role-user'}">${user ? user.role : 'USER'}</span>
                </div>
                <a href="index.html" class="btn btn-secondary btn-sm" title="Switch User Role">🔄 Switch Account</a>
            </div>
        </div>
    `;
}

// Status badge helper
function getStatusBadge(status) {
    if (!status) return '<span class="badge">UNKNOWN</span>';
    const s = status.toUpperCase();
    if (s === 'SUBMITTED') return `<span class="badge badge-submitted">SUBMITTED</span>`;
    if (s === 'APPROVED/ASSIGNED') return `<span class="badge badge-assigned">APPROVED/ASSIGNED</span>`;
    if (s === 'IN_PROGRESS') return `<span class="badge badge-in-progress">IN PROGRESS</span>`;
    if (s === 'COMPLETED') return `<span class="badge badge-completed">COMPLETED</span>`;
    if (s === 'CANCELLED') return `<span class="badge badge-cancelled">CANCELLED</span>`;
    return `<span class="badge">${status}</span>`;
}

// Format date helper
function formatDate(dateStr) {
    if (!dateStr) return '-';
    const d = new Date(dateStr);
    return d.toLocaleString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}
