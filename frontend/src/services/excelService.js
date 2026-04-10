import api from './api';

const excelService = {
    /**
     * Export all members with payment data to Excel
     */
    exportMembers: async () => {
        try {
            const token = localStorage.getItem('token');
            const response = await api.get('/excel/export', {
                headers: {
                    'Authorization': token
                },
                responseType: 'blob' // Important for file download
            });

            // Create blob from response
            const blob = new Blob([response.data], {
                type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
            });

            // Create download link
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;

            // Generate filename with timestamp
            const timestamp = new Date().toISOString().replace(/[:.]/g, '-').slice(0, -5);
            link.download = `members_export_${timestamp}.xlsx`;

            // Trigger download
            document.body.appendChild(link);
            link.click();

            // Cleanup
            document.body.removeChild(link);
            window.URL.revokeObjectURL(url);

            return { success: true, message: 'Export successful' };
        } catch (error) {
            console.error('Export error:', error);

            // If error response is a blob, parse it
            if (error.response?.data instanceof Blob) {
                try {
                    const text = await error.response.data.text();
                    console.log('Error blob content:', text);
                    const errorData = JSON.parse(text);
                    throw new Error(errorData.message || 'Failed to export data');
                } catch (parseError) {
                    console.error('Failed to parse error blob:', parseError);
                    // If we can't parse the error, show a helpful message
                    if (error.response?.status === 500) {
                        throw new Error('Server error occurred. Please ensure the backend server is running and has been restarted after recent code changes.');
                    }
                    throw new Error('Failed to export data. Please check if you are logged in as admin.');
                }
            }

            // Handle regular error responses
            const errorMessage = error.response?.data?.message || error.message || 'Failed to export data';
            throw new Error(errorMessage);
        }
    },

    /**
     * Import members from Excel file
     */
    importMembers: async (file) => {
        try {
            const token = localStorage.getItem('token');
            const formData = new FormData();
            formData.append('file', file);

            const response = await api.post('/excel/import', formData, {
                headers: {
                    'Authorization': token,
                    'Content-Type': 'multipart/form-data'
                }
            });

            return response.data;
        } catch (error) {
            console.error('Import error:', error);
            throw error.response?.data || { message: 'Failed to import data' };
        }
    }
};

export { excelService };
