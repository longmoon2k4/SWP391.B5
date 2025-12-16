/**
 * Bum Platform License Validation SDK for JavaScript
 * Version: 1.0.0
 * 
 * Usage:
 *   const client = new BumApiClient('your-api-key', 'https://yoursite.com');
 *   client.validateLicense('LICENSE-KEY').then(response => {
 *       if (response.valid) {
 *           console.log('License is valid!');
 *       }
 *   });
 */

class BumApiClient {
    /**
     * Constructor
     * @param {string} apiKey - Your Bum API key from dashboard
     * @param {string} baseUrl - Base URL of Bum server (optional, defaults to https://yoursite.com)
     */
    constructor(apiKey, baseUrl = 'https://yoursite.com') {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl.replace(/\/$/, ''); // Remove trailing slash
        this.timeout = 5000;
        this.validateInputs();
    }

    /**
     * Validate input parameters
     */
    validateInputs() {
        if (!this.apiKey || typeof this.apiKey !== 'string') {
            throw new Error('Invalid API key');
        }
        if (!this.baseUrl || typeof this.baseUrl !== 'string') {
            throw new Error('Invalid base URL');
        }
    }

    /**
     * Validate if API key is active
     * @returns {Promise<boolean>} True if API key is valid
     */
    async validateApiKey() {
        try {
            const response = await this._request('/api/v1/validate-key', 'POST', null);
            return response.valid === true;
        } catch (error) {
            console.error('API Key validation error:', error);
            return false;
        }
    }

    /**
     * Validate license key
     * @param {string} licenseKey - The license key to validate
     * @param {string} hardwareId - Optional hardware ID for hardware-locked licenses
     * @returns {Promise<Object>} License validation response
     */
    async validateLicense(licenseKey, hardwareId = null) {
        try {
            if (!licenseKey || typeof licenseKey !== 'string') {
                throw new Error('Invalid license key');
            }

            const payload = {
                licenseKey: licenseKey
            };

            if (hardwareId) {
                payload.hardwareId = hardwareId;
            }

            return await this._request('/api/v1/licenses/validate', 'POST', payload);
        } catch (error) {
            console.error('License validation error:', error);
            return {
                valid: false,
                status: 'error',
                message: error.message,
                productName: null,
                expireDate: null,
                hardwareId: null,
                userId: null
            };
        }
    }

    /**
     * Get hardware ID (system specific)
     * For browser: returns a unique browser ID
     * For Node.js: returns system info
     * @returns {string} Hardware identifier
     */
    static getHardwareId() {
        // Browser environment
        if (typeof navigator !== 'undefined') {
            const browserId = [
                navigator.userAgent,
                navigator.language,
                new Date().getTimezoneOffset(),
                screen.width + 'x' + screen.height
            ].join('|');
            
            // Simple hash
            return 'browser_' + this._hashCode(browserId);
        }

        // Node.js environment
        if (typeof require !== 'undefined') {
            try {
                const os = require('os');
                return 'node_' + os.hostname() + '_' + os.arch();
            } catch (e) {
                return 'node_unknown';
            }
        }

        return 'unknown';
    }

    /**
     * Internal request method
     * @private
     */
    async _request(endpoint, method = 'POST', body = null) {
        const url = `${this.baseUrl}${endpoint}`;
        
        const options = {
            method: method,
            headers: {
                'Authorization': `Bearer ${this.apiKey}`,
                'Content-Type': 'application/json'
            },
            signal: AbortSignal.timeout(this.timeout)
        };

        if (body) {
            options.body = JSON.stringify(body);
        }

        const response = await fetch(url, options);
        
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        return await response.json();
    }

    /**
     * Simple hash function
     * @private
     */
    static _hashCode(str) {
        let hash = 0;
        if (str.length === 0) return hash.toString();
        
        for (let i = 0; i < str.length; i++) {
            const char = str.charCodeAt(i);
            hash = ((hash << 5) - hash) + char;
            hash = hash & hash; // Convert to 32bit integer
        }
        
        return Math.abs(hash).toString(16);
    }

    /**
     * Set custom timeout
     * @param {number} timeoutMs - Timeout in milliseconds
     */
    setTimeout(timeoutMs) {
        this.timeout = timeoutMs;
    }
}

// Export for Node.js and browsers
if (typeof module !== 'undefined' && module.exports) {
    module.exports = BumApiClient;
}
