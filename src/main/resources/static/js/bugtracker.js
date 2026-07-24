// ===============================
// FILE: bugtracker.js
// ===============================

// ===============================
// Global Variables
// ===============================

let hospitals = [];
let uploadModal;
let hospitalModal;
let sheetsModal;
let currentHospitalId = null;
let sheetCounts = {};
let lastUploads = {};
const MAX_FILE_SIZE = 20 * 1024 * 1024; // 20 MB

// ===============================
// ALL HOSPITALS LIST (60+ Hospitals)
// ===============================

const ALL_HOSPITALS = [
    "Agilus Diagnostics",
    "Al Abeer Medical Company/(Group)",
    "AL Borg Diagnostics",
    "Al Sharq Healthcare",
    "AMH",
    "Bharain Specialist Hospital",
    "Cocoona Aesthetics",
    "Core Diagnostics",
    "DDRC Agilus",
    "Hadi Clinic",
    "Kims Health Bahrain",
    "KIMS KSA",
    "KIMS Oman",
    "Mahajan Imaging & Labs",
    "Mahajan Promo",
    "Max Account 1/Max@home",
    "Max Lab",
    "Meitra Hospital",
    "Pushpawati Singhania Hospital & Research Institute (PSRI)",
    "RBH",
    "Royale Hayat Hospital (RHH)",
    "Ujala Cygnus Hospital",
    "Vijaya Diagnostics",
    "Yashoda",
    "Evercare Hospital",
    "KIMS Qatar",
    "Rajagiri Hospital",
    "Al Kindi Hospital",
    "Marengo Asia",
    "Kaya Skin Clinic",
    "ISIC",
    "Sancheti Hospital",
    "Max Health Care (Daksh)",
    "Care Hospital",
    "Max International (Pramod)",
    "IVY Hospital",
    "Shri Ramakrishna Hospital",
    "Happiest Health",
    "Sparsh Hospital",
    "Narayan Memorial Hospital",
    "AINU",
    "RXDX",
    "Paras Healthcare",
    "Sharda Hospital",
    "Woodland Hospital",
    "Neuberg Diagnostic",
    "Sitaram Bhartiya",
    "Livasa Hospital",
    "Prime Health",
    "IBN",
    "Hiranandani Hospital",
    "Medanta",
    "Ankura",
    "Ankura Promo(marketing)",
    "Bank Hospital",
    "Diagnostx Hospital",
    "RKMS",
    "KIMS UAE",
    "gupshup URL",
    "Whats app demo"
];

// ===============================
// DOMContentLoaded
// ===============================

document.addEventListener("DOMContentLoaded", () => {

    console.log('🚀 Bug Tracker Initialized');

    // Initialize Modals
    uploadModal = new bootstrap.Modal(document.getElementById("uploadModal"));
    hospitalModal = new bootstrap.Modal(document.getElementById("hospitalModal"));
    sheetsModal = new bootstrap.Modal(document.getElementById("sheetsModal"));

    // Load initial data
    loadDashboard();

    // Register all event listeners
    registerEvents();
});

// ===============================
// Register Events
// ===============================

function registerEvents() {

    document.getElementById("btnRefresh").addEventListener("click", loadDashboard);

    document.getElementById("btnUpload").addEventListener("click", () => {
        loadHospitalsForDropdown();
        uploadModal.show();
    });

    document.getElementById("btnHospital").addEventListener("click", () => {
        document.getElementById("hospitalName").value = '';
        document.getElementById("hospitalAddress").value = '';
        document.getElementById("hospitalPhone").value = '';
        document.getElementById("hospitalEmail").value = '';
        hospitalModal.show();
    });

    document.getElementById("searchHospital").addEventListener("keyup", searchHospital);

    document.getElementById("btnSaveHospital").addEventListener("click", saveHospital);

    document.getElementById("btnUploadExcel").addEventListener("click", uploadExcel);

    document.getElementById("btnLoadAllHospitals").addEventListener("click", loadAllHospitals);

    document.getElementById("uploadHospital").addEventListener("change", validateUploadForm);
    document.getElementById("uploadFile").addEventListener("change", validateUploadForm);

    document.querySelectorAll('[data-bs-dismiss="modal"]').forEach(btn => {
        btn.addEventListener('click', () => {
            if ($.fn.select2 && $('#uploadHospital').hasClass("select2-hidden-accessible")) {
                $('#uploadHospital').select2('destroy');
            }
        });
    });

    document.getElementById('menuToggle')?.addEventListener('click', () => {
        document.querySelector('.app-sidebar')?.classList.toggle('open');
    });

    document.getElementById('logoutBtn')?.addEventListener('click', () => {
        localStorage.removeItem('voiceoc_logged_in');
        localStorage.removeItem('voiceoc_user');
        showToast('Logged out');
        setTimeout(() => { window.location.href = 'index.html'; }, 500);
    });
}

