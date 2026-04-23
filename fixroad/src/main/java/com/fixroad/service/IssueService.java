package com.fixroad.service;

import com.fixroad.dto.AssignRepairRequest;
import com.fixroad.dto.CommentResponse;
import com.fixroad.dto.IssueDetailResponse;
import com.fixroad.dto.IssueRequest;
import com.fixroad.dto.IssueResponse;
import com.fixroad.model.Comment;
import com.fixroad.model.Issue;
import com.fixroad.model.IssueImage;
import com.fixroad.model.IssueStatus;
import com.fixroad.model.RoadCategory;
import com.fixroad.model.User;
import com.fixroad.repository.UserRepository;
import com.fixroad.repository.CommentRepository;
import com.fixroad.repository.IssueImageRepository;
import com.fixroad.repository.IssueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fixroad.model.Upvote;
import com.fixroad.repository.UpvoteRepository;
import com.fixroad.exception.DuplicateIssueException;
import com.fixroad.exception.ResourceNotFoundException;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fixroad.service.priority.PriorityScoringService;
import com.fixroad.service.status.StatusTransitionService;
import com.fixroad.service.duplicate.DuplicateDetectionService;
import com.fixroad.dto.DuplicateResultDTO;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.lang.NonNull;

@Service
public class IssueService {

    // ==============================
    // REPOSITORIES AND SERVICES
    // ==============================

    @Autowired
    private IssueRepository issueRepository;
    @Autowired
    private UpvoteRepository upvoteRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private StatusHistoryService statusHistoryService;

    private final DuplicateDetectionService duplicateDetectionService;

    private final UserRepository userRepository;

    private final PriorityScoringService priorityScoringService;

    private final StatusTransitionService statusTransitionService;


    // ==============================
    // CONSTRUCTOR DEPENDENCY INJECTION
    // ==============================

    public IssueService(IssueRepository issueRepository,
                    UserRepository userRepository,
                    DuplicateDetectionService duplicateDetectionService,
                    PriorityScoringService priorityScoringService,
                    StatusTransitionService statusTransitionService                   
                    ) {

        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
        this.duplicateDetectionService = duplicateDetectionService;
        this.priorityScoringService = priorityScoringService;
        this.statusTransitionService = statusTransitionService;
                    }
   

    // ==============================
    // CREATE ISSUE 
    // ==============================

    public IssueResponse createIssue(IssueRequest request) {

        Issue issue = new Issue();

        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription());
        issue.setPlace(request.getPlace());
        issue.setDamageSeverity(request.getDamageSeverity());
        issue.setLatitude(request.getLatitude());
        issue.setLongitude(request.getLongitude());
        issue.setRoadCategory(request.getRoadCategory());
        issue.setVoteCount(0);
        issue.setCreatedAt(LocalDateTime.now());
        issue.setUpdatedAt(LocalDateTime.now());
        issue.setStatus(IssueStatus.REPORTED);
    

   
        // GET LOGGED-IN USER
       Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
       String email = authentication.getName();

       User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

       issue.setUser(user);
   

       // DUPLICATE ISSUE DETECTION
       DuplicateResultDTO duplicateResult = duplicateDetectionService.checkDuplicate(
            request.getTitle(),
            request.getDescription(),
            request.getLatitude(),
            request.getLongitude()
       );

       if (duplicateResult.isDuplicate()) {
          throw new DuplicateIssueException(
                "Duplicate issue detected",
                duplicateResult.getScore(),
                duplicateResult.getMatchedIssue().getId()
           );
        }


        // SAVE ISSUE
        Issue savedIssue = issueRepository.save(issue);

        

       // SAVE STATUS HISTORY
       statusHistoryService.saveHistory(
            savedIssue,
            null,
            savedIssue.getStatus().name(),
            user
        );


       // BUILD ISSUE RESPONSE
       IssueResponse response = new IssueResponse();
       response.setId(savedIssue.getId());
       response.setTitle(savedIssue.getTitle());
       response.setDescription(savedIssue.getDescription());
       response.setPlace(savedIssue.getPlace());
       response.setDamageSeverity(savedIssue.getDamageSeverity());
       response.setLatitude(savedIssue.getLatitude());
       response.setLongitude(savedIssue.getLongitude());
       response.setRoadCategory(savedIssue.getRoadCategory());
       response.setVoteCount(savedIssue.getVoteCount());
       response.setStatus(savedIssue.getStatus());
       response.setCreatedAt(savedIssue.getCreatedAt());
       response.setComments(getCommentsForIssue(issue));
       response.setDescription(issue.getDescription());

