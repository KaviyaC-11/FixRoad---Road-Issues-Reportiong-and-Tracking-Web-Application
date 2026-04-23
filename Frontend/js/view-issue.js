/* =====================================================
   VIEW ISSUE PAGE SCRIPT
   Handles:
   - Loading issue details from backend
   - Displaying issue information
   - Image preview and navigation
   - Assigning repair team
   - Updating issue status
===================================================== */

document.addEventListener("DOMContentLoaded", loadIssueDetails);


/* =====================================================
   LOAD ISSUE DETAILS
   Fetch issue data using issue ID from URL
===================================================== */
async function loadIssueDetails(){

    try{

        // Extract issue ID from query parameter
        const params = new URLSearchParams(window.location.search);
        const issueId = params.get("id");

        const token = localStorage.getItem("token");

        // Request issue details from backend
        const response = await fetch(
            `http://localhost:8080/api/issues/${issueId}/details`,
            {
                headers:{
                    "Authorization":"Bearer " + token
                }
            }
        );

        const issue = await response.json();

        // Populate UI with issue data
        populateIssue(issue);
        renderComments(issue)

    }
    catch(error){

        console.error("Failed to load issue details", error);

    }

}


/* =====================================================
   IMAGE STATE VARIABLES
   Stores issue images and currently displayed image
===================================================== */
let issueImages = [];
let currentImageIndex = 0;


/* =====================================================
   POPULATE ISSUE DATA
   Fills issue details into the UI
===================================================== */
function populateIssue(issue){

    // Display shortened issue ID
   

    const image = document.querySelector(".issue-main-image");

    /* ---------------------------------
       LOAD ISSUE IMAGES
    ---------------------------------- */
    if(issue.imageUrls && issue.imageUrls.length > 0){

        issueImages = issue.imageUrls;
        currentImageIndex = 0;

        // Display first image
        image.src = `http://localhost:8080/uploads/${issueImages[currentImageIndex]}`;

    }

    /* ---------------------------------
       SHOW / HIDE IMAGE NAVIGATION
       Hide buttons if only one image
    ---------------------------------- */
    if(issueImages.length <= 1){
        prevBtn.style.display = "none";
        nextBtn.style.display = "none";
    } else {
        prevBtn.style.display = "inline-block";
        nextBtn.style.display = "inline-block";
    }

    console.log(issue);


    /* ---------------------------------
       ISSUE SUMMARY INFORMATION
    ---------------------------------- */

    document.getElementById("issueIdBadge").innerText =  `Issue ID: ${issue.id.substring(0,11)}`;

    document.getElementById("priorityText").innerText = issue.priority;

    document.getElementById("issueTypeTitle").innerText = issue.title;

    document.getElementById("issueUser").innerText =
        issue.userName || "Citizen";

    document.getElementById("issueLocation").innerText =
        issue.place;

    document.getElementById("issueDate").innerText =
        new Date(issue.createdAt).toLocaleDateString();

    document.getElementById("issueStatus").innerText =
        issue.status.replace("_"," ");

        // Set dropdown to current issue status
    document.getElementById("statusSelect").value = issue.status;
    
    console.log(issue);

    /* ---------------------------------
       REPAIR TEAM INFORMATION
       Handles first assignment vs editing
    ---------------------------------- */

    if (issue.status === "RESOLVED") {

        document.getElementById("editRepairBtn").style.display = "none";
        document.getElementById("saveRepairBtn").style.display = "none";

        const msg = document.getElementById("repairAssignMessage");
        if (msg) {
            msg.innerText = "Issue is resolved. Repair team cannot be edited.";
            msg.style.color = "red";
        }
    }

    if(issue.repairTeamName){

        // Populate existing repair team details
        document.getElementById("repairTeamName").value = issue.repairTeamName;
        document.getElementById("repairContactNumber").value = issue.repairContactNumber;

        // Lock inputs after assignment
        document.getElementById("repairTeamName").disabled = true;
        document.getElementById("repairContactNumber").disabled = true;

        // Show edit button
        document.getElementById("editRepairBtn").style.display = "inline-block";
        document.getElementById("saveRepairBtn").style.display = "none";

    }else{

        // First-time assignment
        document.getElementById("repairTeamName").disabled = false;
        document.getElementById("repairContactNumber").disabled = false;

        document.getElementById("editRepairBtn").style.display = "none";
        document.getElementById("saveRepairBtn").style.display = "inline-block";

    }

    if (issue.status === "RESOLVED") {

    document.getElementById("updateStatusBtn").disabled = true;
    document.getElementById("statusSelect").disabled = true;

    const msg = document.getElementById("statusUpdateMessage");
    if (msg) {
           msg.innerText = "Issue is already resolved";
           msg.style.color = "red";
        }
    }

}

 /* ---------------------------------
       Comments
    ---------------------------------- */


    function renderComments(issue) {

    const container = document.getElementById("commentsContainer");
    container.innerHTML = "";

    // Description as first comment
    if (issue.description) {
        const div = document.createElement("div");
        div.classList.add("comment-item");

        div.innerHTML = `
            <p>${issue.description}</p>
            <small>Reported by ${issue.userName}</small>
        `;

        container.appendChild(div);
    }

    if (issue.comments && issue.comments.length > 0) {
    // Actual comments
    issue.comments.forEach(comment => {

        const div = document.createElement("div");
        div.classList.add("comment-item");

        div.innerHTML = `
            <p>${comment.content}</p>
            <small>
                ${comment.username} • 
                ${new Date(comment.createdAt).toLocaleString()}
            </small>
        `;

        container.appendChild(div);
    });

}
    }


