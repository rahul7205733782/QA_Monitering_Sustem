// ===============================
// UTILITY FUNCTIONS
// ===============================

const API_BASE = window.location.origin + "/api";

// Toast Notification
function showToast(message, isError = false) {
    const toast = document.getElementById('toastMsg');
    const text = document.getElementById('toastText');

    if (!toast) {
        console.log('Toast:', message);
        return;
    }

    text.innerText = message;

    if (isError) {
        toast.classList.add('error');
        const icon = toast.querySelector('i');
        if (icon) icon.className = 'fas fa-exclamation-circle me-2';
    } else {
        toast.classList.remove('error');
        const icon = toast.querySelector('i');
        if (icon) icon.className = 'fas fa-check-circle me-2';
    }

    toast.style.display = 'block';
    clearTimeout(toast._timeout);

    toast._timeout = setTimeout(() => {
        toast.style.display = 'none';
    }, 5000);
}

// Format Date
function formatDate(dateString) {
    if (!dateString) return 'N/A';
    try {
        const date = new Date(dateString);
        return date.toLocaleString('en-US', {
            day: '2-digit',
            month: 'short',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            hour12: true
        });
    } catch (e) {
        return dateString;
    }
}

// Format File Size
function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

// Get Status Badge Class
function getStatusBadgeClass(status) {
    const statusMap = {
        'Open': 'bg-danger',
        'In Progress': 'bg-warning text-dark',
        'Resolved': 'bg-success',
        'Closed': 'bg-secondary',
        'Reopened': 'bg-info text-dark'
    };
    return statusMap[status] || 'bg-secondary';
}

// Get Priority Badge Class
function getPriorityBadgeClass(priority) {
    const priorityMap = {
        'Critical': 'bg-danger',
        'High': 'bg-warning text-dark',
        'Medium': 'bg-info text-dark',
        'Low': 'bg-success'
    };
    return priorityMap[priority] || 'bg-secondary';
}