/* =========================
   PAGE PROTECTION
========================= */
document.addEventListener("DOMContentLoaded", function () {

    protectPage("USER");


    const roadCategory = document.getElementById("roadCategory");
    const otherRoadCategoryBox = document.getElementById("otherRoadCategoryBox");

    roadCategory.addEventListener("change", function () {
        if (this.value === "OTHER") {
            otherRoadCategoryBox.style.display = "block";
        } else {
            otherRoadCategoryBox.style.display = "none";
        }
    });

});

/* =========================
   SPECIFY ISSUE (OTHER OPTION)
========================= */

const issueTypeSelect = document.getElementById("issueType");
const otherIssueBox = document.getElementById("otherIssueBox");
const otherIssueInput = document.getElementById("otherIssue");

if (issueTypeSelect && otherIssueBox) {

    issueTypeSelect.addEventListener("change", function () {

        if (this.value === "other") {

            otherIssueBox.style.display = "block";

        } else {

            otherIssueBox.style.display = "none";

            // clear value when switching away
            if (otherIssueInput)
                otherIssueInput.value = "";

        }

    });

}


/* =========================
   GLOBAL VARIABLES
========================= */

let currentLatitude = null;
let currentLongitude = null;

let map;
let marker = null;
let geocoder;

/* =========================
   ELEMENT REFERENCES
========================= */

const issueForm = document.getElementById("issueForm");
const locationInput = document.getElementById("locationInput");
const gpsBtn = document.getElementById("gpsBtn");

/* =========================
   RESET COORDINATES WHEN LOCATION CHANGES
========================= */

if (locationInput) {

    locationInput.addEventListener("input", function () {

        currentLatitude = null;
        currentLongitude = null;

    });

}


/* =========================
   IMAGE PREVIEW + REMOVE BUTTON
   (WORKS WITH YOUR HTML)
========================= */

const imageInput = document.getElementById("imageInput");
const uploadArea = document.querySelector(".upload-area");

if (imageInput && uploadArea) {

    imageInput.addEventListener("change", function () {

        const file = this.files[0];

        if (!file) return;

        const reader = new FileReader();

        reader.onload = function (e) {

            const uploadInner = uploadArea.querySelector(".upload-inner");

            uploadInner.innerHTML = `
                <div style="position:relative;width:100%;height:100%;">

                    <img src="${e.target.result}"
                         style="
                            width:100%;
                            height:100%;
                            object-fit:cover;
                            border-radius:8px;
                         ">

                    <button id="removeImageBtn"
                        type="button"
                        style="
                            position:absolute;
                            top:5px;
                            right:5px;
                            background:red;
                            color:white;
                            border:none;
                            border-radius:50%;
                            width:28px;
                            height:28px;
                            cursor:pointer;
                        ">×</button>

                </div>
            `;


            /* REMOVE BUTTON */

            document.getElementById("removeImageBtn")
                .addEventListener("click", function (e) {

                    e.stopPropagation();

                    imageInput.value = "";

                    uploadInner.innerHTML = `
                    <div class="upload-cloud">☁</div>
                    <div class="upload-text">
                        Upload Image
                    </div>
                `;

                });

        };

        reader.readAsDataURL(file);

    });

}

/* =========================
   ISSUE FORM SUBMIT
========================= */

