import re

with open('src/main/java/com/donations/donations/controller/VerificationController.java', 'r') as f:
    content = f.read()

replacement = """
    @PostMapping("/me")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<VerificationRequest> submit(@RequestBody VerificationRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(submitVerificationUseCase.execute(getCreatorId(userDetails.getId()), request));
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        cancelVerificationUseCase.execute(getCreatorId(userDetails.getId()));
    }
}"""

# Fix the broken getCreatorId
broken = """    private UUID getCreatorId(UUID userId) {
        return creatorRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Creator not found"))
                .getId();
    
    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        cancelVerificationUseCase.execute(getCreatorId(userDetails.getId()));
    }
}"""

fixed_getCreatorId = """    private UUID getCreatorId(UUID userId) {
        return creatorRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Creator not found"))
                .getId();
    }"""

content = content.replace(broken, fixed_getCreatorId)

# Replace the last endpoint with both submit and cancel
last_endpoint = """    @PostMapping("/me")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<VerificationRequest> submit(@RequestBody VerificationRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(submitVerificationUseCase.execute(getCreatorId(userDetails.getId()), request));
    }
}"""

content = content.replace(last_endpoint, replacement.strip() + "\n")

with open('src/main/java/com/donations/donations/controller/VerificationController.java', 'w') as f:
    f.write(content)
