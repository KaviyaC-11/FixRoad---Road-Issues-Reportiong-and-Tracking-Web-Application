/* =====================================================
   MY ISSUES PAGE SCRIPT
   Displays issues reported by the currently logged-in user
   Handles:
   - Fetching user's issues from backend
   - Displaying issues in a table
   - Showing message if no issues exist
===================================================== */

document.addEventListener("DOMContentLoaded", async function () {

    // Table body where issues will be rendered
    const tbody = document.getElementById("issuesTableBody");

    // Message shown when user has no issues
    const noMsg = document.getElementById("noIssuesMessage");

    // Container holding the issues table
    const tableContainer = document.getElementById("issuesTableContainer");


    /* =====================================================
       INITIAL DATA LOAD
       Fetch issues reported by the current user
    ===================================================== */
    await loadMyIssues();



    /* =====================================================
       LOAD USER ISSUES FROM BACKEND
       Sends authenticated request to retrieve user's issues
    ===================================================== */
    async function loadMyIssues() {

        try {

            // Retrieve authentication token
            const token = localStorage.getItem("token");

            // Call backend API for user's issues
            const response = await fetch("http://localhost:8080/api/issues/my", {
                method: "GET",
                headers: {
                    "Authorization": "Bearer " + token,
                    "Content-Type": "application/json"
                }
            });

            if (!response.ok) {
                throw new Error("Failed to fetch issues");
            }

            const issues = await response.json();

            // Display retrieved issues in table
            displayIssues(issues);

        }
        catch (error) {

            console.error("Error:", error);

            // Show error message if loading fails
            noMsg.innerText = "Failed to load issues.";
            noMsg.style.display = "block";
            tableContainer.style.display = "none";
        }
    }



    /* =====================================================
       DISPLAY USER ISSUES
       Renders issues in table or shows message if empty
    ===================================================== */
    function displayIssues(issues) {

        // Clear any existing rows before rendering
        tbody.innerHTML = "";

        /*
        If no issues exist, hide table and show message
        */
        if (!issues || issues.length === 0) {

            tableContainer.style.display = "none";
            noMsg.style.display = "block";
            return;
        }

        // Show table when issues are available
        tableContainer.style.display = "block";
        noMsg.style.display = "none";

        issues.forEach(issue => {

            /*
            Format creation date for display.
            Uses DD/MM/YYYY format for readability.
            */
            const formattedDate = issue.createdAt
                ? new Date(issue.createdAt).toLocaleDateString("en-GB")
                : "-";

            const row = `
                <tr>
                    <!-- Issue title -->
                    <td>${issue.title}</td>

                    <!-- Location where issue was reported -->
                    <td>${issue.place}</td>

                    <!-- Date the issue was created -->
                    <td>${formattedDate}</td>

                    <!-- Current issue status (formatted for readability) -->
                    <td class="status-text">${issue.status.replace(/_/g, ' ').replace(/\w\S*/g, w => w.charAt(0) + w.slice(1).toLowerCase())}</td>

                    <!-- Link to track issue status -->
                    <td>
                        <a href="track-status.html?issueId=${issue.id}">View</a>
                    </td>
                </tr>
            `;

            // Append row to table
            tbody.innerHTML += row;
        });
    }

});