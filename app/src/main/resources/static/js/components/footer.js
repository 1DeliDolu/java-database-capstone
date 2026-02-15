/**
 * Footer Component
 * 
 * This module provides a reusable footer component that renders consistently
 * across all pages. Includes branding, navigation links, and legal information.
 */

/**
 * Render footer content into the page
 * Injects footer HTML with branding, navigation links, and legal information
 */
function renderFooter() {
  // Access the footer container from the DOM
  const footer = document.getElementById("footer");
  
  // Return early if footer element doesn't exist on the page
  if (!footer) return;

  // Inject footer HTML content with branding, links, and legal info
  footer.innerHTML = `
    <footer class="footer">
      <div class="footer-container">
        
        <!-- Branding and Copyright Section -->
        <div class="footer-logo">
          <img src="../assets/images/logo/logo.png" alt="Hospital CMS Logo" class="footer-logo-img">
          <p>&copy; Copyright 2025. All Rights Reserved by Hospital CMS.</p>
        </div>

        <!-- Navigation Links Section (3 Columns) -->
        <div class="footer-links">
          
          <!-- Company Links Column -->
          <div class="footer-column">
            <h4>Company</h4>
            <a href="#">About</a>
            <a href="#">Careers</a>
            <a href="#">Press</a>
          </div>

          <!-- Support Links Column -->
          <div class="footer-column">
            <h4>Support</h4>
            <a href="#">Account</a>
            <a href="#">Help Center</a>
            <a href="#">Contact Us</a>
          </div>

          <!-- Legal Links Column -->
          <div class="footer-column">
            <h4>Legals</h4>
            <a href="#">Terms & Conditions</a>
            <a href="#">Privacy Policy</a>
            <a href="#">Licensing</a>
          </div>

        </div>
        <!-- End of footer-links -->

      </div>
      <!-- End of footer-container -->
    </footer>`;
}

// Call renderFooter when DOM is loaded
document.addEventListener("DOMContentLoaded", renderFooter);
