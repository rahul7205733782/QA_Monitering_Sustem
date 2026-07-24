// =====================================
// SESSION MANAGER - Auto Logout for ALL Pages
// =====================================

let inactivityTimer;
let warningTimer;
let countdownInterval;
const INACTIVITY_TIMEOUT = 15 * 60 * 1000; // 15 minutes
const WARNING_TIMEOUT = 14 * 60 * 1000; // Warning at 14 minutes (1 minute before logout)
let isInactivityLogoutEnabled = true;
let warningShown = false;
let remainingSeconds = 0;

// Toast function (if not available globally)
function showToast(message, type = 'info') {
    const toastText = document.getElementById('toastText');
    const toast = document.getElementById('toastMsg');
    if (toastText) toastText.innerText = message;
    if (toast) {
        toast.className = 'toast-msg';
        if (type === 'error') toast.classList.add('error');
        else if (type === 'success') toast.classList.add('success');
        toast.style.display = 'block';
        clearTimeout(toast._timeout);
        toast._timeout = setTimeout(() => {
            toast.style.display = 'none';
        }, 4000);
    }
}

// Reset the inactivity timer
function resetInactivityTimer() {
    clearTimeout(inactivityTimer);
    clearTimeout(warningTimer);
    clearInterval(countdownInterval);
    warningShown = false;
    
    // Remove existing warning modal if present
    const existingModal = document.getElementById('sessionWarningModal');
    if (existingModal) {
        try {
            const modal = bootstrap.Modal.getInstance(existingModal);
            if (modal) modal.dispose();
        } catch(e) {}
        existingModal.remove();
    }
    
    if (isInactivityLogoutEnabled) {
        // Show warning before logout
        warningTimer = setTimeout(showSessionWarning, WARNING_TIMEOUT);
        // Auto logout after timeout
        inactivityTimer = setTimeout(autoLogout, INACTIVITY_TIMEOUT);
    }
}

// Show session warning with countdown
function showSessionWarning() {
    if (!isInactivityLogoutEnabled || warningShown) return;
    warningShown = true;
    
    // Calculate remaining time
    remainingSeconds = Math.ceil((INACTIVITY_TIMEOUT - WARNING_TIMEOUT) / 1000);
    
    // Check if Bootstrap is loaded
    if (typeof bootstrap === 'undefined') {
        console.warn('Bootstrap not loaded, using simple alert');
        if (confirm('Your session will expire in ' + remainingSeconds + ' seconds. Click OK to stay logged in.')) {
            extendSession();
        }
        return;
    }
    
    // Create warning modal
    const warningHTML = `
        <div class="modal fade" id="sessionWarningModal" tabindex="-1" data-bs-backdrop="static" data-bs-keyboard="false">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content" style="border: 3px solid #F59E0B;">
                    <div class="modal-header" style="background: #FEF3C7; border-bottom: 2px solid #F59E0B;">
                        <h5 class="modal-title" style="color: #92400E;">
                            <i class="fas fa-clock me-2" style="color: #F59E0B;"></i>
                            Session Expiring Soon!
                        </h5>
                    </div>
                    <div class="modal-body text-center py-4">
                        <div style="font-size: 48px; margin-bottom: 15px;">
                            <i class="fas fa-hourglass-half" style="color: #F59E0B;"></i>
                        </div>
                        <h5 class="mb-3">Your session will expire in:</h5>
                        <div id="countdownDisplay" style="font-size: 72px; font-weight: 800; color: #DC2626; font-family: 'Inter', sans-serif;">
                            ${remainingSeconds}s
                        </div>
                        <p class="text-muted mt-3 mb-0">Please move your mouse or press any key to stay logged in.</p>
                    </div>
                    <div class="modal-footer" style="border-top: 2px solid #F1F5F9;">
                        <button class="btn btn-warning" onclick="extendSession()" style="font-weight: 600; padding: 10px 30px;">
                            <i class="fas fa-sync-alt me-2"></i> Stay Logged In
                        </button>
                    </div>
                </div>
            </div>
        </div>
    `;
    
    // Append modal to body
    const modalContainer = document.createElement('div');
    modalContainer.innerHTML = warningHTML;
    document.body.appendChild(modalContainer.firstElementChild);
    
    // Show modal
    const modalElement = document.getElementById('sessionWarningModal');
    try {
        const modal = new bootstrap.Modal(modalElement, {
            backdrop: 'static',
            keyboard: false
        });
        modal.show();
    } catch(e) {
        console.error('Error showing modal:', e);
    }
    
    // Start countdown
    startCountdown();
}

// Start countdown timer
function startCountdown() {
    clearInterval(countdownInterval);
    countdownInterval = setInterval(() => {
        remainingSeconds--;
        const display = document.getElementById('countdownDisplay');
        if (display) {
            display.textContent = remainingSeconds + 's';
            if (remainingSeconds <= 2) {
                display.style.color = '#DC2626';
                display.style.animation = 'pulse 0.5s ease-in-out infinite';
            }
        }
        if (remainingSeconds <= 0) {
            clearInterval(countdownInterval);
        }
    }, 1000);
}

// Extend session (reset timer)
function extendSession() {
    // Close modal
    const modalElement = document.getElementById('sessionWarningModal');
    if (modalElement) {
        try {
            const modal = bootstrap.Modal.getInstance(modalElement);
            if (modal) modal.dispose();
        } catch(e) {}
        modalElement.remove();
    }
    clearInterval(countdownInterval);
    warningShown = false;
    
    if (typeof showToast === 'function') {
        showToast('Session extended!', 'success');
    } else {
        console.log('Session extended!');
    }
    resetInactivityTimer();
}

// Auto logout function
function autoLogout() {
    if (!isInactivityLogoutEnabled) return;
    
    // Close any open modals
    const modalElement = document.getElementById('sessionWarningModal');
    if (modalElement) {
        try {
            const modal = bootstrap.Modal.getInstance(modalElement);
            if (modal) modal.dispose();
        } catch(e) {}
        modalElement.remove();
    }
    clearInterval(countdownInterval);
    
    if (localStorage.getItem('voiceoc_logged_in') === 'true') {
        if (typeof showToast === 'function') {
            showToast('Session expired due to inactivity. Logging out...', 'error');
        }
        setTimeout(() => {
            localStorage.removeItem('voiceoc_logged_in');
            localStorage.removeItem('voiceoc_user');
            window.location.href = 'index.html';
        }, 2000);
    }
}

// Track user activity
function trackUserActivity() {
    const events = ['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart', 'click'];
    events.forEach(event => {
        document.removeEventListener(event, resetInactivityTimer);
        document.addEventListener(event, resetInactivityTimer);
    });
}

// Initialize auto-logout
function initAutoLogout() {
    try {
        const settings = JSON.parse(localStorage.getItem('voiceoc_settings') || '{}');
        isInactivityLogoutEnabled = settings.sessionTimeout !== undefined ? settings.sessionTimeout : true;
        
        if (isInactivityLogoutEnabled) {
            trackUserActivity();
            resetInactivityTimer();
            console.log('Auto-logout initialized (10s for testing)');
        } else {
            console.log('Auto-logout disabled');
        }
    } catch(e) {
        console.error('Error initializing auto-logout:', e);
    }
}

// Make functions globally available
window.resetInactivityTimer = resetInactivityTimer;
window.extendSession = extendSession;
window.initAutoLogout = initAutoLogout;

// Auto-initialize when DOM is ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initAutoLogout);
} else {
    initAutoLogout();
}