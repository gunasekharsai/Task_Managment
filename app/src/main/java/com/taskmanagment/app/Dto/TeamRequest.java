package com.taskmanagment.app.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class TeamRequest {
 
    @Data
    public static class Create {
        @NotBlank(message = "Team name is required")
        @Size(min = 2, max = 100)
        private String name;
 
        @Size(max = 500)
        private String description;
    }
 
    @Data
    public static class Update {
        @Size(min = 2, max = 100)
        private String name;
 
        @Size(max = 500)
        private String description;
    }
 
    @Data
    public static class Invite {
        @NotBlank(message = "Invitee email is required")
        @Email(message = "Must be a valid email")
        private String email;
    }
}