       return response;
    }


    // ==============================
    // ISSUE IMAGE REPOSITORY
    // ==============================

     @Autowired
     private IssueImageRepository issueImageRepository;

 
    // ==============================
    // CREATE ISSUE WITH IMAGE
    // ==============================

    public IssueResponse createIssueWithImage(
        String title,
        String description,
        String place,
        RoadCategory roadCategory,
        String damageSeverity,
        Double latitude,
        Double longitude,
        MultipartFile image
    ) 
    {

            Issue issue = new Issue();

            issue.setTitle(title);
            issue.setDescription(description != null ? description : "");
            issue.setPlace(place);
            issue.setDamageSeverity(String.valueOf(damageSeverity));
            issue.setLatitude(latitude);
            issue.setLongitude(longitude);
            issue.setRoadCategory(roadCategory);


    
        // GET LOGGED-IN USER-----------------------------------------
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
          .orElseThrow(() -> new RuntimeException("User not found"));

        issue.setUser(user);


        // DUPLICATE ISSUE DETECTION-----------------------------------------
         DuplicateResultDTO duplicateResult = duplicateDetectionService.checkDuplicate(
                title,
                description != null ? description : "",
                latitude,
                longitude
            );

        if (duplicateResult.isDuplicate()) {
            throw new DuplicateIssueException(
                 "Duplicate issue detected",
                 duplicateResult.getScore(),
                 duplicateResult.getMatchedIssue().getId()
            );
        }


         // SAVE ISSUE------------------------------------------------------
         Issue savedIssue = issueRepository.save(issue);

         emailService.sendIssueReportedEmail(
            user.getEmail(),
            issue.getTitle(),
            issue.getPlace()
        );


        statusHistoryService.saveHistory(
           savedIssue,
           null,
           savedIssue.getStatus().name(),
           user
        );


         // IMAGE UPLOAD AND STORAGE-----------------------------------------
         if (image != null && !image.isEmpty()) {

                try {

                     String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
                     Path path = Paths.get("uploads/" + fileName);
                     Files.copy(image.getInputStream(), path);

                     IssueImage issueImage = new IssueImage();

                     issueImage.setIssue(savedIssue);
                     issueImage.setImageUrl(fileName);

                     issueImageRepository.save(issueImage);

                } catch (Exception e) {
                        e.printStackTrace();
                    }
            }


         // BUILD ISSUE RESPONSE
  

        IssueResponse response = new IssueResponse();

        response.setId(savedIssue.getId());
        response.setTitle(savedIssue.getTitle());
        response.setDescription(savedIssue.getDescription());
        response.setPlace(savedIssue.getPlace());
        response.setRoadCategory(savedIssue.getRoadCategory());
        response.setDamageSeverity(savedIssue.getDamageSeverity());
        response.setLatitude(savedIssue.getLatitude());
        response.setLongitude(savedIssue.getLongitude());
        response.setVoteCount(savedIssue.getVoteCount());
        response.setStatus(savedIssue.getStatus());
        response.setCreatedAt(savedIssue.getCreatedAt());

        return response;
    } 


    // ==============================
    // ENTITY → RESPONSE CONVERSION
    // ==============================

    private IssueResponse convertToResponse(Issue issue) {

        IssueResponse response = new IssueResponse();

        response.setId(issue.getId());
        response.setTitle(issue.getTitle());
        response.setDescription(issue.getDescription());
        response.setPlace(issue.getPlace());
        response.setRoadCategory(issue.getRoadCategory());
        response.setDamageSeverity(issue.getDamageSeverity());
        response.setLatitude(issue.getLatitude());
        response.setLongitude(issue.getLongitude());
        response.setStatus(issue.getStatus());
        response.setCreatedAt(issue.getCreatedAt());
        response.setRepairTeamName(issue.getRepairTeamName());  
        response.setRepairContactNumber(issue.getRepairContactNumber());

        return response;
    } 


    // ==============================
    // GET ISSUES BY USER EMAIL
    // ==============================

    public List<IssueResponse> getIssuesByUserEmail(String email) {

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        List<Issue> issues = issueRepository.findByUser(user);

        return issues.stream()
            .map(this::convertToResponse)
            .toList();
    }


    // ==============================
    // ISSUE UPVOTING
    // ==============================

    @Transactional
    public void upvoteIssue(UUID issueId) {

        if (issueId == null) {
            throw new IllegalArgumentException("Issue ID cannot be null");
        }

         Issue issue = issueRepository.findById(issueId)
            .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (upvoteRepository.existsByIssueAndUser(issue, user)) {
            throw new IllegalStateException("You have already upvoted this issue.");
        }

        Upvote upvote = new Upvote();
        upvote.setIssue(issue);
        upvote.setUser(user);

        upvoteRepository.save(upvote);

        issue.setVoteCount(issue.getVoteCount() + 1);
        issueRepository.save(issue);
    }


    // ==============================
    // GET ISSUE VOTE COUNT
    // ==============================

    public int getVoteCount(UUID issueId) {

        if (issueId == null) {
            throw new IllegalArgumentException("Issue ID cannot be null");
        }

        Issue issue = issueRepository.findById(issueId)
            .orElseThrow(() -> new RuntimeException("Issue not found"));

        return issue.getVoteCount();
    }


    // ==============================
    // ADD COMMENT TO ISSUE
    // ==============================

    @Transactional
    public CommentResponse addComment(UUID issueId, String content) {

        issueId = java.util.Objects.requireNonNull(issueId, "Issue ID cannot be null");

        Issue issue = issueRepository.findById(issueId)
            .orElseThrow(() -> new RuntimeException("Issue not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = new Comment();
        comment.setIssue(issue);
        comment.setUser(user);
        comment.setContent(content);

        commentRepository.save(comment);

        return new CommentResponse(
        user.getName(),
        comment.getContent(),
        comment.getCreatedAt()
);
    }

    public List<CommentResponse> getCommentsForIssue(Issue issue) {

        List<Comment> comments =
            commentRepository.findByIssueOrderByCreatedAtDesc(issue);

        return comments.stream()
            .map(c -> new CommentResponse(
                    c.getUser().getName(), // make sure this exists
                    c.getContent(),
                    c.getCreatedAt()
            ))
            .toList();
    }

    // ==============================
    // ADD IMAGE TO EXISTING ISSUE
    // ==============================

    @Transactional
    public String addImageToExistingIssue(UUID issueId, MultipartFile image) {

        issueId = java.util.Objects.requireNonNull(issueId, "Issue ID cannot be null");

        Issue issue = issueRepository.findById(issueId)
            .orElseThrow(() -> new RuntimeException("Issue not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (image == null || image.isEmpty()) {
           throw new IllegalArgumentException("Image file is empty");
        }

        
        try {

            String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();

            Path uploadPath = Paths.get("uploads");

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);

            Files.copy(
                image.getInputStream(),
                filePath,
                StandardCopyOption.REPLACE_EXISTING
            );

            IssueImage issueImage = new IssueImage();
            issueImage.setIssue(issue);
            issueImage.setUploadedBy(user);
            issueImage.setImageUrl(fileName);

            issueImageRepository.save(issueImage);

            return "uploads/" + fileName;


        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to upload image: " + e.getMessage());
        }
    }


    // ==============================
    // GET FULL ISSUE DETAILS
    // ==============================

    public IssueDetailResponse getIssueDetails(UUID issueId) {

        issueId = java.util.Objects.requireNonNull(issueId, "Issue ID cannot be null");

        Issue issue = issueRepository.findById(issueId)
            .orElseThrow(() -> new RuntimeException("Issue not found"));

            String userName = issue.getUser().getName();


        // FETCH ISSUE IMAGES
        List<IssueImage> imageEntities =
            issueImageRepository.findByIssue(issue);

        List<String> imageUrls = imageEntities.stream()
            .map(IssueImage::getImageUrl)
            .toList();


       // FETCH ISSUE COMMENTS
        List<Comment> commentEntities =
            commentRepository.findByIssueOrderByCreatedAtDesc(issue);

        List<CommentResponse> comments = commentEntities.stream()
            .map(c -> new CommentResponse(
                    c.getUser().getName(),
                    c.getContent(),
                    c.getCreatedAt()
            ))
            .toList();


        // CALCULATE PRIORITY SCORE
            double priorityScore = priorityScoringService.calculatePriorityScore(issue);
            String priority = priorityScoringService.getPriorityLevel(priorityScore);


        // BUILD DETAIL RESPONSE
            return new IssueDetailResponse(
                issue.getId(),
                issue.getTitle(),
                issue.getDescription(),
                issue.getPlace(),
                userName,
                priority,
                issue.getRoadCategory(),
                issue.getDamageSeverity(),
                issue.getStatus(),
                issue.getVoteCount(),
                issue.getCreatedAt(),
                imageUrls,
                imageUrls, comments,
                issue.getRepairTeamName(),
                issue.getRepairContactNumber()
            );
    }


    // ==============================
    // DASHBOARD STATISTICS
    // ==============================

    public Map<String, Object> getDashboardStats() {

        Map<String, Object> stats = new HashMap<>();

        long totalIssues = issueRepository.count();

        long reported = issueRepository.countByStatus(IssueStatus.REPORTED);
        long inProgress = issueRepository.countByStatus(IssueStatus.IN_PROGRESS);
        long resolved = issueRepository.countByStatus(IssueStatus.RESOLVED);

        List<Map<String, Object>> recentIssues =
            issueRepository.findTop5ByOrderByCreatedAtDesc()
            .stream()
            .map(issue -> {

            double score = priorityScoringService.calculatePriorityScore(issue);
            String priority = priorityScoringService.getPriorityLevel(score);

            Map<String, Object> map = new HashMap<>();
            map.put("id", issue.getId());
            map.put("title", issue.getTitle());
            map.put("place", issue.getPlace());
            map.put("createdAt", issue.getCreatedAt());
            map.put("status", issue.getStatus());
            map.put("priority", priority);

            return map;

        }).toList();

        stats.put("totalIssues", totalIssues);
        stats.put("reported", reported);
        stats.put("inProgress", inProgress);
        stats.put("resolved", resolved);
        stats.put("recentIssues", recentIssues);

        return stats;
    }


    // ==============================
    // GET ALL ISSUES (ADMIN VIEW)
    // ==============================

    public List<IssueResponse> getAllIssues() {

        List<Issue> issues = issueRepository.findAll();

        return issues.stream()
            .map(issue -> {

                String userName = issue.getUser().getName();

                UUID imageId = null;

                List<IssueImage> images = issueImageRepository.findByIssue(issue);

                if(images != null && !images.isEmpty()){
                    imageId = images.get(0).getId();
                }

                double score = priorityScoringService.calculatePriorityScore(issue);
                String priority = priorityScoringService.getPriorityLevel(score);

                return new IssueResponse(
                        issue.getId(),
                        issue.getTitle(),
                        issue.getDescription(),
                        issue.getPlace(),
                        issue.getRoadCategory(),
                        issue.getDamageSeverity(),
                        issue.getLatitude(),
                        issue.getLongitude(),
                        issue.getVoteCount(),
                        issue.getStatus(),
                        issue.getCreatedAt(),
                        userName,
                        imageId,
                        priority,
                        issue.getRepairTeamName(),
                        issue.getRepairContactNumber()

                        
                );

            })
            .toList();
    }


    // ==============================
    // ASSIGN REPAIR TEAM
    // ==============================

    public void assignRepairTeam(@NonNull UUID issueId, AssignRepairRequest request) {

         Issue issue = issueRepository.findById(issueId)
            .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));

        if (issue.getStatus() == IssueStatus.RESOLVED) {
            throw new IllegalStateException("Cannot assign repair team after issue is resolved");
        }

        boolean isFirstAssignment =
            issue.getRepairTeamName() == null || issue.getRepairTeamName().isBlank();

        String oldTeam = issue.getRepairTeamName();
        String oldContact = issue.getRepairContactNumber();

        issue.setRepairTeamName(request.getRepairTeamName());
        issue.setRepairContactNumber(request.getRepairContactNumber());

        issueRepository.save(issue);

        if (isFirstAssignment) {
          statusHistoryService.saveHistory(
          issue,
          issue.getStatus().name(),
          "ASSIGNED",
          issue.getUser()
          );
        } else {
            statusHistoryService.saveHistory(
            issue,
            issue.getStatus().name(),
            "UPDATED",
            issue.getUser()
            );
        }

        if(isFirstAssignment){

            emailService.sendRepairAssignedEmail(
                issue.getUser().getEmail(),
                issue.getTitle(),
                issue.getPlace(),
                request.getRepairTeamName(),
                request.getRepairContactNumber()
            );

        }else{

            emailService.sendRepairUpdatedEmail(
                issue.getUser().getEmail(),
                issue.getTitle(),
                issue.getPlace(),
                oldTeam,
                oldContact,
                request.getRepairTeamName(),
                request.getRepairContactNumber()
            );

        }
    }   


    // ==============================
    // UPDATE ISSUE STATUS
    // ==============================

    @Transactional
    public void updateIssueStatus(@NonNull UUID issueId, IssueStatus status) {

        Issue issue = issueRepository.findById(issueId)
            .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));

        IssueStatus oldStatus = issue.getStatus();

        // 🚨 VALIDATION HERE
        if (!statusTransitionService.isValidStatusTransition(oldStatus, status)) {
            throw new IllegalStateException(
                "Invalid status transition from " + oldStatus + " to " + status
            );
        }

        issue.setStatus(status);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User admin = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if(status == IssueStatus.RESOLVED && issue.getResolvedAt() == null){

            issue.setResolvedAt(LocalDateTime.now());

            emailService.sendIssueResolvedEmail(
                issue.getUser().getEmail(),
                issue.getTitle(),
                issue.getPlace()
            );
        }

        issueRepository.save(issue);

        statusHistoryService.saveHistory(
            issue,
            oldStatus != null ? oldStatus.name() : null,
            status.name(),
            admin
        );
    }
}