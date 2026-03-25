package com.anju.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ObjectAuthorizationService {

    private final SecondaryVerificationService secondaryVerificationService;

    public boolean canAccessResource(Object resource, Long userId, String role) {
        if (resource == null) {
            return false;
        }

        if ("ADMIN".equals(role)) {
            return true;
        }

        return checkResourceOwnership(resource, userId);
    }

    public boolean canModifyResource(Object resource, Long userId, String role) {
        if (resource == null) {
            return false;
        }

        if ("ADMIN".equals(role)) {
            return true;
        }

        return checkResourceOwnership(resource, userId);
    }

    public boolean hasAnyRole(String userRole, String... allowedRoles) {
        if (userRole == null || allowedRoles == null) {
            return false;
        }
        for (String role : allowedRoles) {
            if (userRole.equals(role)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasRole(UserPrincipal principal, String role) {
        if (principal == null || role == null) {
            return false;
        }
        return principal.hasRole(role);
    }

    public boolean isOwnerOrAdmin(Object resource, Long userId, String role) {
        if ("ADMIN".equals(role)) {
            return true;
        }
        return checkResourceOwnership(resource, userId);
    }

    private boolean checkResourceOwnership(Object resource, Long userId) {
        try {
            java.lang.reflect.Field ownerField = findOwnerField(resource.getClass());
            if (ownerField != null) {
                ownerField.setAccessible(true);
                Object ownerId = ownerField.get(resource);
                if (ownerId != null && ownerId.equals(userId)) {
                    return true;
                }
            }

            java.lang.reflect.Field uploadedByField = findField(resource.getClass(), "uploadedBy");
            if (uploadedByField != null) {
                uploadedByField.setAccessible(true);
                Object uploadedBy = uploadedByField.get(resource);
                if (uploadedBy != null && uploadedBy.equals(userId)) {
                    return true;
                }
            }

            java.lang.reflect.Field operatorIdField = findField(resource.getClass(), "operatorId");
            if (operatorIdField != null) {
                operatorIdField.setAccessible(true);
                Object operatorId = operatorIdField.get(resource);
                if (operatorId != null && operatorId.equals(userId)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private java.lang.reflect.Field findOwnerField(Class<?> clazz) {
        try {
            return clazz.getDeclaredField("ownerId");
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    private java.lang.reflect.Field findField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() != null) {
                return findField(clazz.getSuperclass(), fieldName);
            }
            return null;
        }
    }
}
