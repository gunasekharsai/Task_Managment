package com.taskmanagment.app.Dto;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;





 

 

 
// ─── Team ─────────────────────────────────────────────────────────────────────

 
// ─── Notification ─────────────────────────────────────────────────────────────
// @Data @Builder @NoArgsConstructor @AllArgsConstructor
// public class NotificationResponseDto {
//     private String id;
//     private Notification.Type type;
//     private String message;
//     private String referenceId;
//     private String referenceType;
//     private boolean read;
//     private LocalDateTime createdAt;
 
//     public static NotificationResponseDto from(Notification n) {
//         return NotificationResponseDto.builder()
//             .id(n.getId()).type(n.getType()).message(n.getMessage())
//             .referenceId(n.getReferenceId()).referenceType(n.getReferenceType())
//             .read(n.isRead()).createdAt(n.getCreatedAt()).build();
//     }
// }

@Data @Builder @NoArgsConstructor
 @AllArgsConstructor
public class Responses<T> {
    private boolean success;
    private String message;
    private T data;
 
    public static <T> Responses<T> ok(T data) {
        return Responses.<T>builder().success(true).data(data).build();
    }
 
    public static <T> Responses<T> ok(String message, T data) {
        return Responses.<T>builder().success(true).message(message).data(data).build();
    }
 
    public static <T> Responses<T> error(String message) {
        return Responses.<T>builder().success(false).message(message).build();
    }
}