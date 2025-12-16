#!/usr/bin/env python3
"""
Bum Platform License Validation SDK for Python
Version: 1.0.0

Installation:
    pip install requests

Usage:
    from bum_sdk import BumApiClient
    
    client = BumApiClient('your-api-key', 'https://yoursite.com')
    response = client.validate_license('LICENSE-KEY')
    
    if response['valid']:
        print('License is valid!')
    else:
        print(f'License invalid: {response["message"]}')
"""

import requests
import json
import hashlib
import uuid
import os
from typing import Optional, Dict, Any
from urllib.parse import urljoin


class BumApiClient:
    """
    Client for Bum Platform License Validation API
    """

    def __init__(self, api_key: str, base_url: str = 'https://yoursite.com'):
        """
        Initialize BumApiClient
        
        Args:
            api_key: Your Bum API key from dashboard
            base_url: Base URL of Bum server
        
        Raises:
            ValueError: If api_key or base_url is invalid
        """
        if not api_key or not isinstance(api_key, str):
            raise ValueError('Invalid API key')
        if not base_url or not isinstance(base_url, str):
            raise ValueError('Invalid base URL')
        
        self.api_key = api_key
        self.base_url = base_url.rstrip('/')
        self.timeout = 5
        self.session = requests.Session()
        self._setup_session()

    def _setup_session(self):
        """Setup session with default headers"""
        self.session.headers.update({
            'Content-Type': 'application/json',
            'User-Agent': 'BumApiClient/1.0.0 (Python)'
        })

    def validate_api_key(self) -> bool:
        """
        Validate if API key is active
        
        Returns:
            bool: True if API key is valid and active
        """
        try:
            response = self._request('/api/v1/validate-key', 'POST')
            return response.get('valid', False) is True
        except Exception as e:
            print(f'Error validating API key: {e}')
            return False

    def validate_license(self, license_key: str, hardware_id: Optional[str] = None) -> Dict[str, Any]:
        """
        Validate license key
        
        Args:
            license_key: The license key to validate
            hardware_id: Optional hardware ID for hardware-locked licenses
        
        Returns:
            dict: License validation response with keys:
                - valid (bool): Whether license is valid
                - status (str): License status (active, expired, banned, etc.)
                - message (str): Status message
                - productName (str): Product name
                - expireDate (str): Expiration date
                - hardwareId (str): Hardware ID
                - userId (str): User ID
        """
        try:
            if not license_key or not isinstance(license_key, str):
                raise ValueError('Invalid license key')

            payload = {
                'licenseKey': license_key
            }

            if hardware_id:
                payload['hardwareId'] = hardware_id

            return self._request('/api/v1/licenses/validate', 'POST', payload)
        except Exception as e:
            print(f'Error validating license: {e}')
            return {
                'valid': False,
                'status': 'error',
                'message': str(e),
                'productName': None,
                'expireDate': None,
                'hardwareId': None,
                'userId': None
            }

    @staticmethod
    def get_hardware_id() -> str:
        """
        Get system hardware ID
        
        Returns:
            str: Unique hardware identifier
        """
        try:
            # Try to get MAC address
            mac = uuid.getnode()
            if mac != 0:
                return f'hw_{mac:x}'
            
            # Fallback to hostname + platform
            import platform
            hostname = platform.node()
            machine = platform.machine()
            return hashlib.sha256(f'{hostname}_{machine}'.encode()).hexdigest()[:16]
        except Exception:
            # Last resort: random UUID
            return f'hw_{uuid.uuid4().hex[:16]}'

    def _request(self, endpoint: str, method: str = 'POST', body: Optional[Dict] = None) -> Dict:
        """
        Internal request method
        
        Args:
            endpoint: API endpoint path
            method: HTTP method (POST, GET, etc.)
            body: Request body as dictionary
        
        Returns:
            dict: Response data as dictionary
        
        Raises:
            requests.RequestException: If request fails
        """
        url = urljoin(self.base_url, endpoint)
        
        headers = {
            'Authorization': f'Bearer {self.api_key}',
            'Content-Type': 'application/json'
        }

        try:
            if method.upper() == 'POST':
                response = self.session.post(
                    url,
                    json=body,
                    headers=headers,
                    timeout=self.timeout
                )
            else:
                response = self.session.request(
                    method,
                    url,
                    json=body,
                    headers=headers,
                    timeout=self.timeout
                )

            response.raise_for_status()
            return response.json()
        except requests.exceptions.Timeout:
            raise Exception('Request timeout')
        except requests.exceptions.ConnectionError:
            raise Exception('Connection error')
        except requests.exceptions.HTTPError as e:
            raise Exception(f'HTTP {response.status_code}: {response.text}')
        except json.JSONDecodeError:
            raise Exception('Invalid JSON response')

    def set_timeout(self, timeout_seconds: int):
        """
        Set request timeout
        
        Args:
            timeout_seconds: Timeout in seconds
        """
        self.timeout = timeout_seconds

    def close(self):
        """Close session"""
        self.session.close()

    def __enter__(self):
        """Context manager entry"""
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        """Context manager exit"""
        self.close()


# Example usage
if __name__ == '__main__':
    # Initialize client
    client = BumApiClient('your-api-key-here')
    
    # Validate API key
    print('Validating API key...')
    if client.validate_api_key():
        print('✓ API key is valid')
    else:
        print('✗ API key is invalid')
    
    # Validate license
    print('\nValidating license...')
    response = client.validate_license('YOUR-LICENSE-KEY')
    
    if response['valid']:
        print(f'✓ License is valid')
        print(f"  Product: {response.get('productName')}")
        print(f"  Expires: {response.get('expireDate')}")
        print(f"  User ID: {response.get('userId')}")
    else:
        print(f'✗ License invalid: {response["message"]}')
        print(f"  Status: {response.get('status')}")
    
    # Close session
    client.close()
