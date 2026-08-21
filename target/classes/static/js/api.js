// ==========================================================
// INVENTORY MANAGEMENT SYSTEM - CORE API & CLIENT CONTROLLER
// ==========================================================

const API_BASE = '/api';

// ==========================================================
// MERCHANT PAYMENT CONFIGURATION
// You can change your UPI ID or custom QR image URL below:
// ==========================================================
window.MERCHANT_CONFIG = {
    upiId: "inventorypro@upi",           // <-- Change to your real UPI ID if needed
    merchantName: "InventoryPro ERP",    // <-- Your Shop / Business Name
    customQrImageUrl: "pay.jpeg"         // <-- Custom QR Code image pay.jpeg configured
};

// Auth State Helpers
function getToken() {
    return localStorage.getItem('ims_jwt_token');
}

function setToken(token) {
    if (token) {
        localStorage.setItem('ims_jwt_token', token);
    } else {
        localStorage.removeItem('ims_jwt_token');
    }
}

function getUser() {
    try {
        const u = localStorage.getItem('ims_user');
        return u ? JSON.parse(u) : null;
    } catch (e) {
        return null;
    }
}

function setUser(user) {
    if (user) {
        localStorage.setItem('ims_user', JSON.stringify(user));
    } else {
        localStorage.removeItem('ims_user');
    }
}

function clearAuth() {
    localStorage.removeItem('ims_jwt_token');
    localStorage.removeItem('ims_user');
}

// Core Fetch Wrapper with Automatic JWT Header & Error Interceptor
async function apiRequest(path, method = 'GET', body = null) {
    const headers = {
        'Content-Type': 'application/json'
    };

    const token = getToken();
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const options = {
        method,
        headers
    };

    if (body !== null) {
        options.body = JSON.stringify(body);
    }

    let response;
    try {
        response = await fetch(API_BASE + path, options);
    } catch (networkError) {
        const error = new Error('Network error: Unable to connect to server. Please check your connection.');
        error.status = 0;
        throw error;
    }

    // Handle 401 Unauthorized
    if (response.status === 401) {
        clearAuth();
        if (!window.location.pathname.endsWith('login.html') && !window.location.pathname.endsWith('register.html')) {
            window.location.href = 'login.html?expired=1';
        }
        const error = new Error('Session expired or unauthorized. Please login again.');
        error.status = 401;
        throw error;
    }

    if (response.status === 204) {
        return null;
    }

    let data = null;
    try {
        data = await response.json();
    } catch (e) {
        data = null;
    }

    if (!response.ok) {
        let errorMsg = 'Request failed (' + response.status + ')';
        if (data) {
            if (data.message) errorMsg = data.message;
            else if (data.errors && typeof data.errors === 'object') {
                errorMsg = Object.values(data.errors).join(', ');
            } else if (data.error) errorMsg = data.error;
        }
        const error = new Error(errorMsg);
        error.status = response.status;
        error.data = data;
        throw error;
    }

    return data;
}

// Page Auth Guard
async function requireLogin(requiredRole = null) {
    const token = getToken();
    if (!token) {
        window.location.href = 'login.html';
        return null;
    }

    try {
        const user = await apiRequest('/auth/me');
        setUser(user);

        if (requiredRole) {
            const role = (user.role || '').toUpperCase();
            if (role !== requiredRole.toUpperCase() && role !== 'ADMIN') {
                showToast('Access denied: You do not have permission to view this page.', 'danger');
                setTimeout(() => { 
                    window.location.href = role === 'ADMIN' ? 'dashboard.html' : 'products.html'; 
                }, 1200);
                return null;
            }
        }
        return user;
    } catch (e) {
        clearAuth();
        window.location.href = 'login.html';
        return null;
    }
}

async function logout() {
    try {
        await apiRequest('/auth/logout', 'POST');
    } catch (ignored) {}
    clearAuth();
    window.location.href = 'login.html';
}