// ===============================
// Validate Upload Form
// ===============================

function validateUploadForm() {
    const hospitalId = document.getElementById("uploadHospital").value;
    const fileInput = document.getElementById("uploadFile");
    const uploadBtn = document.getElementById('btnUploadExcel');
    
    if (hospitalId && fileInput.files.length > 0) {
        uploadBtn.disabled = false;
    } else {
        uploadBtn.disabled = true;
    }
}

// ===============================
// Load Dashboard
// ===============================

async function loadDashboard() {

    try {
        console.log('🔄 Loading dashboard...');

        const response = await fetch(`${API_BASE}/hospitals`);

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Failed to fetch hospitals');
        }

        hospitals = await response.json();
        console.log('✅ Hospitals loaded:', hospitals.length);

        document.getElementById('totalHospitals').innerText = hospitals.length;

        let totalSheets = 0;
        let lastUploadDate = null;
        let lastUploadHospital = null;
        let todayUploads = 0;
        const today = new Date().toDateString();
        
        sheetCounts = {};
        lastUploads = {};
        
        for (const hospital of hospitals) {
            try {
                const sheetResponse = await fetch(`${API_BASE}/bugsheets/hospital/${hospital.id}`);
                if (sheetResponse.ok) {
                    const sheets = await sheetResponse.json();
                    const count = sheets.length;
                    totalSheets += count;
                    sheetCounts[hospital.id] = count;
                    
                    const todaySheets = sheets.filter(s => {
                        const d = new Date(s.uploadDate);
                        return d.toDateString() === today;
                    });
                    todayUploads += todaySheets.length;
                    
                    if (sheets.length > 0) {
                        const latest = sheets.reduce((a, b) => 
                            new Date(a.uploadDate) > new Date(b.uploadDate) ? a : b
                        );
                        lastUploads[hospital.id] = {
                            uploadedBy: latest.uploadedBy || 'Anonymous',
                            uploadDate: latest.uploadDate,
                            sheetName: latest.sheetName || 'Default'
                        };
                        
                        if (!lastUploadDate || new Date(latest.uploadDate) > new Date(lastUploadDate)) {
                            lastUploadDate = latest.uploadDate;
                            lastUploadHospital = hospital.name;
                        }
                    }
                }
            } catch (e) {
                console.warn('Could not fetch sheets for hospital:', hospital.id);
            }
        }
        
        document.getElementById('totalSheets').innerText = totalSheets;
        document.getElementById('todayUploads').innerText = todayUploads;
        
        const lastUploadEl = document.getElementById('lastUpload');
        if (lastUploadDate) {
            lastUploadEl.innerHTML = `${formatDate(lastUploadDate)}<br><small class="text-muted">${lastUploadHospital || ''}</small>`;
        } else {
            lastUploadEl.innerText = '--';
        }

        loadHospitalCards(hospitals);
        updateLastUpdated();

    } catch (error) {
        console.error('❌ Error loading dashboard:', error);
        showToast('Failed to load hospitals: ' + error.message, true);
    }
}

// ===============================
// Load Hospital Cards
// ===============================

