/* =====================================================
   DASHBOARD PAGE SCRIPT
   Handles:
   - Loading dashboard statistics
   - Displaying recent issues in the dashboard table
===================================================== */


/* =====================================================
   INITIAL PAGE LOAD
   Fetch dashboard data when the page loads
===================================================== */
document.addEventListener("DOMContentLoaded", loadDashboard);



/* =====================================================
   LOAD DASHBOARD DATA
   Retrieves summary statistics and recent issues
   from the backend dashboard API
===================================================== */
async function loadDashboard() {

    try {

        // Retrieve authentication token from browser storage
        const token = localStorage.getItem("token");

        // Request dashboard data from backend
        const response = await fetch("http://localhost:8080/api/issues/dashboard",{
            headers:{
                "Authorization":"Bearer " + token
            }
        });

        const data = await response.json();

        /*
        Populate dashboard statistics
        These values represent counts of issues by status
        */
        document.getElementById("totalIssues").innerText = data.totalIssues;
        document.getElementById("reportedIssues").innerText = data.reported;
        document.getElementById("inProgressIssues").innerText = data.inProgress;
        document.getElementById("resolvedIssues").innerText = data.resolved;

        // Load recent issues into dashboard table
        loadRecentIssues(data.recentIssues);

    } 
    catch(error){

        console.error("Dashboard load failed",error);

    }
}



/* =====================================================
   LOAD RECENT ISSUES
   Displays the latest reported issues in dashboard
   table for quick access
===================================================== */
function loadRecentIssues(issues){

    const table = document.getElementById("recentIssuesTable");

    // Clear any existing rows before rendering
    table.innerHTML = "";

    issues.forEach(issue => {

        const row = document.createElement("tr");

        row.innerHTML = `

            <!-- Shortened issue ID for readability -->
            <td>#${issue.id.substring(0,8)}</td>

            <!-- Issue title -->
            <td>${issue.title}</td>

            <!-- Location where issue was reported -->
            <td>${issue.place || "Unknown"}</td>

            <!-- Date the issue was created -->
            <td>${new Date(issue.createdAt).toLocaleDateString()}</td>

            <!-- Calculated priority level -->
            <td>${issue.priority}</td>

            <!-- Current issue status (underscores become line breaks for mobile) -->
            <td>${issue.status.replace(/_/g, '<br>')}</td>

            <!-- Link to detailed issue view -->
            <td>
                <a href="view-issue.html?id=${issue.id}" class="btn-dark">
                    View
                </a>
            </td>

        `;

        table.appendChild(row);

    });

}