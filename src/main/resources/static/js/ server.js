const express = require('express');
const mysql = require('mysql2/promise');
const cors = require('cors');
const dotenv = require('dotenv');
const axios = require('axios');
const cron = require('node-cron');

dotenv.config();

const app = express();
const PORT = process.env.PORT || 8080;

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Database Connection
let db;

async function connectDB() {
    try {
        db = await mysql.createConnection({
            host: process.env.DB_HOST,
            user: process.env.DB_USER,
            password: process.env.DB_PASSWORD,
            database: process.env.DB_NAME,
            waitForConnections: true,
            connectionLimit: 10,
            queueLimit: 0
        });
        console.log('✅ MySQL Connected successfully');
        
        // Create tables if they don't exist
        await initDatabase();
    } catch (error) {
        console.error('❌ Database connection failed:', error.message);
        process.exit(1);
    }
}

async function initDatabase() {
    try {
        // Check if dashboards table exists
        const [tables] = await db.query(`
            SELECT TABLE_NAME 
            FROM INFORMATION_SCHEMA.TABLES 
            WHERE TABLE_SCHEMA = ? AND TABLE_NAME = 'dashboards'
        `, [process.env.DB_NAME]);

        if (tables.length === 0) {
            console.log('📦 Creating database tables...');
            // Run the SQL schema
            const fs = require('fs');
            const sql = fs.readFileSync('./db/database.sql', 'utf8');
            const statements = sql.split(';').filter(stmt => stmt.trim());
            
            for (const statement of statements) {
                if (statement.trim()) {
                    await db.query(statement);
                }
            }
            console.log('✅ Database tables created successfully');
        }
    } catch (error) {
        console.error('❌ Error initializing database:', error.message);
    }
}

// =============================================
// API ENDPOINTS
// =============================================

// GET all dashboards
app.get('/api/dashboard-monitor', async (req, res) => {
    try {
        const [rows] = await db.query(`
            SELECT 
                id, client_name AS clientName, 
                dashboard_url AS dashboardUrl, 
                status, status_code AS statusCode,
                response_time AS responseTime, 
                uptime_percentage AS uptimePercentage,
                last_checked AS lastChecked,
                created_at AS createdAt,
                updated_at AS updatedAt
            FROM dashboards 
            ORDER BY id ASC
        `);
        
        // Format dates for frontend
        const formattedRows = rows.map(row => ({
            ...row,
            lastChecked: row.lastChecked ? formatDate(row.lastChecked) : null,
            createdAt: row.createdAt ? formatDate(row.createdAt) : null,
            updatedAt: row.updatedAt ? formatDate(row.updatedAt) : null
        }));
        
        res.json(formattedRows);
    } catch (error) {
        console.error('Error fetching dashboards:', error);
        res.status(500).json({ error: 'Failed to fetch dashboards' });
    }
});

// GET single dashboard
app.get('/api/dashboard-monitor/:id', async (req, res) => {
    try {
        const [rows] = await db.query(
            `SELECT 
                id, client_name AS clientName, 
                dashboard_url AS dashboardUrl, 
                status, status_code AS statusCode,
                response_time AS responseTime, 
                uptime_percentage AS uptimePercentage,
                last_checked AS lastChecked
            FROM dashboards 
            WHERE id = ?`,
            [req.params.id]
        );
        
        if (rows.length === 0) {
            return res.status(404).json({ error: 'Dashboard not found' });
        }
        
        const row = rows[0];
        row.lastChecked = row.lastChecked ? formatDate(row.lastChecked) : null;
        
        res.json(row);
    } catch (error) {
        console.error('Error fetching dashboard:', error);
        res.status(500).json({ error: 'Failed to fetch dashboard' });
    }
});

