package seb39_40.coffeewithme.wishlist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import seb39_40.coffeewithme.wishlist.domain.Wishlist;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findAllByUserId(Long userId);
    Optional<Wishlist> findByUserIdAndCafeId(Long userId, Long cafeId);
    Long countByUserId(Long userId);
}
