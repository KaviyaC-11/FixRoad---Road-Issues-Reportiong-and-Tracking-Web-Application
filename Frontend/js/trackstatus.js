// ===============================
// STATUS MESSAGE MAPPING
// ===============================
function getStatusMessage(status){

    if(!status) return "Status updated.";

    status = status.trim().toUpperCase();

    switch(status){

        case "REPORTED":
            return "Your issue has been reported and is awaiting review.";

        case "ASSIGNED":
            return "The issue has been assigned to the repair team.";

        case "UPDATED":
            return "The repair team has been changed. Please check the E-mail  for details";
           

        case "IN_PROGRESS":
            return "Repair work is currently in progress.";

        case "RESOLVED":
            return "The reported road issue has been fixed. Thank you for helping improve road safety.";

        default:
            return "Status updated.";
    }
}


// ===============================
// GET ISSUE ID FROM URL
// ===============================
const params = new URLSearchParams(window.location.search);
const issueId = params.get("issueId");

console.log("IssueId:", issueId);

const timeline = document.getElementById("timeline");


// ===============================
// LOAD STATUS HISTORY
// ===============================
async function loadStatusHistory() {

    // Stop if issueId missing
    if(!issueId || issueId === "null"){

        console.error("Invalid issueId:", issueId);

        timeline.innerHTML = `
            <p style="color:red;">Unable to load issue timeline.</p>
        `;

        return;
    }

    try {

        const response = await fetch(
            `http://localhost:8080/api/status-history/${issueId}`
        );

        // If API failed
        if(!response.ok){

            console.error("Server returned error:", response.status);

            timeline.innerHTML = `
                <p style="color:red;">Failed to load status history.</p>
            `;

            return;
        }

        const history = await response.json();

        // Validate response
        if(!Array.isArray(history)){

            console.error("Invalid response:", history);

            timeline.innerHTML = `
                <p style="color:red;">Invalid timeline data received.</p>
            `;

            return;
        }

        // Clear previous content
        timeline.innerHTML = "";

        // If no history found
        if(history.length === 0){

            timeline.innerHTML = `
                <p>No status updates available.</p>
            `;

            return;
        }

        // Render timeline
        history.forEach(item => {

            const div = document.createElement("div");
            div.className = "timeline-item";

            const status = item.newStatus.replaceAll("_"," ");

            const message = getStatusMessage(item.newStatus);

            const time = new Date(item.changedAt)
                .toLocaleString("en-IN", {
                    dateStyle: "medium",
                    timeStyle: "short"
                });

            div.innerHTML = `
                <div class="timeline-status">${status}</div>
                <div class="timeline-message">${message}</div>
                <div class="timeline-time">${time}</div>
            `;

            timeline.appendChild(div);

        });

    } catch (error) {

        console.error("Error loading status history:", error);

        timeline.innerHTML = `
            <p style="color:red;">Error loading timeline.</p>
        `;
    }
}


// ===============================
// INITIALIZE PAGE
// ===============================
document.addEventListener("DOMContentLoaded", loadStatusHistory);