if (issueForm) {

    issueForm.addEventListener("submit", async function (e) {

        e.preventDefault();

        const submitBtn = document.getElementById("submitBtn");
        const errorDiv = document.getElementById("errorMessage");

        submitBtn.disabled = true;
        submitBtn.innerText = "Submitting...";

        if (errorDiv) errorDiv.innerText = "";

        try {

            const issueType = document.getElementById("issueType").value;
            const otherIssue = document.getElementById("otherIssue").value.trim();
            const location = locationInput.value.trim();
            const description = document.getElementById("description").value;
            const roadCategory = document.getElementById("roadCategory").value;
            const damageSeverity = document.getElementById("damageSeverity").value;


            const imageFile = imageInput.files[0];

            /* VALIDATION */

            if (issueType === "other" && !otherIssue) {
                errorDiv.innerText = "Please specify the issue.";
                return;
            }

            if (!imageFile) {
                errorDiv.innerText = "Please upload an image.";
                return;
            }

            if (!location) {
                errorDiv.innerText = "Please select location.";
                return;
            }

            if (!roadCategory) {
                errorDiv.innerText = "Please select road category.";
                return;
            }

            const title = issueType === "other" ? otherIssue : issueType;


            /* GET COORDINATES */

            let latitude = currentLatitude;
            let longitude = currentLongitude;

            if (latitude === null || longitude === null) {

                const coords = await getCoordinatesFromAddress(location);

                if (!coords) {

                    errorDiv.innerText =
                        "Unable to find location. Please pin location again.";

                    currentLatitude = null;
                    currentLongitude = null;

                    return;
                }

                latitude = coords.lat;
                longitude = coords.lon;
            }


            if (imageFile.size > 2 * 1024 * 1024) {
                errorDiv.innerText = "Image size must be less than 2MB.";
                errorDiv.style.display = "block";
                return;
            }


            /* CREATE FORM DATA */

            const formData = new FormData();

            formData.append("title", title);
            formData.append("description", description);
            formData.append("place", location);
            formData.append("roadCategory", roadCategory);
            formData.append("damageSeverity", damageSeverity);
            formData.append("latitude", latitude);
            formData.append("longitude", longitude);
            formData.append("image", imageFile);


            /* TOKEN */

            const token = localStorage.getItem("token");

            if (!token) {

                alert("Session expired. Please login again.");
                window.location.href = "../login.html";
                return;
            }


            /* API CALL */

            const response = await fetch("http://localhost:8080/api/issues", {

                method: "POST",

                headers: {
                    Authorization: "Bearer " + token
                },

                body: formData

            });


            if (!response.ok) {
                const contentType = response.headers.get("content-type");

                if (response.status === 409 && contentType.includes("application/json")) {
                    const errorData = await response.json();

                    if (errorData.duplicate && errorData.issueId) {
                        // 🔥 OPEN DUPLICATE MODAL
                        showDuplicateModal(errorData.issueId);
                        return;
                    }
                }

                // Normal error handling
                const errorText = await response.text();
                alert(errorText);
                return;
            }


            /* SUCCESS */

            document.getElementById("successPopup").style.display = "flex";

            currentLatitude = null;
            currentLongitude = null;

            issueForm.reset();

            setTimeout(() => {

                window.location.href = "my-issues.html";

            }, 2000);

        }
        catch (error) {

            console.error(error);

            errorDiv.innerText = "Unexpected error occurred.";

        }
        finally {

            submitBtn.disabled = false;
            submitBtn.innerText = "SUBMIT";

        }

    });

}


function initGoogleServices() {
    if (!window.google || !google.maps) {
        console.error("Google Maps not loaded");
        return;
    }

    if (!geocoder) {
        geocoder = new google.maps.Geocoder();
    }
}


/* =========================
   GPS BUTTON
========================= */

if (gpsBtn && locationInput) {

    gpsBtn.innerText = "Use GPS";

    gpsBtn.addEventListener("click", function () {

        gpsBtn.innerText = "Getting location...";

        if (!navigator.geolocation) {
            alert("GPS not supported. Please enter location manually.");
            gpsBtn.innerText = "Use GPS";
            return;
        }

        navigator.geolocation.getCurrentPosition(

            function (position) {

                currentLatitude = position.coords.latitude;
                currentLongitude = position.coords.longitude;

                if (!geocoder) {
                    geocoder = new google.maps.Geocoder();
                }

                reverseGeocode(currentLatitude, currentLongitude);

                gpsBtn.innerText = "Use GPS";
            },

            function (error) {

                console.error(error);

                alert("Unable to fetch location. Please enter manually.");

                gpsBtn.innerText = "Use GPS";
            }

        );

    });

}




/* =========================
   REVERSE GEOCODE
========================= */

function reverseGeocode(lat, lon) {

    geocoder.geocode(
        { location: { lat: lat, lng: lon } },
        function (results, status) {

            if (status === "OK" && results[0]) {

                locationInput.value = results[0].formatted_address;

            }

        }
    );
}


