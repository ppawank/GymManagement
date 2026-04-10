import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { excelService } from '../services/excelService';
import { authService } from '../services/authService';
import './ExcelManagement.css';

function ExcelManagement() {
    const [selectedFile, setSelectedFile] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [importResult, setImportResult] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        // Security check - only admin can access
        if (!authService.isAdmin()) {
            navigate('/dashboard');
            return;
        }
    }, [navigate]);

    const handleFileSelect = (e) => {
        const file = e.target.files[0];
        setSelectedFile(file);
        setError('');
        setSuccess('');
        setImportResult(null);
    };

    const handleExport = async () => {
        console.log('Export button clicked');
        setError('');
        setSuccess('');
        setLoading(true);

        try {
            console.log('Calling excelService.exportMembers()');
            await excelService.exportMembers();
            console.log('Export successful');
            setSuccess('Members data exported successfully!');
        } catch (err) {
            console.error('Export failed:', err);
            setError(err.message || 'Failed to export data');
        } finally {
            setLoading(false);
        }
    };

    const handleImport = async () => {
        if (!selectedFile) {
            setError('Please select a file first');
            return;
        }

        // Validate file type
        const fileName = selectedFile.name.toLowerCase();
        if (!fileName.endsWith('.xlsx') && !fileName.endsWith('.xls')) {
            setError('Invalid file type. Please upload an Excel file (.xlsx or .xls)');
            return;
        }

        setError('');
        setSuccess('');
        setImportResult(null);
        setLoading(true);

        try {
            const result = await excelService.importMembers(selectedFile);
            setImportResult(result);

            if (result.success) {
                setSuccess(result.message);
                setSelectedFile(null);
                // Reset file input
                document.getElementById('fileInput').value = '';
            } else {
                setError(result.message);
            }
        } catch (err) {
            setError(err.message || 'Failed to import data');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="page-container">
            <div className="page-header">
                <button onClick={() => navigate('/dashboard')} className="back-button">
                    ← Back
                </button>
                <h1>Excel Management</h1>
                <div style={{ width: '80px' }}></div>
            </div>

            {/* Export Section */}
            <div className="excel-section">
                <h2 className="section-title">📊 Export Members Data</h2>
                <p className="section-description">
                    Download all members with their latest payment information as an Excel file.
                </p>
                <button
                    onClick={handleExport}
                    className="btn-export"
                    disabled={loading}
                >
                    {loading ? '⏳ Exporting...' : '⬇️ Export to Excel'}
                </button>
            </div>

            {/* Import Section */}
            <div className="excel-section">
                <h2 className="section-title">📤 Import Members Data</h2>
                <p className="section-description">
                    Upload an Excel file to import new members. The file should contain columns:
                    Name, Email, Phone, Status, Join Date.
                </p>

                <div className="file-upload-container">
                    <input
                        type="file"
                        id="fileInput"
                        accept=".xlsx,.xls"
                        onChange={handleFileSelect}
                        className="file-input"
                    />
                    <label htmlFor="fileInput" className="file-label">
                        {selectedFile ? (
                            <>
                                <span className="file-icon">📄</span>
                                <span className="file-name">{selectedFile.name}</span>
                            </>
                        ) : (
                            <>
                                <span className="file-icon">📁</span>
                                <span>Choose Excel File</span>
                            </>
                        )}
                    </label>
                </div>

                <button
                    onClick={handleImport}
                    className="btn-import"
                    disabled={!selectedFile || loading}
                >
                    {loading ? '⏳ Importing...' : '⬆️ Import from Excel'}
                </button>
            </div>

            {/* Messages */}
            {error && (
                <div className="message-box error-box">
                    <strong>❌ Error:</strong> {error}
                </div>
            )}

            {success && (
                <div className="message-box success-box">
                    <strong>✅ Success:</strong> {success}
                </div>
            )}

            {/* Import Results */}
            {importResult && (
                <div className="import-results">
                    <h3 className="results-title">Import Summary</h3>
                    <div className="results-stats">
                        <div className="stat-item success-stat">
                            <span className="stat-label">Created:</span>
                            <span className="stat-value">{importResult.successCount || 0}</span>
                        </div>
                        <div className="stat-item warning-stat">
                            <span className="stat-label">Skipped:</span>
                            <span className="stat-value">{importResult.skippedCount || 0}</span>
                        </div>
                        <div className="stat-item error-stat">
                            <span className="stat-label">Errors:</span>
                            <span className="stat-value">{importResult.errorCount || 0}</span>
                        </div>
                    </div>

                    {importResult.errors && importResult.errors.length > 0 && (
                        <div className="errors-list">
                            <h4>Details:</h4>
                            <ul>
                                {importResult.errors.slice(0, 10).map((err, index) => (
                                    <li key={index}>{err}</li>
                                ))}
                                {importResult.errors.length > 10 && (
                                    <li className="more-errors">
                                        ... and {importResult.errors.length - 10} more
                                    </li>
                                )}
                            </ul>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}

export default ExcelManagement;
