// ===============================
// API MODULE - All API Calls
// ===============================

const API = {

    // Hospitals
    hospitals: {
        getAll: async () => {
            const response = await fetch(`${API_BASE}/hospitals`);
            if (!response.ok) throw new Error('Failed to fetch hospitals');
            return response.json();
        },
        getById: async (id) => {
            const response = await fetch(`${API_BASE}/hospitals/${id}`);
            if (!response.ok) throw new Error('Hospital not found');
            return response.json();
        },
        getByName: async (name) => {
            const response = await fetch(`${API_BASE}/hospitals/search?name=${encodeURIComponent(name)}`);
            if (!response.ok) throw new Error('Hospital not found');
            return response.json();
        },
        create: async (data) => {
            const response = await fetch(`${API_BASE}/hospitals`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });
            if (!response.ok) {
                const error = await response.text();
                throw new Error(error || 'Failed to create hospital');
            }
            return response.json();
        },
        delete: async (id) => {
            const response = await fetch(`${API_BASE}/hospitals/${id}`, {
                method: 'DELETE'
            });
            if (!response.ok) {
                const error = await response.text();
                throw new Error(error || 'Failed to delete hospital');
            }
            return true;
        }
    },

    // Bug Sheets
    bugsheets: {
        getByHospital: async (hospitalId) => {
            const response = await fetch(`${API_BASE}/bugsheets/hospital/${hospitalId}`);
            if (!response.ok) throw new Error('Failed to fetch sheets');
            return response.json();
        },
        getById: async (id) => {
            const response = await fetch(`${API_BASE}/bugsheets/${id}`);
            if (!response.ok) throw new Error('Sheet not found');
            return response.json();
        },
        upload: async (formData) => {
            const response = await fetch(`${API_BASE}/bugsheets/upload`, {
                method: 'POST',
                body: formData
            });
            if (!response.ok) {
                const error = await response.text();
                throw new Error(error || 'Upload failed');
            }
            return response.json();
        },
        delete: async (id) => {
            const response = await fetch(`${API_BASE}/bugsheets/${id}`, {
                method: 'DELETE'
            });
            if (!response.ok) {
                const error = await response.text();
                throw new Error(error || 'Failed to delete sheet');
            }
            return true;
        },
        download: async (id) => {
            const response = await fetch(`${API_BASE}/bugsheets/download/${id}`);
            if (!response.ok) throw new Error('Failed to download sheet');
            return response.blob();
        },
        view: async (id) => {
            window.open(`${API_BASE}/bugsheets/view/${id}`, '_blank');
        },
        getStats: async (hospitalId) => {
            const response = await fetch(`${API_BASE}/bugsheets/stats/hospital/${hospitalId}`);
            if (!response.ok) throw new Error('Failed to fetch stats');
            return response.json();
        }
    },

    // Dashboard
    dashboard: {
        getStats: async () => {
            const response = await fetch(`${API_BASE}/bugsheets/dashboard/stats`);
            if (!response.ok) throw new Error('Failed to fetch dashboard stats');
            return response.json();
        }
    }
};