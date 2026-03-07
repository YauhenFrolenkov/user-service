package com.innowise.user.repository;

import com.innowise.user.entity.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long> {

    List<PaymentCard> findByUserId(Long userId);

    @Query("SELECT c FROM PaymentCard c WHERE c.active = true")
    List<PaymentCard> findAllActiveCards();

    @Query(value = "SELECT * FROM payment_cards WHERE number = :number", nativeQuery = true)
    Optional<PaymentCard> findByNumberNative(String number);

    int countByUserId(Long userId);

}