function loadHospitalCards(list) {

    const grid = document.getElementById("hospitalGrid");

    if (!grid) {
        console.error('❌ hospitalGrid element not found!');
        return;
    }

    if (list.length === 0) {
        grid.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-hospital"></i>
                <h5>No Hospitals Found</h5>
                <p>Click "Add Hospital" to add your first hospital</p>
            </div>
        `;
        return;
    }

    let html = '';

    list.forEach((hospital, index) => {
        const sheetCount = sheetCounts[hospital.id] || 0;
        const hasSheets = sheetCount > 0;
        const lastUpload = lastUploads[hospital.id];
        
        html += `
            <div class="hospital-card fade-in" style="animation-delay: ${index * 0.05}s">
                <div class="card-header">
                    <h4 class="card-title">
                        <span class="hospital-icon"><i class="fas fa-hospital"></i></span>
                        ${hospital.name}
                    </h4>
                    <span class="card-id">ID: ${hospital.id}</span>
                </div>

                <div class="card-body">
                    <div class="info-grid">
                        <div class="info-item">
                            <i class="fas fa-map-marker-alt"></i>
                            <span class="label">Address:</span>
                            <span class="value">${hospital.address || 'N/A'}</span>
                        </div>
                        ${hospital.phone ? `
                        <div class="info-item">
                            <i class="fas fa-phone"></i>
                            <span class="label">Phone:</span>
                            <span class="value">${hospital.phone}</span>
                        </div>
                        ` : ''}
                        ${hospital.email ? `
                        <div class="info-item">
                            <i class="fas fa-envelope"></i>
                            <span class="label">Email:</span>
                            <span class="value">${hospital.email}</span>
                        </div>
                        ` : ''}
                    </div>

                    <div class="stats-row">
                        <div class="stat-item">
                            <span class="stat-number ${hasSheets ? 'has-sheets' : 'no-sheets'}">${sheetCount}</span>
                            <span class="stat-label">Uploaded Sheets</span>
                        </div>
                        <div class="stat-item">
                            <span class="stat-number">${lastUpload ? 1 : 0}</span>
                            <span class="stat-label">Last Upload</span>
                        </div>
                    </div>

                    <div class="last-upload-info">
                        <i class="fas fa-clock"></i>
                        ${lastUpload ? `
                            <span class="uploader-name">${lastUpload.uploadedBy}</span>
                            <span class="upload-date">• ${formatDate(lastUpload.uploadDate)}</span>
                        ` : `
                            <span class="upload-date">No uploads yet</span>
                        `}
                    </div>
                </div>

                <div class="card-footer">
                    <button class="btn btn-upload" onclick="openUpload(${hospital.id})">
                        <i class="fas fa-upload"></i> Upload
                    </button>
                    <button class="btn btn-sheets ${hasSheets ? 'has-sheets' : ''}" onclick="viewSheets(${hospital.id})">
                        <i class="fas fa-file-excel"></i> ${hasSheets ? 'View Sheets' : 'No Sheets'}
                    </button>
                    <button class="btn btn-delete" onclick="deleteHospitalConfirm(${hospital.id})" title="Delete Hospital">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </div>
        `;
    });

    grid.innerHTML = html;
    console.log('✅ Hospital cards rendered:', list.length);
}

// ===============================
// Search Hospital
// ===============================

function searchHospital() {

    const keyword = document.getElementById("searchHospital").value.toLowerCase().trim();

    if (keyword === '') {
        loadHospitalCards(hospitals);
        return;
    }

    const filtered = hospitals.filter(h =>
        h.name.toLowerCase().includes(keyword)
    );

    loadHospitalCards(filtered);
}

// ===============================
// Load All Hospitals to Database
// ===============================

async function loadAllHospitals() {
    
    const btn = document.getElementById('btnLoadAllHospitals');
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status"></span> Loading...';
    
    let added = 0;
    let failed = 0;
    let skipped = 0;

    showToast('⏳ Loading all hospitals...');

    for (const hospitalName of ALL_HOSPITALS) {
        try {
            const checkResponse = await fetch(`${API_BASE}/hospitals/search?name=${encodeURIComponent(hospitalName)}`);
            
            if (checkResponse.ok) {
                skipped++;
                continue;
            }

            const hospitalData = {
                name: hospitalName,
                address: 'To be updated',
                phone: null,
                email: null
            };

            const response = await fetch(`${API_BASE}/hospitals`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(hospitalData)
            });

            if (response.ok) {
                added++;
                console.log(`✅ Added: ${hospitalName}`);
            } else {
                failed++;
                console.log(`❌ Failed: ${hospitalName}`);
            }
        } catch (error) {
            failed++;
            console.error(`❌ Error adding ${hospitalName}:`, error);
        }
    }

    btn.disabled = false;
    btn.innerHTML = '<i class="fas fa-database me-1"></i> Load All';

    showToast(`✅ Added ${added} hospitals, ${skipped} already existed, ${failed} failed`);
    await loadDashboard();
}

// ===============================
// Open Upload Modal
// ===============================

async function openUpload(hospitalId) {

    document.getElementById("uploadSheetName").value = "";
    document.getElementById("uploadedBy").value = "";
    document.getElementById("uploadFile").value = "";
    document.getElementById("uploadHospital").value = "";
    
    await loadHospitalsForDropdown();

    if (uploadModal) {
        uploadModal.show();

        document.getElementById('btnUploadExcel').disabled = true;

        document.getElementById('uploadProgress').style.display = 'none';
        const progressBar = document.querySelector('#uploadProgress .progress-bar');
        if (progressBar) {
            progressBar.style.width = '0%';
            progressBar.textContent = '0%';
            progressBar.className = 'progress-bar progress-bar-striped progress-bar-animated';
        }

        setTimeout(() => {
            if ($.fn.select2) {
                $('#uploadHospital').select2({
                    dropdownParent: $('#uploadModal'),
                    width: '100%',
                    placeholder: '🔍 Search Hospital...',
                    allowClear: true
                });
            }

            if (hospitalId) {
                $('#uploadHospital').val(hospitalId).trigger('change');
            }
            
            validateUploadForm();
        }, 200);
    }
}

// ===============================
// Load Hospitals for Dropdown
// ===============================

async function loadHospitalsForDropdown() {

    try {
        const response = await fetch(`${API_BASE}/hospitals`);

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || "Unable to load hospitals");
        }

        const hospitals = await response.json();

        const select = document.getElementById("uploadHospital");

        select.innerHTML = '<option value="">-- Select Hospital --</option>';

        hospitals.forEach(hospital => {
            select.innerHTML += `
                <option value="${hospital.id}">
                    ${hospital.name}
                </option>
            `;
        });

        if ($.fn.select2 && $('#uploadHospital').hasClass("select2-hidden-accessible")) {
            $('#uploadHospital').trigger("change");
        }

    } catch (e) {
        console.error(e);
        showToast("Unable to load hospitals", true);
    }
}

// ===============================
// Upload Excel
// ===============================

async function uploadExcel() {

    const uploadBtn = document.getElementById('btnUploadExcel');
    
    if (uploadBtn.disabled) {
        return;
    }

    const hospitalId = document.getElementById("uploadHospital").value;
    const sheetName = document.getElementById("uploadSheetName").value.trim();
    const uploadedBy = document.getElementById("uploadedBy").value.trim() || "Anonymous";
    const fileInput = document.getElementById("uploadFile");
    const file = fileInput.files[0];

    if (!hospitalId) {
        showToast("Please select a hospital", true);
        return;
    }

    if (!file) {
        showToast("Please select a file", true);
        return;
    }

    const allowedExtensions = [".xlsx", ".xls"];
    const fileName = file.name.toLowerCase();
    const validExtension = allowedExtensions.some(ext => fileName.endsWith(ext));

    if (!validExtension) {
        showToast("Only Excel (.xls, .xlsx) files are allowed.", true);
        return;
    }

    if (file.size > MAX_FILE_SIZE) {
        showToast(`File size must be less than ${MAX_FILE_SIZE / (1024 * 1024)} MB.`, true);
        return;
    }

    const hospitalName = document.getElementById("uploadHospital").selectedOptions[0].text;

    uploadBtn.disabled = true;
    uploadBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status"></span> Uploading...';
    
    document.getElementById('uploadProgress').style.display = 'block';
    const progressBar = document.querySelector('#uploadProgress .progress-bar');
    if (progressBar) {
        progressBar.style.width = '60%';
        progressBar.textContent = 'Uploading...';
    }

    const formData = new FormData();
    formData.append('file', file);
    formData.append('hospitalId', hospitalId);
    formData.append('sheetName', sheetName || 'Default');
    formData.append('uploadedBy', uploadedBy);

    try {
        console.log('📤 Uploading with hospitalId:', hospitalId);

        const response = await fetch(`${API_BASE}/bugsheets/upload`, {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Upload failed');
        }

        const result = await response.json();
        
        if (progressBar) {
            progressBar.style.width = '100%';
            progressBar.textContent = 'Complete!';
        }
        
        showToast(`✅ ${file.name} uploaded successfully for ${hospitalName}.`);

        document.getElementById('uploadSheetName').value = '';
        document.getElementById('uploadedBy').value = '';
        document.getElementById('uploadFile').value = '';
        document.getElementById('uploadProgress').style.display = 'none';
        if (progressBar) {
            progressBar.style.width = '0%';
            progressBar.textContent = '0%';
        }

        document.getElementById("uploadHospital").value = "";
        validateUploadForm();

        if ($.fn.select2 && $('#uploadHospital').hasClass("select2-hidden-accessible")) {
            $('#uploadHospital').select2('destroy');
        }

        uploadModal.hide();
        await loadDashboard();

        if (currentHospitalId !== null) {
            await viewSheets(currentHospitalId);
        }

    } catch (error) {
        console.error('❌ Upload error:', error);
        showToast('Upload failed: ' + error.message, true);
        
        if (progressBar) {
            progressBar.style.width = '100%';
            progressBar.className = 'progress-bar bg-danger';
            progressBar.textContent = 'Upload Failed';
        }
    } finally {
        uploadBtn.disabled = false;
        uploadBtn.innerHTML = '<i class="fas fa-upload me-1"></i> Upload';
        setTimeout(() => {
            document.getElementById('uploadProgress').style.display = 'none';
            if (progressBar) {
                progressBar.className = 'progress-bar progress-bar-striped progress-bar-animated';
                progressBar.style.width = '0%';
                progressBar.textContent = '0%';
            }
        }, 2000);
    }
}

// ===============================
// Save Hospital
// ===============================

async function saveHospital() {

    const name = document.getElementById('hospitalName').value.trim();
    const address = document.getElementById('hospitalAddress').value.trim();
    const phone = document.getElementById('hospitalPhone').value.trim();
    const email = document.getElementById('hospitalEmail').value.trim();

    if (!name) {
        showToast('Please enter hospital name', true);
        return;
    }

    if (!address) {
        showToast('Please enter hospital address', true);
        return;
    }

    if (phone && !/^[0-9]{10}$/.test(phone)) {
        showToast('Phone number must be exactly 10 digits.', true);
        return;
    }

    if (email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        showToast('Please enter a valid email address.', true);
        return;
    }

    const hospitalData = {
        name: name,
        address: address,
        phone: phone || null,
        email: email || null
    };

    try {
        showToast('⏳ Adding hospital...');

        const response = await fetch(`${API_BASE}/hospitals`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(hospitalData)
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Failed to add hospital');
        }

        const result = await response.json();
        showToast(`✅ Hospital "${result.name}" added successfully! (ID: ${result.id})`);

        document.getElementById('hospitalName').value = '';
        document.getElementById('hospitalAddress').value = '';
        document.getElementById('hospitalPhone').value = '';
        document.getElementById('hospitalEmail').value = '';

        hospitalModal.hide();
        await loadDashboard();
        await loadHospitalsForDropdown();

    } catch (error) {
        console.error('❌ Error adding hospital:', error);
        showToast('Failed to add hospital: ' + error.message, true);
    }
}

// ===============================
// Delete Hospital - Confirm
// ===============================

function deleteHospitalConfirm(hospitalId) {

    const hospital = hospitals.find(h => h.id === hospitalId);
    const sheetCount = sheetCounts[hospitalId] || 0;

    if (!hospital) return;

    const message = `
⚠️ Delete Hospital?

This will permanently delete:

• Hospital: ${hospital.name}
• ${sheetCount} uploaded Excel sheet(s)
• All associated metadata

This action cannot be undone.

Are you sure you want to continue?
    `.trim();

    if (!confirm(message)) {
        return;
    }

    deleteHospital(hospitalId);
}

// ===============================
// Delete Hospital
// ===============================

async function deleteHospital(hospitalId) {

    try {
        const response = await fetch(`${API_BASE}/hospitals/${hospitalId}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Failed to delete hospital');
        }

        showToast('✅ Hospital deleted successfully');
        await loadDashboard();
        await loadHospitalsForDropdown();

    } catch (error) {
        console.error('❌ Error deleting hospital:', error);
        showToast('Failed to delete hospital: ' + error.message, true);
    }
}

