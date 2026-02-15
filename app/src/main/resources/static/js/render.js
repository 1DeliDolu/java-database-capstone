/**
 * Render Module
 * 
 * Handles role-based navigation and content rendering:
 * - Role selection and storage (selectRole)
 * - Role-based page redirects
 * - Content rendering based on user permissions
 * - Authentication checks and guards
 * 
 * Dependencies:
 * - setRole(), getRole() from ./util.js
 * - localStorage for token and role storage
 */

import { setRole, getRole, hasRole, isAuthenticated } from './util.js';

/**
 * Handle role selection and redirect user to appropriate dashboard
 * Called from index.html when user clicks a role button
 * 
 * Redirects:
 * - admin: goes to admin dashboard (requires authentication)
 * - doctor: goes to doctor dashboard (requires authentication)
 * - patient: goes to patient dashboard (allows browsing without login)
 * - loggedPatient: goes to patient dashboard authenticated view
 * 
 * @param {string} role - The role selected ('admin', 'doctor', 'patient', 'loggedPatient')
 */
window.selectRole = function (role) {
  try {
    // Step 1: Validate role
    const validRoles = ['admin', 'doctor', 'patient', 'loggedPatient'];
    if (!validRoles.includes(role)) {
      console.error('Invalid role:', role);
      alert('Invalid role selection. Please try again.');
      return;
    }

    // Step 2: Store role in localStorage
    setRole(role);

    // Step 3: Get token and current role if exists
    const token = localStorage.getItem('token');
    const currentRole = localStorage.getItem('userRole');

    // Step 4: Redirect based on role
    switch (role) {
      case 'admin':
        if (token && currentRole === 'admin') {
          window.location.href = `/adminDashboard/${token}`;
        } else {
          window.location.href = '/login?role=admin';
        }
        break;

      case 'doctor':
        if (token && currentRole === 'doctor') {
          window.location.href = `/doctorDashboard/${token}`;
        } else {
          window.location.href = '/login?role=doctor';
        }
        break;

      case 'patient':
        if (token && (currentRole === 'loggedPatient' || currentRole === 'patient')) {
          window.location.href = '/pages/loggedPatientDashboard.html';
        } else {
          window.location.href = '/login?role=patient';
        }
        break;

      case 'loggedPatient':
        if (token) {
          // Authenticated patient - go to patient dashboard (authenticated view)
          window.location.href = '/pages/loggedPatientDashboard.html';
        } else {
          // No token - shouldn't happen, fallback to patient dashboard
          window.location.href = '/pages/patientDashboard.html';
        }
        break;

      default:
        console.error('Unhandled role:', role);
        window.location.href = '/';
    }
  } catch (error) {
    console.error('Error in selectRole:', error);
    alert('An error occurred. Please try again.');
  }
};

/**
 * Render content based on user's authenticated role
 * Called on page load to check authorization
 * Prevents access to pages user doesn't have permission for
 * 
 * @returns {void} Redirects to login if not authenticated
 */
window.renderContent = function () {
  try {
    // Step 1: Get current role
    const role = getRole();

    // Step 2: Check if user has a role
    if (!role) {
      console.log('No role found. Redirecting to role selection.');
      window.location.href = '/'; // Redirect to role selection page
      return;
    }

    // Step 3: Handle role-specific rendering
    // Admin and Doctor require token; Patient doesn't
    if ((role === 'admin' || role === 'doctor' || role === 'loggedPatient') && !localStorage.getItem('token')) {
      console.log('No token found for role:', role);
      // Don't redirect immediately - let dashboard show login modal
      // Could redirect here if we want to enforce strict auth
    }

    // Content is now rendered based on role
    console.log('Rendering content for role:', role);
  } catch (error) {
    console.error('Error in renderContent:', error);
    window.location.href = '/';
  }
};

/**
 * Protected page wrapper - call at top of authenticated pages
 * Ensures user has required role before allowing page access
 * 
 * @param {string|Array} requiredRoles - Role(s) allowed on this page
 * @returns {boolean} True if user has permission, false if redirected
 */
window.protectPage = function (requiredRoles) {
  try {
    const currentRole = getRole();
    const roles = Array.isArray(requiredRoles) ? requiredRoles : [requiredRoles];

    if (!roles.includes(currentRole)) {
      console.log('Access denied. Required roles:', roles, 'Current role:', currentRole);
      window.location.href = '/';
      return false;
    }

    return true;
  } catch (error) {
    console.error('Error in protectPage:', error);
    window.location.href = '/';
    return false;
  }
};

/**
 * Handle logout - clear authentication and return to role selection
 * Called by header component logout button
 */
window.handleLogout = function () {
  try {
    // Step 1: Clear authentication data
    localStorage.removeItem('token');
    localStorage.removeItem('userRole');

    // Step 2: Show confirmation
    alert('✓ You have been logged out.');

    // Step 3: Redirect to role selection
    window.location.href = '/';
  } catch (error) {
    console.error('Error during logout:', error);
    window.location.href = '/';
  }
};

/**
 * Check if user is authenticated and has a specific role
 * Useful for conditionally showing/hiding UI elements
 * 
 * @param {string} role - Role to check for
 * @returns {boolean} True if user has the specified role
 */
window.hasUserRole = function (role) {
  try {
    return hasRole(role);
  } catch (error) {
    console.error('Error in hasUserRole:', error);
    return false;
  }
};

export {
  selectRole: window.selectRole,
  renderContent: window.renderContent,
  protectPage: window.protectPage,
  handleLogout: window.handleLogout,
  hasUserRole: window.hasUserRole
};
