document.addEventListener("DOMContentLoaded", function () {

    /* =========================
       Global Enter key handler
    ========================== */
    document.addEventListener("keydown", function (event) {
        if (event.key === "Enter") {
            const activeElement = document.activeElement;
            const form = activeElement.closest("form");

            if (form) {
                event.preventDefault();
                form.requestSubmit();
            }
        }
    });
    
});

    /* =========================
       Register → OTP
    ========================== */

const registerForm = document.getElementById("registerForm");

if (registerForm) {

    registerForm.addEventListener("submit", async function (e) {

        e.preventDefault();

        const registerBtn = document.getElementById("registerBtn");
        const errorBox = document.getElementById("registerError");

        errorBox.textContent = "";

        const name = document.getElementById("regName").value.trim();
        const email = document.getElementById("regEmail").value.trim();
        const password = document.getElementById("regPassword").value;
        const confirmPassword = document.getElementById("regConfirmPassword").value;

        // password check
        if (password !== confirmPassword) {
            errorBox.textContent = "Passwords do not match";
            return;
        }

        try {

            // ✅ Disable button and show loading
            registerBtn.disabled = true;
            registerBtn.textContent = "Creating account...";

            const response = await fetch("http://127.0.0.1:8080/api/auth/register", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    name: name,
                    email: email,
                    password: password
                })
            });

            const result = await response.text();

            if (response.ok) {

                localStorage.setItem("registeredEmail", email);

                // small delay for UX smoothness
                setTimeout(() => {
                    window.location.href = "otp.html?type=register";
                }, 500);

            } else {

                errorBox.textContent = result;

                // re-enable button
                registerBtn.disabled = false;
                registerBtn.textContent = "Register";

            }

        } catch (error) {

            errorBox.textContent = "Server error. Please try again.";

            registerBtn.disabled = false;
            registerBtn.textContent = "Register";

        }

    });

}

/* =========================
   OTP Verification
========================= */

const otpForm = document.getElementById("otpForm");

if (otpForm) {

    // auto-fill email
    const emailInput = document.getElementById("otpEmail");
    const savedEmail = localStorage.getItem("registeredEmail");

    if (savedEmail && emailInput) {
        emailInput.value = savedEmail;
        emailInput.readOnly = true;
    }

    otpForm.addEventListener("submit", async function (e) {

        e.preventDefault();

        const email = emailInput.value;
        const otp = document.getElementById("otpInput").value;

        const message = document.getElementById("otpMessage");

        message.textContent = "Verifying OTP...";

        try {

            const response = await fetch("http://localhost:8080/api/auth/verify-otp", {

                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify({
                    email: email,
                    otp: otp
                })
            });

            const result = await response.text();

            if (response.ok) {

                message.textContent = "Verification successful.";

                // clear saved email
                localStorage.removeItem("registeredEmail");

                setTimeout(() => {
                    window.location.href = "login.html";
                }, 1000);

            } else {

                message.textContent = result;
            }

        } catch (error) {

            message.textContent = "Server error. Try again.";
        }

    });

}

/* =========================
   Login
========================== */

 const loginForm = document.getElementById("loginForm");

if (loginForm) {

    loginForm.addEventListener("submit", async function (e) {

        e.preventDefault();

        const errorBox = document.getElementById("loginError");
        errorBox.textContent = "";

        const email = document.getElementById("loginEmail").value;
        const password = document.getElementById("loginPassword").value;

        try {

            const response = await fetch("http://127.0.0.1:8080/api/auth/login", {

                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify({
                    email: email,
                    password: password
                })

            });

            const text = await response.text();

            if (!response.ok) {

                errorBox.textContent = text;
                return;
            }

            const result = JSON.parse(text);

            // ✅ correct storage
            localStorage.setItem("token", result.token);
            localStorage.setItem("userRole", result.role);
            localStorage.setItem("username", result.name);

            // redirect
            if (result.role === "AUTHORITY") {

                window.location.href = "authority/dashboard.html";

            } else {

                window.location.href = "user/home.html";

            }

        } catch (error) {

            console.error(error);
            errorBox.textContent = "Server error. Please try again.";

        }

    });

}


    /* =========================
   Forgot password → Backend API
========================== */
const forgotForm = document.getElementById("forgotForm");