/* =========================
   FORWARD GEOCODE
========================= */

async function getCoordinatesFromAddress(address) {

    initGoogleServices();

    return new Promise((resolve) => {

        geocoder.geocode(
            { address: address },
            function (results, status) {

                if (status === "OK") {

                    const loc = results[0].geometry.location;

                    resolve({
                        lat: loc.lat(),
                        lon: loc.lng()
                    });

                } else {
                    resolve(null);
                }

            }
        );

    });

}


document.getElementById("issueForm").addEventListener("submit", async function (e) {

    e.preventDefault();

    const address = locationInput.value.trim();

    if (!address) {
        alert("Please enter a location");
        return;
    }

    // If location already selected from map or GPS
    if (currentLatitude === null || currentLongitude === null) {
        const coords = await getCoordinatesFromAddress(address);

        if (!coords) {
            alert("Location not found");
            return;
        }

        currentLatitude = coords.lat;
        currentLongitude = coords.lon;
    }

    console.log("Latitude:", currentLatitude);
    console.log("Longitude:", currentLongitude);


});


/* =========================
      GLOBAL VARIABLES
========================== */

const BASE_URL = "http://localhost:8080";
const TOKEN = localStorage.getItem("token");

let CURRENT_DUPLICATE_ID = null;
let ORIGINAL_IMAGE_URL = "";
let SELECTED_IMAGE_FILE = null;
let CURRENT_IMAGE_URL = "";

/* =========================
       SHOW MODAL FUNCTION
========================== */

async function showDuplicateModal(issueId) {

    const duplicateModal = document.getElementById("duplicateModal");
    const upvoteCountSpan = document.getElementById("upvoteCount");
    const duplicateImage = document.getElementById("duplicateImage");
    const removeImageBtn = document.getElementById("removeImageBtn");
    const commentList = document.getElementById("duplicateCommentList");

    const errorBox = document.getElementById("duplicateErrorMessage");
    if (errorBox) {
        errorBox.style.display = "none";
    }

    try {

        CURRENT_DUPLICATE_ID = issueId;

        const response = await fetch(`${BASE_URL}/api/issues/${issueId}/details`, {
            method: "GET",
            headers: {
                "Authorization": "Bearer " + TOKEN
            }
        });


        if (!response.ok) {
            throw new Error("Failed to fetch issue details");
        }

        const data = await response.json();

        document.querySelector(".duplicate-actions").style.display = "flex";
        document.getElementById("addImageBtn").style.display = "flex";
        document.getElementById("doneBtn").style.display = "inline-block";

        /* Disable voting/comment if resolved */
        if (data.status === "RESOLVED") {

            // Hide voting & comment buttons
            document.querySelector(".duplicate-actions").style.display = "none";

            // Hide add image overlay
            document.getElementById("addImageBtn").style.display = "none";

            // Hide remove image button
            document.getElementById("removeImageBtn").style.display = "none";

            // Hide Done button
            document.getElementById("doneBtn").style.display = "none";

            document.querySelector(".duplicate-comments").style.display = "none";

            // Show information message
            const msg = document.createElement("p");
            msg.innerText = "This issue has been resolved. ";
            msg.style.color = "green";
            msg.style.fontWeight = "500";
            msg.style.marginBottom = "10px";

            document.querySelector(".duplicate-bottom").prepend(msg);
        }


        document.getElementById("duplicateIssueId").innerText =
            "Issue ID " + data.id.substring(0, 6);

        document.getElementById("duplicateTitle").innerText = data.title;
        document.getElementById("duplicateDescription").innerText = data.description || "No description provided.";
        document.getElementById("duplicateLocation").innerText = data.place;
        document.getElementById("duplicateCategory").innerText = data.roadCategory ? data.roadCategory.replace("_", " ") : "N/A";
        document.getElementById("duplicateCreatedAt").innerText = new Date(data.createdAt).toLocaleDateString();
        document.getElementById("duplicateSeverity").innerText = data.damageSeverity;
        document.getElementById("duplicateStatus").innerText = data.status.replace("_", " ");

        upvoteCountSpan.innerText = data.voteCount;

        /* =========================
           LOAD ORIGINAL ISSUE IMAGE 
        ========================== */

        if (data.images && data.images.length > 0) {

            let fileName = data.images[0];

            if (fileName.startsWith("/")) {
                fileName = fileName.substring(1);
            }

            if (fileName.startsWith("uploads/")) {
                ORIGINAL_IMAGE_URL = `${BASE_URL}/${fileName}`;
            } else {
                ORIGINAL_IMAGE_URL = `${BASE_URL}/uploads/${fileName}`;
            }

        } else {
            ORIGINAL_IMAGE_URL = "";
        }

        duplicateImage.src = ORIGINAL_IMAGE_URL;

        if (removeImageBtn) {
            removeImageBtn.style.display = "none";
        }

        /* Load comments */
        commentList.innerHTML = "";

        data.comments.forEach(comment => {

            const commentDiv = document.createElement("div");
            commentDiv.classList.add("comment-item");

            commentDiv.innerHTML = `
                <div>
                    <span class="comment-user">${comment.username}</span>
                    <span class="comment-time">
                        (${new Date(comment.createdAt).toLocaleString()})
                    </span>
                </div>
                <div>${comment.content}</div>
            `;

            commentList.appendChild(commentDiv);
        });

        duplicateModal.style.display = "flex";

    } catch (error) {
        console.error(error);
    }
}

