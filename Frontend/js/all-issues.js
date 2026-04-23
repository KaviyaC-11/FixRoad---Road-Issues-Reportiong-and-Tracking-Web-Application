/* =====================================================
   ALL ISSUES PAGE SCRIPT
   Handles:
   - Fetching issues from backend
   - Rendering issues in table
   - Filtering issues by status, priority and type
===================================================== */

let allIssues = [];


/* =====================================================
   INITIAL PAGE LOAD
   Runs when the page is fully loaded
===================================================== */
document.addEventListener("DOMContentLoaded", () => {

    // Fetch issues from backend
    loadIssues();

    // Setup filter dropdown listeners
    initializeFilters();

});


/* =====================================================
   LOAD ISSUES FROM BACKEND
   Retrieves all issues using authenticated API request
===================================================== */
async function loadIssues() {

    try {

        // Retrieve stored authentication token
        const token = localStorage.getItem("token");

        // Fetch issues from backend API
        const response = await fetch("http://localhost:8080/api/issues", {
            headers: {
                "Authorization": "Bearer " + token
            }
        });

        const data = await response.json();

        /*
        Backend response may return issues in different formats
        depending on pagination or API structure.
        This safely extracts the issues list.
        */
        allIssues = data.content || data.issues || data;

        // Render all issues in table
        renderIssues(allIssues);

    } 
    catch (error) {

        console.error("Failed to load issues", error);

    }

}


/* =====================================================
   RENDER ISSUES IN TABLE
   Dynamically builds table rows for each issue
===================================================== */
function renderIssues(issues) {

    const tableBody = document.getElementById("issuesTableBody");

    // Clear existing rows before rendering
    tableBody.innerHTML = "";

    issues.forEach(issue => {

        const row = document.createElement("tr");

        // Store issue status as data attribute for possible styling
        row.setAttribute("data-status", issue.status);

        row.innerHTML = `

        <!-- Short issue ID (first 8 characters for readability) -->
        <td>#${issue.id ? issue.id.substring(0,8) : "-"}</td>

        <!-- Reporter name -->
        <td>${issue.userName || "Citizen"}</td>

        <!-- Issue title -->
        <td>${issue.title}</td>

        <!-- Location / place of issue -->
        <td>${issue.place}</td>

        <!-- Calculated priority level -->
        <td>${issue.priority}</td>

        <!-- Issue status (formatted for display) -->
        <td class="status-text">${issue.status.replace("_"," ")}</td>

        <!-- View issue details -->
        <td>
            <a href="view-issue.html?id=${issue.id}" class="btn-dark">
                View
            </a>
        </td>

        `;

        tableBody.appendChild(row);

    });

}


/* =====================================================
   FILTER SYSTEM INITIALIZATION
   Attaches event listeners to filter dropdowns
===================================================== */
function initializeFilters(){

    const statusFilter = document.getElementById("filterStatus");
    const priorityFilter = document.getElementById("filterPriority");
    const typeFilter = document.getElementById("filterType");
    const resetBtn = document.getElementById("resetFilter");

    // Apply filters when dropdown values change
    statusFilter.addEventListener("change", filterIssues);
    priorityFilter.addEventListener("change", filterIssues);
    typeFilter.addEventListener("change", filterIssues);

    // Reset all filters and show full issue list
    resetBtn.addEventListener("click", () => {

        statusFilter.value = "";
        priorityFilter.value = "";
        typeFilter.value = "";

        renderIssues(allIssues);

    });

}


/* =====================================================
   APPLY FILTERS
   Filters issues based on selected dropdown values
===================================================== */
function filterIssues() {

    const status = document.getElementById("filterStatus").value;
    const priority = document.getElementById("filterPriority").value;
    const type = document.getElementById("filterType").value;

    const filtered = allIssues.filter(issue => {

        /*
        Backend stores status as ENUM format (e.g., IN_PROGRESS).
        Convert dropdown value to same format for comparison.
        */
        const formattedFilterStatus = status
            ? status.toUpperCase().replace(" ", "_")
            : "";

        const matchStatus = !formattedFilterStatus || issue.status === formattedFilterStatus;
        const matchPriority = !priority || issue.priority === priority;
        const matchType = !type || issue.title === type;

        return matchStatus && matchPriority && matchType;

    });

    // Render filtered results
    renderIssues(filtered);

}