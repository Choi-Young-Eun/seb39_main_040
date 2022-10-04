package seb39_40.coffeewithme.user.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import seb39_40.coffeewithme.cafe.domain.Cafe;
import seb39_40.coffeewithme.cafe.mapper.CafeMapper;
import seb39_40.coffeewithme.exception.BusinessLogicException;
import seb39_40.coffeewithme.jwt.CustomUserDetails;
import seb39_40.coffeewithme.review.domain.Review;
import seb39_40.coffeewithme.user.domain.User;
import seb39_40.coffeewithme.user.dto.UserRequestDto;
import seb39_40.coffeewithme.user.mapper.UserMapper;
import seb39_40.coffeewithme.user.service.LikeService;
import seb39_40.coffeewithme.user.service.UserService;

import java.io.IOException;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final LikeService likeService;
    private final UserMapper userMapper;
    private final CafeMapper cafeMapper;

    @PostMapping("/signup")
    public ResponseEntity joinUser(@RequestBody UserRequestDto.UserJoin join){
        User user = userMapper.userJoinToUser(join);
        try {
            userService.createUser(user);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }catch (BusinessLogicException e){
            return new ResponseEntity<>(e.getErrorResponse(),e.getErrorResponse().getStatus());
        }
    }

    @PostMapping("/login")
    public ResponseEntity loginUser(@AuthenticationPrincipal CustomUserDetails userDetails){
        try {
            userService.verifyUser(userDetails.getUsername());
            System.out.println("** Success Login : " + userDetails.getUsername());
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (BusinessLogicException e){
            return new ResponseEntity<>(e.getErrorResponse(),e.getErrorResponse().getStatus());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity logoutUser(@AuthenticationPrincipal CustomUserDetails userDetails){
        userService.logoutUser(userDetails.getUsername());
        System.out.println("** Success Logout : "+userDetails.getUsername());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/withdraw")
    public ResponseEntity withdrawUser(@AuthenticationPrincipal CustomUserDetails userDetails){
        userService.withdrawUser(userDetails.getUsername());
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @PatchMapping("/token")
    public ResponseEntity reissuanceToken(){
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @GetMapping("/information")
    public ResponseEntity getUserInformation(@AuthenticationPrincipal CustomUserDetails userDetails){
        try {
            System.out.println("** Get User Information : " + userDetails.getUsername());
            User user = userService.getInformation(userDetails.getUsername());
            return new ResponseEntity<>(userMapper.userToUserInfo(user), HttpStatus.OK);
        }catch (BusinessLogicException e){
            return new ResponseEntity<>(e.getErrorResponse(),e.getErrorResponse().getStatus());
        }
    }

    @PatchMapping("/information")
    public ResponseEntity updateUserInformation(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                @RequestBody UserRequestDto.UserUpdate userRequestDto){
        try{
            User user = userMapper.userUpdateDtoToUser(userRequestDto);
            User result = userService.updateInformation(userDetails.getUsername(), user);
            return new ResponseEntity<>(userMapper.userToUserInfo(result),HttpStatus.OK);
        }catch (BusinessLogicException e){
            return new ResponseEntity<>(e.getErrorResponse(),e.getErrorResponse().getStatus());
        }
    }

    @PostMapping("/wishlist/{cafeId}")
    public ResponseEntity setLike(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @PathVariable("cafeId") Long cafeId){
        try {
            likeService.addLike(userDetails.getUser().getId(), cafeId);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }catch (BusinessLogicException e){
            return new ResponseEntity<>(e.getErrorResponse(),e.getErrorResponse().getStatus());
        }
    }

    @GetMapping("/wishlist")
    public ResponseEntity getLikes(@AuthenticationPrincipal CustomUserDetails userDetails){
        List<Cafe> cafes = likeService.getLike(userDetails.getUser().getId());
        return new ResponseEntity<>(userMapper.cafesToWishlistDto(cafeMapper.cafeListToCafeSimpleDto(cafes)),HttpStatus.OK);
    }

    @DeleteMapping("/wishlist/{cafeId}")
    public ResponseEntity deleteLike(@AuthenticationPrincipal CustomUserDetails userDetails,
                                     @PathVariable("cafeId") Long cafeId){
        try {
            likeService.deleteLike(userDetails.getUser().getId(), cafeId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (BusinessLogicException e) {
            return new ResponseEntity<>(e.getErrorResponse(), e.getErrorResponse().getStatus());
        }
    }

    @GetMapping("/reviews")
    public ResponseEntity getUserReviews(@AuthenticationPrincipal CustomUserDetails userDetails){
        System.out.println("** Get User Review : "+userDetails.getUsername());
        List<Review> reviewList = userService.getReview(userDetails.getUser().getId());
        return new ResponseEntity<>(userMapper.reviewsToUserReviewResponseDto(reviewList),HttpStatus.OK);
    }
}