// POST - Add new dashboard
app.post('/api/dashboard-monitor', async (req, res) => {
    const { clientName, dashboardUrl } = req.body;
    
    if (!clientName || !dashboardUrl) {
        return res.status(400).json({ error: 'Client name and URL are required' });
    }
    
    try {
        const [result] = await db.query(
            `INSERT INTO dashboards 
            (client_name, dashboard_url, status, status_code, response_time, uptime_percentage, last_checked) 
            VALUES (?, ?, 'CHECKING', NULL, NULL, 100.00, NOW())`,
            [clientName, dashboardUrl]
        );
        
        // Immediately check the new dashboard
        const newId = result.insertId;
        await checkSingleDashboard(newId);
        
        const [rows] = await db.query(
            `SELECT 
                id, client_name AS clientName, 
                dashboard_url AS dashboardUrl, 
                status, status_code AS statusCode,
                response_time AS responseTime, 
                uptime_percentage AS uptimePercentage,
                last_checked AS lastChecked
            FROM dashboards 
            WHERE id = ?`,
            [newId]
        );
        
        res.status(201).json(rows[0]);
    } catch (error) {
        console.error('Error adding dashboard:', error);
        res.status(500).json({ error: 'Failed to add dashboard' });
    }
});

// POST - Check single dashboard
app.post('/api/dashboard-monitor/:id/check', async (req, res) => {
    try {
        const result = await checkSingleDashboard(req.params.id);
        res.json(result);
    } catch (error) {
        console.error('Error checking dashboard:', error);
        res.status(500).json({ error: 'Failed to check dashboard' });
    }
});

// POST - Check all dashboards
app.post('/api/dashboard-monitor/check-all', async (req, res) => {
    try {
        const results = await checkAllDashboards();
        res.json({ success: true, checked: results.length });
    } catch (error) {
        console.error('Error checking all dashboards:', error);
        res.status(500).json({ error: 'Failed to check all dashboards' });
    }
});

// DELETE - Remove dashboard
app.delete('/api/dashboard-monitor/:id', async (req, res) => {
    try {
        const [result] = await db.query(
            'DELETE FROM dashboards WHERE id = ?',
            [req.params.id]
        );
        
        if (result.affectedRows === 0) {
            return res.status(404).json({ error: 'Dashboard not found' });
        }
        
        res.json({ success: true, message: 'Dashboard deleted successfully' });
    } catch (error) {
        console.error('Error deleting dashboard:', error);
        res.status(500).json({ error: 'Failed to delete dashboard' });
    }
});

// GET - Dashboard statistics
app.get('/api/dashboard-monitor/stats/summary', async (req, res) => {
    try {
        const [rows] = await db.query(`
            SELECT 
                COUNT(*) as total,
                SUM(CASE WHEN status = 'UP' THEN 1 ELSE 0 END) as up,
                SUM(CASE WHEN status = 'DOWN' THEN 1 ELSE 0 END) as down,
                SUM(CASE WHEN status = 'CHECKING' THEN 1 ELSE 0 END) as checking,
                AVG(CASE WHEN response_time > 0 THEN response_time ELSE NULL END) as avgResponseTime,
                AVG(uptime_percentage) as avgUptime
            FROM dashboards
        `);
        
        res.json(rows[0]);
    } catch (error) {
        console.error('Error fetching stats:', error);
        res.status(500).json({ error: 'Failed to fetch statistics' });
    }
});

// GET - Monitoring logs for a dashboard
app.get('/api/dashboard-monitor/:id/logs', async (req, res) => {
    try {
        const [rows] = await db.query(
            `SELECT 
                id, dashboard_id AS dashboardId,
                status, status_code AS statusCode,
                response_time AS responseTime,
                checked_at AS checkedAt
            FROM monitoring_logs 
            WHERE dashboard_id = ? 
            ORDER BY checked_at DESC 
            LIMIT 20`,
            [req.params.id]
        );
        
        res.json(rows);
    } catch (error) {
        console.error('Error fetching logs:', error);
        res.status(500).json({ error: 'Failed to fetch logs' });
    }
});

// =============================================
// HELPER FUNCTIONS
// =============================================

