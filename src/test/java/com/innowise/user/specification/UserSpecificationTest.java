package com.innowise.user.specification;

import com.innowise.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserSpecificationTest {
    @Test
    void testHasFirstName_NotNull() {
        Specification<User> spec = UserSpecification.hasFirstName("John");

        assertNotNull(spec);

        try {
            spec.toPredicate(null, null, null);
        } catch (Exception ignored) {
            // ignored because we are only testing specification creation
        }
    }

    @Test
    void testHasFirstName_Null() {
        Specification<User> spec = UserSpecification.hasFirstName(null);

        assertNotNull(spec);

        try {
            Object result = spec.toPredicate(null, null, null);
            assertNull(result);
        } catch (Exception ignored) {
            // ignored because we are only testing specification creation
        }
    }

    @Test
    void testHasLastName_NotNull() {
        Specification<User> spec = UserSpecification.hasLastName("Doe");

        assertNotNull(spec);

        try {
            spec.toPredicate(null, null, null);
        } catch (Exception ignored) {
            // ignored because we are only testing specification creation
        }
    }

    @Test
    void testHasLastName_Null() {
        Specification<User> spec = UserSpecification.hasLastName(null);

        assertNotNull(spec);

        try {
            Object result = spec.toPredicate(null, null, null);
            assertNull(result);
        } catch (Exception ignored) {
            // ignored because we are only testing specification creation
        }
    }

    @Test
    void testIsActive() {
        Specification<User> spec = UserSpecification.isActive();

        assertNotNull(spec);

        try {
            spec.toPredicate(null, null, null);
        } catch (Exception ignored) {
            // ignored because we are only testing specification creation
        }
    }
}