// Toast Notification Engine
function showToast(message, type = 'info', title = null) {
    let container = document.getElementById('toastContainer');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toastContainer';
        container.className = 'toast-container-custom';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast-custom toast-${type}`;

    let icon = 'fa-info-circle text-info';
    if (type === 'success') icon = 'fa-check-circle text-success';
    if (type === 'danger') icon = 'fa-exclamation-circle text-danger';
    if (type === 'warning') icon = 'fa-exclamation-triangle text-warning';

    const defaultTitle = type.charAt(0).toUpperCase() + type.slice(1);

    toast.innerHTML = `
        <i class="fas ${icon} fa-lg mt-1"></i>
        <div class="flex-grow-1">
            <strong class="d-block" style="font-size: 0.875rem;">${title || defaultTitle}</strong>
            <span style="font-size: 0.825rem; color: #475569;">${message}</span>
        </div>
        <button type="button" class="btn-close btn-close-sm" style="font-size: 0.7rem;" onclick="this.parentElement.remove()"></button>
    `;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => toast.remove(), 300);
    }, 4500);
}

// Global alert placeholder helper for backward compatibility
function showAlert(containerId, message, type = 'danger') {
    showToast(message, type);
    const container = document.getElementById(containerId);
    if (container) {
        container.innerHTML = `
            <div class="alert alert-${type} alert-dismissible fade show d-flex align-items-center" role="alert">
                <i class="fas ${type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle'} me-2"></i>
                <div>${message}</div>
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>`;
    }
}

// Currency and Date Formatters
function formatCurrency(value) {
    if (value === null || value === undefined || isNaN(value)) return '₹0.00';
    return '₹' + Number(value).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function formatDate(value) {
    if (!value) return '-';
    const d = new Date(value);
    return d.toLocaleDateString('en-IN', {
        day: '2-digit',
        month: 'short',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function stockBadge(status, qty, minLevel) {
    let stat = status;
    if (!stat) {
        if (qty <= 0) stat = 'OUT OF STOCK';
        else if (qty <= minLevel) stat = 'LOW STOCK';
        else stat = 'IN STOCK';
    }

    if (stat === 'OUT OF STOCK') {
        return `<span class="badge-pill badge-out-of-stock"><i class="fas fa-times-circle"></i> Out of Stock</span>`;
    }
    if (stat === 'LOW STOCK') {
        return `<span class="badge-pill badge-low-stock"><i class="fas fa-exclamation-triangle"></i> Low Stock</span>`;
    }
    return `<span class="badge-pill badge-in-stock"><i class="fas fa-check-circle"></i> In Stock</span>`;
}

// Render Master Layout (Sidebar & Top Header)
function renderLayout(activePage, pageTitle = 'Dashboard', breadcrumb = 'Overview') {
    const user = getUser() || { username: 'User', role: 'USER', fullName: 'Staff User' };
    const isAdmin = (user.role || '').toUpperCase() === 'ADMIN';

    // Top Header
    const headerContainer = document.getElementById('layoutHeader');
    if (headerContainer) {
        headerContainer.innerHTML = `
            <header class="app-header">
                <div class="header-left">
                    <button class="sidebar-toggle-btn" id="sidebarToggle" onclick="toggleSidebar()">
                        <i class="fas fa-bars"></i>
                    </button>
                    <div>
                        <h1 class="header-page-title">${pageTitle}</h1>
                        <div class="header-breadcrumb"><i class="fas fa-home me-1"></i> Home / ${breadcrumb}</div>
                    </div>
                </div>
                <div class="header-right">
                    <span class="badge-pill ${isAdmin ? 'badge-role-admin' : 'badge-role-user'} d-none d-md-inline-flex">
                        <i class="fas ${isAdmin ? 'fa-shield-alt' : 'fa-user'}"></i> ${user.role || 'USER'}
                    </span>
                    <div class="dropdown">
                        <button class="btn btn-header-action dropdown-toggle" data-bs-toggle="dropdown">
                            <div class="user-avatar" style="width: 28px; height: 28px; font-size: 0.75rem;">
                                ${(user.fullName || user.username || 'U').charAt(0).toUpperCase()}
                            </div>
                            <span class="d-none d-sm-inline font-weight-600">${user.fullName || user.username}</span>
                        </button>
                        <ul class="dropdown-menu dropdown-menu-end shadow-sm border-0">
                            <li class="px-3 py-2 border-bottom">
                                <div class="font-weight-600">${user.fullName || user.username}</div>
                                <small class="text-muted">@${user.username} (${user.role})</small>
                            </li>
                            ${isAdmin ? '<li><a class="dropdown-item py-2" href="users.html"><i class="fas fa-users-cog me-2 text-primary"></i> User Management</a></li>' : ''}
                            <li><hr class="dropdown-divider my-1"></li>
                            <li><a class="dropdown-item py-2 text-danger" href="javascript:void(0)" onclick="logout()"><i class="fas fa-sign-out-alt me-2"></i> Logout</a></li>
                        </ul>
                    </div>
                </div>
            </header>
        `;
    }

    // Sidebar Navigation
    const sidebarContainer = document.getElementById('layoutSidebar');
    if (sidebarContainer) {
        let navItems = [];
        if (isAdmin) {
            navItems = [
                { header: 'Main' },
                { href: 'dashboard.html', icon: 'fa-chart-pie', label: 'Dashboard' },
                { href: 'pos.html', icon: 'fa-cash-register', label: 'POS Terminal' },
                { header: 'Inventory' },
                { href: 'products.html', icon: 'fa-box', label: 'Products' },
                { href: 'categories.html', icon: 'fa-tags', label: 'Categories' },
                { href: 'stock.html', icon: 'fa-warehouse', label: 'Stock Monitoring' },
                { header: 'Operations' },
                { href: 'customers.html', icon: 'fa-users', label: 'Customers' },
                { href: 'sales.html', icon: 'fa-shopping-cart', label: 'Sales History' },
                { href: 'purchases.html', icon: 'fa-truck-loading', label: 'Purchases' },
                { href: 'suppliers.html', icon: 'fa-handshake', label: 'Suppliers' },
                { header: 'Analytics & Admin' },
                { href: 'reports.html', icon: 'fa-file-invoice-dollar', label: 'Reports & Analytics' },
                { href: 'users.html', icon: 'fa-users-cog', label: 'User Management' }
            ];
        } else {
            navItems = [
                { header: 'Main' },
                { href: 'pos.html', icon: 'fa-cash-register', label: 'POS Terminal' },
                { header: 'Inventory' },
                { href: 'products.html', icon: 'fa-box', label: 'Products' },
                { href: 'stock.html', icon: 'fa-warehouse', label: 'Stock Monitoring' },
                { header: 'Operations' },
                { href: 'customers.html', icon: 'fa-users', label: 'Customers' },
                { href: 'sales.html', icon: 'fa-shopping-cart', label: 'Sales History' }
            ];
        }

        let navHtml = `
            <div class="sidebar-header">
                <a href="dashboard.html" class="sidebar-brand">
                    <div class="sidebar-brand-icon">
                        <i class="fas fa-boxes-stacked"></i>
                    </div>
                    <span>InventoryPro</span>
                </a>
            </div>
            <div class="sidebar-nav">
        `;

        navItems.forEach(item => {
            if (item.header) {
                navHtml += `<div class="nav-section-title">${item.header}</div>`;
            } else {
                const isActive = activePage === item.href ? 'active' : '';
                navHtml += `
                    <a href="${item.href}" class="nav-link-item ${isActive}">
                        <i class="fas ${item.icon}"></i>
                        <span>${item.label}</span>
                    </a>
                `;
            }
        });

        navHtml += `
            </div>
            <div class="sidebar-footer">
                <div class="user-compact">
                    <div class="user-avatar">
                        ${(user.fullName || user.username || 'U').charAt(0).toUpperCase()}
                    </div>
                    <div class="user-info">
                        <div class="user-name">${user.fullName || user.username}</div>
                        <span class="user-role-badge">${user.role || 'USER'}</span>
                    </div>
                </div>
                <button class="btn btn-sm btn-link text-danger p-0" title="Logout" onclick="logout()">
                    <i class="fas fa-sign-out-alt fa-lg"></i>
                </button>
            </div>
        `;

        sidebarContainer.innerHTML = navHtml;
        sidebarContainer.className = 'app-sidebar';

        // Backdrop for mobile
        let backdrop = document.getElementById('sidebarBackdrop');
        if (!backdrop) {
            backdrop = document.createElement('div');
            backdrop.id = 'sidebarBackdrop';
            backdrop.className = 'sidebar-backdrop';
            backdrop.onclick = toggleSidebar;
            document.body.appendChild(backdrop);
        }
    }
}

function toggleSidebar() {
    const sidebar = document.getElementById('layoutSidebar');
    const backdrop = document.getElementById('sidebarBackdrop');
    if (sidebar) sidebar.classList.toggle('show');
    if (backdrop) backdrop.classList.toggle('show');
}

// Select helper
async function populateSelect(selectId, path, valueField, labelField, placeholder) {
    const select = document.getElementById(selectId);
    if (!select) return;
    select.innerHTML = `<option value="">${placeholder}</option>`;
    try {
        const items = await apiRequest(path);
        if (Array.isArray(items)) {
            items.forEach(item => {
                const opt = document.createElement('option');
                opt.value = item[valueField];
                opt.textContent = item[labelField];
                select.appendChild(opt);
            });
        }
    } catch (e) {
        console.error('Failed to load ' + path, e);
    }
}

// Skeleton table rows
function showSkeleton(tbodyId, rowCount = 5, colCount = 6) {
    const tbody = document.getElementById(tbodyId);
    if (!tbody) return;
    let html = '';
    for (let i = 0; i < rowCount; i++) {
        html += '<tr>';
        for (let j = 0; j < colCount; j++) {
            html += `<td><div class="skeleton" style="height: 20px; width: ${Math.floor(Math.random() * 40 + 50)}%;"></div></td>`;
        }
        html += '</tr>';
    }
    tbody.innerHTML = html;
}