// ===============================
// View Sheets
// ===============================

async function viewSheets(hospitalId) {

    try {
        console.log('📋 Loading sheets for hospital ID:', hospitalId);
        currentHospitalId = hospitalId;

        const response = await fetch(`${API_BASE}/bugsheets/hospital/${hospitalId}`);

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || "Unable to load sheets");
        }

        const sheets = await response.json();

        console.log('✅ Sheets loaded:', sheets.length);

        const container = document.getElementById('sheetsListContainer');
        const hospital = hospitals.find(h => h.id === hospitalId);
        document.getElementById('sheetsModalTitle').textContent = `📋 Uploaded Sheets - ${hospital?.name || 'Hospital'}`;

        if (sheets.length === 0) {
            container.innerHTML = `
                <tr>
                    <td colspan="6" class="text-center py-4">
                        <i class="fas fa-file-excel fa-2x text-muted mb-2 d-block"></i>
                        <h6 class="text-muted">No Sheets Uploaded</h6>
                        <p class="text-muted small">Upload your first Excel sheet for this hospital</p>
                        <button class="btn btn-primary btn-sm" onclick="openUpload(${hospitalId})">
                            <i class="fas fa-upload me-1"></i> Upload Sheet
                        </button>
                    </td>
                </tr>
            `;
        } else {
            let html = '';
            sheets.forEach((sheet, index) => {
                const originalFileName = sheet.originalFileName || sheet.fileName || '';
                html += `
                    <tr>
                        <td>${index + 1}</td>
                        <td>
                            <strong><i class="fas fa-file-excel text-success me-1"></i> ${sheet.sheetName || 'Default'}</strong>
                        </td>
                        <td><small class="text-muted">${originalFileName}</small></td>
                        <td>${sheet.uploadedBy || 'Anonymous'}</td>
                        <td>${formatDate(sheet.uploadDate)}</td>
                        <td>
                            <div class="btn-group-actions">
                                <button class="btn btn-sm btn-info" onclick="viewSheet(${sheet.id})" title="View Excel">
                                    <i class="fas fa-eye"></i>
                                </button>
                                <button class="btn btn-sm btn-primary" onclick="downloadSheet(${sheet.id})" title="Download">
                                    <i class="fas fa-download"></i>
                                </button>
                                <button class="btn btn-sm btn-danger" onclick="deleteSheetConfirm(${sheet.id})" title="Delete">
                                    <i class="fas fa-trash"></i>
                                </button>
                            </div>
                        </td>
                    </tr>
                `;
            });

            container.innerHTML = html;
        }

        sheetsModal.show();

    } catch (e) {
        console.error(e);
        showToast("Unable to load sheets", true);
    }
}

