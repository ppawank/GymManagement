package com.gym.management.enums;

public enum UserRole {
    ADMIN, // Full access - can manage users, verify payments, delete members
    STAFF, // Can add members, mark attendance, record payments (needs verification)
    VIEW_ONLY // Read-only access to all data
}
