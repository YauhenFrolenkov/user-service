package com.innowise.user.specification;

import com.innowise.user.entity.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    private UserSpecification() {
    }

    public static Specification<User> hasFirstName(String firstName) {
        return (root, query, criteriaBuilder) ->
                firstName == null ? null :
                        criteriaBuilder.equal(root.get("name"), firstName);
    }

    public static Specification<User> hasLastName(String lastName) {
        return (root, query, criteriaBuilder) ->
                lastName == null ? null :
                        criteriaBuilder.equal(root.get("surname"), lastName);
    }

    public static Specification<User> isActive() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isTrue(root.get("active"));
    }
}