/* =====================================================
   IMAGE LIGHTBOX PREVIEW
   Allows users to view the image in full screen
===================================================== */

const preview = document.getElementById("imagePreview");
const previewImg = document.getElementById("previewImg");
const issueImg = document.querySelector(".preview-image");
const closeBtn = document.querySelector(".close-preview");

if(issueImg){

    // Open preview when image is clicked
    issueImg.addEventListener("click", function(){

        preview.style.display = "flex";
        previewImg.src = this.src;

    });

    // Close preview using close button
    closeBtn.addEventListener("click", function(){

        preview.style.display = "none";

    });

    // Close preview when clicking outside image
    preview.addEventListener("click", function(e){

        if(e.target === preview){
            preview.style.display = "none";
        }

    });

}


/* =====================================================
   ASSIGN REPAIR TEAM
   Sends repair team details to backend
===================================================== */
async function assignRepairTeam() {

    const loader = document.getElementById("repairLoader");
    const btnText = document.getElementById("repairBtnText");
    const btn = document.getElementById("saveRepairBtn");

    // Show loading state
    btn.disabled = true;
    loader.style.display = "inline-block";
    btnText.innerText = "Saving";

    const params = new URLSearchParams(window.location.search);
    const issueId = params.get("id");

    const repairTeamName = document.getElementById("repairTeamName").value;
    const repairContactNumber = document.getElementById("repairContactNumber").value;

    const token = localStorage.getItem("token");

    const response = await fetch(`http://localhost:8080/api/issues/${issueId}/assign-repair`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify({
            repairTeamName,
            repairContactNumber
        })
    });

    // Reset button state
    loader.style.display = "none";
    btnText.innerText = "Save";
    btn.disabled = false;

    const msg = document.getElementById("repairAssignMessage");

    if (response.ok) {

        // Lock fields after assignment
        document.getElementById("repairTeamName").disabled = true;
        document.getElementById("repairContactNumber").disabled = true;

        document.getElementById("editRepairBtn").style.display = "inline-block";
        document.getElementById("saveRepairBtn").style.display = "none";

        msg.innerText = "Repair team assigned successfully";
        msg.style.color = "green";

    } else {

         const errorText = await response.text();

        msg.innerText = "Failed to assign repair team";
        msg.style.color = "red";
    }
}


/* =====================================================
   ENABLE EDIT MODE FOR REPAIR TEAM
===================================================== */
function enableEditRepair() {

    const status = document
        .getElementById("issueStatus")
        ?.innerText
        ?.trim()
        ?.toUpperCase();

    const msg = document.getElementById("repairAssignMessage");
    const editBtn = document.getElementById("editRepairBtn");
    const saveBtn = document.getElementById("saveRepairBtn");
    const teamInput = document.getElementById("repairTeamName");
    const contactInput = document.getElementById("repairContactNumber");

    if (status === "RESOLVED") {

        if (msg) {
            msg.innerText = "Cannot edit repair team after issue is resolved";
            msg.style.color = "red";
        }

        if (teamInput) teamInput.disabled = true;
        if (contactInput) contactInput.disabled = true;

        if (editBtn) editBtn.style.display = "none";
        if (saveBtn) saveBtn.style.display = "none";

        return;
    }

    if (msg) msg.innerText = "";

    if (teamInput) teamInput.disabled = true;
    if (contactInput) contactInput.disabled = true;

    if (editBtn) editBtn.style.display = "inline-block";
    if (saveBtn) saveBtn.style.display = "none";
}


