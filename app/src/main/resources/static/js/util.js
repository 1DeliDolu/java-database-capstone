/**
 * Utility Functions Module
 *
 * Provides common helper functions used throughout the application:
 * - Token management (storage, retrieval, clearing)
 * - Date formatting and manipulation
 * - Role management (storage, retrieval, checking)
 * - User notifications (alerts, success messages)
 *
 * This module centralizes shared functionality to promote code reuse
 * and maintain a single source of truth for utility operations.
 */

// =====================================================
// TOKEN MANAGEMENT
// =====================================================

/**
 * Store authentication token in localStorage
 *
 * @param {string} token - The JWT token to store
 */
export function setToken(token) {
  if (!token) {
    console.warn("setToken: Empty token provided");
    return;
  }
  localStorage.setItem("token", token);
}

/**
 * Retrieve authentication token from localStorage
 *
 * @returns {string|null} The stored token or null if not found
 */
export function getToken() {
  return localStorage.getItem("token");
}

/**
 * Remove authentication token from localStorage
 */
export function clearToken() {
  localStorage.removeItem("token");
}

/**
 * Check if a valid token exists in localStorage
 *
 * @returns {boolean} True if token exists and is not empty
 */
export function hasValidToken() {
  const token = getToken();
  return !!token && token.length > 0;
}

// =====================================================
// ROLE MANAGEMENT
// =====================================================

/**
 * Store user role in localStorage
 * Roles: 'admin', 'doctor', 'patient', 'loggedPatient'
 *
 * @param {string} role - The user role to store
 */
export function setRole(role) {
  if (!role) {
    console.warn("setRole: Empty role provided");
    return;
  }
  localStorage.setItem("userRole", role);
}

/**
 * Retrieve user role from localStorage
 *
 * @returns {string|null} The stored role or null if not found
 */
export function getRole() {
  return localStorage.getItem("userRole");
}

/**
 * Remove user role from localStorage
 */
export function clearRole() {
  localStorage.removeItem("userRole");
}

/**
 * Check if current user has a specific role
 *
 * @param {string} role - The role to check for
 * @returns {boolean} True if current role matches the provided role
 */
export function hasRole(role) {
  return getRole() === role;
}

/**
 * Check if current user is authenticated
 * A user is authenticated if they have both a valid token and a role set
 *
 * @returns {boolean} True if user has valid token and role
 */
export function isAuthenticated() {
  return hasValidToken() && !!getRole();
}

// =====================================================
// DATE FORMATTING
// =====================================================

/**
 * Get today's date as a string in YYYY-MM-DD format
 * Used for initializing date pickers and setting default dates
 *
 * @returns {string} Today's date in format YYYY-MM-DD
 */