/* =========================
   duplicate complaint 
========================== */

document.addEventListener("DOMContentLoaded", function () {

    const duplicateModal = document.getElementById("duplicateModal");
    const closeDuplicate = document.querySelector(".close-duplicate");

    const upvoteBtn = document.querySelector(".upvote-btn");
    const upvoteCountSpan = document.getElementById("upvoteCount");

    const commentBtn = document.querySelector(".comment-btn");
    const commentInputBox = document.getElementById("commentInputBox");
    const postCommentBtn = document.getElementById("postCommentBtn");
    const newCommentInput = document.getElementById("newCommentInput");

    const addImageBtn = document.getElementById("addImageBtn");
    const duplicateImageInput = document.getElementById("duplicateImageInput");

    const duplicateImage = document.getElementById("duplicateImage");
    const removeImageBtn = document.getElementById("removeImageBtn");


    duplicateImageInput.addEventListener("change", async function (e) {

        const file = e.target.files[0];
        if (!file) return;

        SELECTED_IMAGE_FILE = file;

           // optional preview BEFORE upload
             const previewUrl = URL.createObjectURL(file);
             duplicateImage.src = previewUrl;

             const formData = new FormData();
             formData.append("image", file);

        try {
            const res = await fetch(
                `${BASE_URL}/api/issues/${CURRENT_DUPLICATE_ID}/images`, {
                method: "POST",
                headers: {
                    "Authorization": "Bearer " + TOKEN
                },
                body: formData
            });

            const data = await res.json();

            let fileName = data.imageUrl || data.fileName || data.url;

             if (!fileName) {
                console.error("Invalid upload response:", data);
                 return;
            }

            if (fileName.startsWith("/")) {
                fileName = fileName.substring(1);
            }

            if (fileName.startsWith("uploads/")) {
                CURRENT_IMAGE_URL = `${BASE_URL}/${fileName}`;
            } else {
                CURRENT_IMAGE_URL = `${BASE_URL}/uploads/${fileName}`;
            }

            // replace preview with real image
            duplicateImage.src = CURRENT_IMAGE_URL;
            URL.revokeObjectURL(previewUrl);

            if (removeImageBtn) {
                removeImageBtn.style.display = "block";
            }

        } catch (error) {
            console.error(error);
        }
    });

   
    /* =========================
       CLOSE MODAL
    ========================== */

    if (closeDuplicate) {
        closeDuplicate.addEventListener("click", function () {
            duplicateModal.style.display = "none";
        });
    }

    /* =========================
       COMMENT BUTTON TOGGLE
    ========================== */

    if (commentBtn) {
        commentBtn.addEventListener("click", function () {
            commentInputBox.style.display =
                commentInputBox.style.display === "flex" ? "none" : "flex";
        });
    }

    /* =========================
       UPVOTE
    ========================== */

    if (upvoteBtn) {
        upvoteBtn.addEventListener("click", async function () {

            if (!CURRENT_DUPLICATE_ID) return;

            try {
                const response = await fetch(
                    `${BASE_URL}/api/issues/${CURRENT_DUPLICATE_ID}/upvote`,
                    {
                        method: "POST",
                        headers: { "Authorization": "Bearer " + TOKEN }
                    }
                );

                if (response.ok) {
                    const result = await response.json();
                    upvoteCountSpan.innerText = result.voteCount;
                }

            } catch (error) {
                console.error(error);
            }
        });
    }

    /* =========================
       POST COMMENT
    ========================== */

    if (postCommentBtn) {
        postCommentBtn.addEventListener("click", async function () {

            const content = newCommentInput.value.trim();
            if (!content || !CURRENT_DUPLICATE_ID) return;

            try {
                const response = await fetch(
                    `${BASE_URL}/api/issues/${CURRENT_DUPLICATE_ID}/comment`,
                    {
                        method: "POST",
                        headers: {
                            "Authorization": "Bearer " + TOKEN,
                            "Content-Type": "application/json"
                        },
                        body: JSON.stringify({ 
                            content, 
                            imageUrl: CURRENT_IMAGE_URL 
                        })
                    }
                );

                if (response.ok) {

                    const newComment = await response.json();

                    const commentDiv = document.createElement("div");
                    commentDiv.classList.add("comment-item");

                    commentDiv.innerHTML = `
                       <div>
                           <span class="comment-user">${newComment.username}</span>
                           <span class="comment-time">
                                (${new Date(newComment.createdAt).toLocaleString()})
                           </span>
                      </div>
                    <div>${newComment.content}</div>
                    `;

                   document.getElementById("duplicateCommentList").appendChild(commentDiv);

                   newCommentInput.value = "";

                   CURRENT_IMAGE_URL = "";
                   removeImageBtn.style.display = "none";
                }

            } catch (error) {
                console.error(error);
            }
        });
    }

    /* =========================
       IMAGE PREVIEW + REMOVE
    ========================== */

    if (addImageBtn) {
        addImageBtn.addEventListener("click", function () {
            duplicateImageInput.click();
        });
    }

    if (removeImageBtn) {
        removeImageBtn.addEventListener("click", function () {

             duplicateImage.src = ORIGINAL_IMAGE_URL;
             duplicateImageInput.value = "";

             SELECTED_IMAGE_FILE = null;
             CURRENT_IMAGE_URL = ""; 

    removeImageBtn.style.display = "none";
        });
    }

    /* =========================
       DONE BUTTON
    ========================== */

    const doneBtn = document.getElementById("doneBtn");

    if (doneBtn) {
        doneBtn.addEventListener("click", async function () {

            try {

                if (SELECTED_IMAGE_FILE && CURRENT_DUPLICATE_ID) {

                    const formData = new FormData();
                    formData.append("image", SELECTED_IMAGE_FILE);

                    const response = await fetch(
                        `${BASE_URL}/api/issues/${CURRENT_DUPLICATE_ID}/images`,
                        {
                            method: "POST",
                            headers: {
                                "Authorization": "Bearer " + TOKEN
                            },
                            body: formData
                        }
                    );

                    if (!response.ok) {

                        let errorMessage = "Failed to submit issue.";

                        const contentType = response.headers.get("content-type");

                        if (contentType && contentType.includes("application/json")) {
                            const errorData = await response.json();
                            errorMessage = errorData.error || errorMessage;
                        }

                        const errorDiv = document.getElementById("duplicateErrorMessage");
                        errorDiv.innerText = errorMessage;
                        errorDiv.style.display = "block";
                        return;
                    }
                }

                SELECTED_IMAGE_FILE = null;
                duplicateModal.style.display = "none";

            } catch (error) {
                console.error("Done button error:", error);
            }
        });
    }

});