// ===============================
// View Sheet
// ===============================

async function viewSheet(sheetId) {
    const url = `${API_BASE}/bugsheets/view/${sheetId}`;
    window.open(url, "_blank");
    showToast('📄 Opening Excel sheet...');
}

// ===============================
// Download Sheet
// ===============================

async function downloadSheet(sheetId) {

    try {
        console.log('📥 Downloading sheet ID:', sheetId);

        const response = await fetch(`${API_BASE}/bugsheets/download/${sheetId}`);

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Failed to download sheet');
        }

        const blob = await response.blob();
        const contentDisposition = response.headers.get('content-disposition');
        let filename = "BugTracker.xlsx";

        if (contentDisposition) {
            const match = contentDisposition.match(/filename="(.+)"/);
            if (match) filename = match[1];
        }

        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);

        showToast('✅ File downloaded successfully');

    } catch (error) {
        console.error('❌ Error downloading sheet:', error);
        showToast('Failed to download sheet: ' + error.message, true);
    }
}

// ===============================
// Delete Sheet - Confirm
// ===============================

function deleteSheetConfirm(sheetId) {

    if (!confirm('⚠️ Are you sure you want to delete this uploaded Excel sheet?')) {
        return;
    }

    deleteSheet(sheetId);
}

// ===============================
// Delete Sheet
// ===============================

