package com.inventory.dto;

import javax.validation.constraints.Size;

public class UserUpdateRequest {

    private String fullName;

    private String role; // ADMIN or USER

    private String status; // ACTIVE or INACTIVE

    @Size(min = 4, max = 100, message = "Password must be at least 4 characters if provided")
    private String password; // Optional: only update if provided

    public UserUpdateRequest() {
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