/* =====================================================
   CLICK EDIT BUTTON
===================================================== */
function onClickEditRepair() {

    const status = document
        .getElementById("issueStatus")
        ?.innerText
        ?.trim()
        ?.toUpperCase();

    if (status === "RESOLVED") {
        document.getElementById("repairAssignMessage").innerText =
            "Cannot edit after issue is resolved";
        return;
    }

    document.getElementById("repairTeamName").disabled = false;
    document.getElementById("repairContactNumber").disabled = false;

    document.getElementById("editRepairBtn").style.display = "none";
    document.getElementById("saveRepairBtn").style.display = "inline-block";
}


/* =====================================================
   UPDATE ISSUE STATUS
===================================================== */
async function updateStatus(){

    const currentStatusText = document.getElementById("issueStatus").innerText.trim().toUpperCase();

    if (currentStatusText === "RESOLVED") {
        document.getElementById("statusUpdateMessage").innerText =
            "Issue already resolved";
        return;
    }

    const loader = document.getElementById("statusLoader");
    const btnText = document.getElementById("statusBtnText");
    const btn = document.getElementById("updateStatusBtn");

    btn.disabled = true;
    loader.style.display = "inline-block";
    btnText.innerText = "Updating";

    const params = new URLSearchParams(window.location.search);
    const issueId = params.get("id");

    const status = document.getElementById("statusSelect").value; // IN_PROGRESS / RESOLVED
    const token = localStorage.getItem("token");

    let response;

    try {
        response = await fetch(
            `http://localhost:8080/api/issues/${issueId}/status`,
            {
                method: "PATCH",
                headers:{
                    "Content-Type":"application/json",
                    "Authorization":"Bearer " + token
                },
                body: JSON.stringify({ status })
            }
        );
    } catch (error) {
        document.getElementById("statusUpdateMessage").innerText =
            "Network error. Try again.";
        loader.style.display = "none";
        btnText.innerText = "Update Status";
        btn.disabled = false;
        return;
    }

    // Reset button state
    loader.style.display = "none";
    btnText.innerText = "Update Status";
    btn.disabled = false;

    if(response.ok){

        // 🔥 THE LINE YOU KEPT FORGETTING
        currentIssueStatus = status;

        if (status === "RESOLVED") {
            document.getElementById("updateStatusBtn").disabled = true;
            document.getElementById("statusSelect").disabled = true;
        }

        // Update UI text
        document.getElementById("issueStatus").innerText =
            status.replace("_"," ");

        document.getElementById("statusUpdateMessage").innerText =
            "Status updated successfully";

        // Re-evaluate UI based on new status
        enableEditRepair();

    } else {
        if (response.status === 400) {
            document.getElementById("statusUpdateMessage").innerText =
                "Invalid status change";
        } else {
            document.getElementById("statusUpdateMessage").innerText =
                "Failed to update status";
        }
    }
}


/* =====================================================
   IMAGE NAVIGATION (PREV / NEXT)
===================================================== */

const prevBtn = document.getElementById("prevImageBtn");
const nextBtn = document.getElementById("nextImageBtn");


function showImage(index){

    const image = document.querySelector(".issue-main-image");

    image.src = `http://localhost:8080/uploads/${issueImages[index]}`;
}


if(prevBtn){

    prevBtn.addEventListener("click", function(){

        if(issueImages.length === 0) return;

        currentImageIndex--;

        if(currentImageIndex < 0){
            currentImageIndex = issueImages.length - 1;
        }

        showImage(currentImageIndex);

    });

}


if(nextBtn){

    nextBtn.addEventListener("click", function(){

        if(issueImages.length === 0) return;

        currentImageIndex++;

        if(currentImageIndex >= issueImages.length){
            currentImageIndex = 0;
        }

        showImage(currentImageIndex);

    });

}