async function checkSingleDashboard(id) {
    try {
        const [rows] = await db.query(
            'SELECT id, client_name, dashboard_url FROM dashboards WHERE id = ?',
            [id]
        );
        
        if (rows.length === 0) {
            throw new Error('Dashboard not found');
        }
        
        const dashboard = rows[0];
        const startTime = Date.now();
        let status = 'DOWN';
        let statusCode = null;
        let responseTime = 0;
        let uptime = 0;
        
        try {
            const response = await axios.get(dashboard.dashboard_url, {
                timeout: 5000,
                validateStatus: false,
                headers: {
                    'User-Agent': 'VoiceOC-Monitor/1.0'
                }
            });
            
            responseTime = Date.now() - startTime;
            statusCode = response.status;
            
            if (response.status >= 200 && response.status < 400) {
                status = 'UP';
                uptime = Math.min(100, Math.max(0, 100 - (responseTime / 2000) * 10));
            } else {
                status = 'DOWN';
                uptime = 0;
            }
        } catch (error) {
            responseTime = Date.now() - startTime;
            status = 'DOWN';
            uptime = 0;
            statusCode = error.code || 503;
        }
        
        // Get current uptime for calculation
        const [current] = await db.query(
            'SELECT uptime_percentage FROM dashboards WHERE id = ?',
            [id]
        );
        
        const currentUptime = current[0]?.uptime_percentage || 100;
        const newUptime = status === 'UP' 
            ? Math.min(100, currentUptime + 0.1) 
            : Math.max(0, currentUptime - 5);
        
        // Update dashboard
        await db.query(
            `UPDATE dashboards 
            SET status = ?, status_code = ?, response_time = ?, 
                uptime_percentage = ?, last_checked = NOW() 
            WHERE id = ?`,
            [status, statusCode, responseTime, parseFloat(newUptime.toFixed(2)), id]
        );
        
        // Log the check
        await db.query(
            `INSERT INTO monitoring_logs (dashboard_id, status, status_code, response_time) 
            VALUES (?, ?, ?, ?)`,
            [id, status, statusCode, responseTime]
        );
        
        return {
            id,
            status,
            statusCode,
            responseTime,
            uptime: parseFloat(newUptime.toFixed(2))
        };
    } catch (error) {
        console.error(`Error checking dashboard ${id}:`, error.message);
        throw error;
    }
}

async function checkAllDashboards() {
    try {
        const [rows] = await db.query('SELECT id FROM dashboards');
        const results = [];
        
        for (const row of rows) {
            try {
                const result = await checkSingleDashboard(row.id);
                results.push(result);
            } catch (error) {
                console.error(`Failed to check dashboard ${row.id}:`, error.message);
                results.push({ id: row.id, error: error.message });
            }
        }
        
        return results;
    } catch (error) {
        console.error('Error checking all dashboards:', error);
        throw error;
    }
}

function formatDate(date) {
    if (!date) return null;
    const d = new Date(date);
    return d.toLocaleString('en-US', {
        month: 'short',
        day: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        hour12: true
    });
}

// =============================================
// CRON JOB - Auto check every 5 minutes
// =============================================

cron.schedule('*/5 * * * *', async () => {
    console.log('🔄 Running scheduled health check...');
    try {
        await checkAllDashboards();
        console.log('✅ Scheduled health check completed');
    } catch (error) {
        console.error('❌ Scheduled health check failed:', error.message);
    }
});

// =============================================
// START SERVER
// =============================================

app.listen(PORT, async () => {
    console.log(`🚀 Server running on http://localhost:${PORT}`);
    await connectDB();
    console.log(`📊 Monitoring ${process.env.CHECK_INTERVAL/1000} dashboards`);
    console.log(`⏰ Auto-check every 5 minutes`);
});

// Graceful shutdown
process.on('SIGINT', async () => {
    console.log('\n🛑 Shutting down server...');
    if (db) await db.end();
    process.exit(0);
});