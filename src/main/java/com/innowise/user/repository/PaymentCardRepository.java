package com.innowise.user.repository;

import com.innowise.user.entity.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long> {

    List<PaymentCard> findByUserId(Long userId);

    @Query("SELECT c FROM PaymentCard c WHERE c.active = true")
    List<PaymentCard> findAllActiveCards();

    @Query(value = "SELECT * FROM payment_cards WHERE number = :number AND active = true", nativeQuery = true)
    Optional<PaymentCard> findByNumberNative(@Param("number") String number);

    int countByUserIdAndActiveTrue(Long userId);

    @Modifying
    @Query(value = "UPDATE payment_cards SET active = true WHERE user_id = :userId", nativeQuery = true)
    void activateCardsByUserId(Long userId);

    @Modifying
    @Query(value = "UPDATE payment_cards SET active = false WHERE user_id = :userId", nativeQuery = true)
    void deactivateCardsByUserId(Long userId);

    @Query(value = "SELECT * FROM payment_cards WHERE id = :id", nativeQuery = true)
    Optional<PaymentCard> findByIdIncludingInactive(@Param("id") Long id);

}