if (forgotForm) {

    forgotForm.addEventListener("submit", async function (e) {

        e.preventDefault();

        const email = document.getElementById("forgotEmail").value;
        const message = document.getElementById("forgotMessage");

        message.textContent = "Sending reset link...";

        try {

            const response = await fetch("http://localhost:8080/api/auth/forgot-password", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    email: email
                })
            });

            if (response.ok) {

                message.textContent = "Password reset link sent to your email.";

            } else {

                message.textContent = "Email not found.";
            }

        } catch (error) {

            message.textContent = "Server error. Try again.";
        }

    });

}


    /* =========================
   Reset password → Backend API
========================== */
const resetForm = document.getElementById("resetForm");

if (resetForm) {

    resetForm.addEventListener("submit", async function (e) {

        e.preventDefault();

        const newPass = document.getElementById("newPassword").value;
        const confirmPass = document.getElementById("confirmPassword").value;

        const successMsg = document.getElementById("resetMessage");
        const errorMsg = document.getElementById("resetError");

        successMsg.textContent = "";
        errorMsg.textContent = "";

        // Validate passwords
        if (newPass !== confirmPass) {

            errorMsg.textContent = "Passwords do not match.";
            return;
        }

        if (newPass.length < 6) {

            errorMsg.textContent = "Password must be at least 6 characters.";
            return;
        }

        // Get token from URL
        const params = new URLSearchParams(window.location.search);
        const token = params.get("token");

        if (!token) {

            errorMsg.textContent = "Invalid reset link.";
            return;
        }

        try {

            const response = await fetch("http://localhost:8080/api/auth/reset-password", {

                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify({
                    token: token,
                    newPassword: newPass
                })
            });

            if (response.ok) {

                successMsg.textContent = "Password reset successful.";

                setTimeout(() => {
                    window.location.href = "login.html";
                }, 1500);

            } else {

                errorMsg.textContent = "Reset failed or token expired.";
            }

        } catch (error) {

            errorMsg.textContent = "Server error. Try again.";
        }

    });

}

 /* =========================
       Sidebar toggle (responsive)
    ========================== */
    const toggleBtn = document.getElementById("menuToggle");
    const sidebar = document.getElementById("sidebar");
    const content = document.getElementById("content");

    if (toggleBtn && sidebar) {

        // Create overlay element for mobile/tablet
        const overlay = document.createElement("div");
        overlay.className = "sidebar-overlay";
        document.body.appendChild(overlay);

        toggleBtn.addEventListener("click", function () {
            const isMobile = window.innerWidth <= 1024;

            if (isMobile) {
                // Mobile / Tablet — slide-in overlay
                sidebar.classList.toggle("open");
                overlay.classList.toggle("active");
            } else {
                // Desktop — collapse/expand
                sidebar.classList.toggle("collapsed");
                if (content) content.classList.toggle("expanded");
            }
        });

        // Close sidebar when clicking overlay
        overlay.addEventListener("click", function () {
            sidebar.classList.remove("open");
            overlay.classList.remove("active");
        });

        // Fix sidebar state on window resize
        window.addEventListener("resize", function () {
            if (window.innerWidth > 1024) {
                sidebar.classList.remove("open");
                overlay.classList.remove("active");
            }
        });
    }

    /* =========================
       Password Show/Hide Toggle
    ========================== */
document.addEventListener("DOMContentLoaded", function () {

    document.querySelectorAll(".toggle-password-btn").forEach(button => {

        button.addEventListener("click", function () {

            const input = document.getElementById(this.dataset.target);

            if (!input) return;

            if (input.type === "password") {
                input.type = "text";
                this.textContent = "Hide";
            } else {
                input.type = "password";
                this.textContent = "Show";
            }

        });

    });

});

/* =========================
       Logout
    ========================== */
function logout(event) {
    event.preventDefault(); // stop <a> default behaviour
    localStorage.removeItem("userRole");
    localStorage.removeItem("userName");
    window.location.href = "../login.html";
}



 /* =========================
       Page Protection
    ========================== */
function protectPage(requiredRole) {
    const role = localStorage.getItem("userRole");

    if (!role || role !== requiredRole) {
        window.location.href = "../login.html";
    }
}



// ===== Welcome Message =====
const welcomeMessage = document.getElementById("welcomeMessage");

if (welcomeMessage) {

    let username = localStorage.getItem("username");

    if (username) {

        // Format each word (e.g., "kAVIYA c" → "Kaviya C")
        username = username
            .toLowerCase()
            .split(" ")
            .map(word => word.charAt(0).toUpperCase() + word.slice(1))
            .join(" ");

        welcomeMessage.textContent = `Welcome, ${username}!`;

    }
}