async function deleteSheet(sheetId) {

    try {
        console.log('🗑️ Deleting sheet ID:', sheetId);

        const response = await fetch(`${API_BASE}/bugsheets/${sheetId}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Failed to delete sheet');
        }

        showToast('✅ Sheet deleted successfully');

        if (currentHospitalId !== null) {
            const checkResponse = await fetch(`${API_BASE}/bugsheets/hospital/${currentHospitalId}`);
            if (checkResponse.ok) {
                const sheets = await checkResponse.json();
                if (sheets.length === 0) {
                    sheetsModal.hide();
                } else {
                    await viewSheets(currentHospitalId);
                }
            }
        }

        await loadDashboard();

    } catch (error) {
        console.error('❌ Error deleting sheet:', error);
        showToast('Failed to delete sheet: ' + error.message, true);
    }
}

// ===============================
// Update Last Updated Time
// ===============================

function updateLastUpdated() {
    const now = new Date();
    const time = now.toLocaleTimeString('en-US', {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    });
    const element = document.getElementById('lastUpdated');
    if (element) {
        element.textContent = `Updated: ${time}`;
    }
}

// ===============================
// Toast Notification
// ===============================

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
        toast.querySelector('i').className = 'fas fa-exclamation-circle me-2';
    } else {
        toast.classList.remove('error');
        toast.querySelector('i').className = 'fas fa-check-circle me-2';
    }

    toast.style.display = 'block';
    clearTimeout(toast._timeout);

    toast._timeout = setTimeout(() => {
        toast.style.display = 'none';
    }, 5000);
}