export function getTodayDate() {
  const today = new Date();
  const year = today.getFullYear();
  const month = String(today.getMonth() + 1).padStart(2, "0");
  const day = String(today.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

/**
 * Format a date object or string to a readable string
 * Converts Date object or ISO string to format like "Jan 15, 2024"
 *
 * @param {Date|string} date - The date to format
 * @returns {string} Formatted date string
 */
export function formatDate(date) {
  try {
    const dateObj = typeof date === "string" ? new Date(date) : date;

    if (isNaN(dateObj.getTime())) {
      return "Invalid date";
    }

    const options = { year: "numeric", month: "short", day: "numeric" };
    return dateObj.toLocaleDateString("en-US", options);
  } catch (error) {
    console.error("Error formatting date:", error);
    return "Invalid date";
  }
}

/**
 * Format a time string from HH:mm:ss to HH:mm
 * Used for displaying appointment times
 *
 * @param {string} timeString - Time in format HH:mm:ss
 * @returns {string} Formatted time in HH:mm
 */
export function formatTime(timeString) {
  if (!timeString) return "-";

  try {
    const parts = timeString.split(":");
    if (parts.length >= 2) {
      return `${parts[0]}:${parts[1]}`;
    }
    return timeString;
  } catch (error) {
    console.error("Error formatting time:", error);
    return timeString;
  }
}

/**
 * Format a datetime string to a readable format
 * Converts ISO datetime to format like "Jan 15, 2024 2:30 PM"
 *
 * @param {string} datetime - ISO datetime string
 * @returns {string} Formatted datetime string
 */
export function formatDateTime(datetime) {
  try {
    const date = new Date(datetime);

    if (isNaN(date.getTime())) {
      return "Invalid datetime";
    }

    const dateOptions = { year: "numeric", month: "short", day: "numeric" };
    const timeOptions = { hour: "2-digit", minute: "2-digit" };

    const formattedDate = date.toLocaleDateString("en-US", dateOptions);
    const formattedTime = date.toLocaleTimeString("en-US", timeOptions);

    return `${formattedDate} ${formattedTime}`;
  } catch (error) {
    console.error("Error formatting datetime:", error);
    return "Invalid datetime";
  }
}

/**
 * Convert time string to 12-hour format
 * Converts 14:30 to 2:30 PM
 *
 * @param {string} time24 - Time in 24-hour format HH:mm
 * @returns {string} Time in 12-hour format
 */
export function convertTo12Hour(time24) {
  if (!time24) return "-";

  try {
    const [hours, minutes] = time24.split(":");
    let hour = parseInt(hours);
    const ampm = hour >= 12 ? "PM" : "AM";
    hour = hour % 12;
    hour = hour ? hour : 12; // 0 should be 12

    return `${hour}:${minutes} ${ampm}`;
  } catch (error) {
    console.error("Error converting time:", error);
    return time24;
  }
}

// =====================================================
// NOTIFICATION & USER FEEDBACK
// =====================================================

/**
 * Show a success notification to the user
 * Displays a green alert with a checkmark
 *
 * @param {string} message - The success message to display
 */
export function showSuccess(message) {
  alert(`✓ ${message}`);
}

/**
 * Show an error notification to the user
 * Displays a red alert with an X mark
 *
 * @param {string} message - The error message to display
 */
export function showError(message) {
  alert(`✗ ${message}`);
}

/**
 * Show an info notification to the user
 * Displays a blue alert with an info mark
 *
 * @param {string} message - The info message to display
 */
export function showInfo(message) {
  alert(`ℹ ${message}`);
}

/**
 * Show a warning notification to the user
 * Displays a yellow alert with a warning mark
 *
 * @param {string} message - The warning message to display
 */
export function showWarning(message) {
  alert(`⚠ ${message}`);
}

/**
 * Show a confirmation dialog and return result
 * User can accept or decline an action
 *
 * @param {string} message - The confirmation message
 * @returns {boolean} True if user clicks OK, false if clicks Cancel
 */
export function showConfirm(message) {
  return confirm(message);
}

/**
 * Log message to console with timestamp (development only)
 * Helps with debugging without cluttering production
 *
 * @param {string} message - The message to log
 * @param {any} data - Optional data to log
 */
export function debugLog(message, data = null) {
  if (
    process.env.NODE_ENV === "development" ||
    localStorage.getItem("DEBUG_MODE")
  ) {
    const timestamp = new Date().toLocaleTimeString();
    console.log(`[${timestamp}] ${message}`, data || "");
  }
}

// =====================================================
// VALIDATION HELPERS
// =====================================================

/**
 * Validate email format
 *
 * @param {string} email - Email address to validate
 * @returns {boolean} True if email is valid format
 */
export function isValidEmail(email) {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
}

/**
 * Validate password strength (minimum 6 characters)
 *
 * @param {string} password - Password to validate
 * @returns {boolean} True if password meets requirements
 */
export function isValidPassword(password) {
  return password && password.length >= 6;
}

/**
 * Validate phone number format (basic validation)
 * Accepts formats: 1234567890, 123-456-7890, (123) 456-7890
 *
 * @param {string} phone - Phone number to validate
 * @returns {boolean} True if phone is valid format
 */
export function isValidPhone(phone) {
  const phoneRegex = /^\d{10}$|^\d{3}-\d{3}-\d{4}$|^\(\d{3}\) \d{3}-\d{4}$/;
  return phoneRegex.test(phone.replace(/\s/g, ""));
}

/**
 * Check if string is not empty after trimming
 *
 * @param {string} str - String to check
 * @returns {boolean} True if string has content
 */
export function isNotEmpty(str) {
  return str && str.trim().length > 0;
}

// =====================================================
// STRING MANIPULATION
// =====================================================

/**
 * Capitalize first letter of a string
 *
 * @param {string} str - String to capitalize
 * @returns {string} String with first letter capitalized
 */
export function capitalize(str) {
  if (!str) return "";
  return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
}

/**
 * Truncate string to maximum length with ellipsis
 *
 * @param {string} str - String to truncate
 * @param {number} maxLength - Maximum length before truncating
 * @returns {string} Truncated string
 */
export function truncate(str, maxLength = 50) {
  if (!str) return "";
  return str.length > maxLength ? str.substring(0, maxLength) + "..." : str;
}

// =====================================================
// API HELPERS
// =====================================================

/**
 * Get authorization header with Bearer token
 * Used for API calls requiring authentication
 *
 * @returns {Object} Headers object with Authorization
 */
export function getAuthHeaders() {
  const token = getToken();
  return {
    "Content-Type": "application/json",
    Authorization: `Bearer ${token}`,
  };
}

/**
 * Create standard fetch options for API calls
 * Includes method, headers, and optional body
 *
 * @param {string} method - HTTP method (GET, POST, PUT, DELETE)
 * @param {Object} body - Optional request body
 * @returns {Object} Fetch options object
 */
export function getFetchOptions(method = "GET", body = null) {
  const options = {
    method,
    headers: getAuthHeaders(),
  };

  if (body) {
    options.body = JSON.stringify(body);
  }

  return options;
}

export default {
  // Token functions
  setToken,
  getToken,
  clearToken,
  hasValidToken,

  // Role functions
  setRole,
  getRole,
  clearRole,
  hasRole,
  isAuthenticated,

  // Date functions
  getTodayDate,
  formatDate,
  formatTime,
  formatDateTime,
  convertTo12Hour,

  // Notification functions
  showSuccess,
  showError,
  showInfo,
  showWarning,
  showConfirm,
  debugLog,

  // Validation functions
  isValidEmail,
  isValidPassword,
  isValidPhone,
  isNotEmpty,

  // String functions
  capitalize,
  truncate,

  // API helpers
  getAuthHeaders,
  getFetchOptions,
};
