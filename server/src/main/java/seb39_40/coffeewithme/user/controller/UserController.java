package seb39_40.coffeewithme.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import seb39_40.coffeewithme.security.userdetails.CustomUserDetails;
import seb39_40.coffeewithme.review.domain.Review;
import seb39_40.coffeewithme.user.domain.User;
import seb39_40.coffeewithme.user.dto.request.UserUpdateRequestDto;
import seb39_40.coffeewithme.user.dto.response.UserInfoResponseDto;
import seb39_40.coffeewithme.user.mapper.UserMapper;
import seb39_40.coffeewithme.user.service.UserService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping("/withdraw")
    public ResponseEntity withdrawUser(@AuthenticationPrincipal CustomUserDetails userDetails){
        userService.withdrawUser(userDetails.getUsername());
        log.info("** Success Withdraw [{}]",userDetails.getUsername());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/token")
    public ResponseEntity reissuanceToken(@AuthenticationPrincipal CustomUserDetails userDetails){
        log.info("** Success Reissuance [{}] Token",userDetails.getUsername());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/information")
    public ResponseEntity getUserInformation(@AuthenticationPrincipal CustomUserDetails userDetails){
        log.info("** Get [{}] Information",userDetails.getUsername());
        User user = userService.getInformation(userDetails.getUsername());
        return new ResponseEntity<>(UserInfoResponseDto.from(user), HttpStatus.OK);
    }

    @PatchMapping("/information")
    public ResponseEntity updateUserInformation(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                @Valid @RequestBody UserUpdateRequestDto updateDto){
        log.info("** Patch [{}] Information",userDetails.getUsername());
        User result = userService.updateInformation(userDetails.getUsername(), updateDto);
        return new ResponseEntity<>(UserInfoResponseDto.from(result), HttpStatus.OK);
    }

    @GetMapping("/reviews")
    public ResponseEntity getUserReviews(@AuthenticationPrincipal CustomUserDetails userDetails){
        List<Review> reviewList = userService.getReview(userDetails.getUser().getId());
        log.info("** Get Reviews [{}]",userDetails.getUsername());
        return new ResponseEntity<>(userMapper.reviewsToUserReviewResponseDto(reviewList),HttpStatus.OK);
    }
}
