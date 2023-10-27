package seb39_40.coffeewithme.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import seb39_40.coffeewithme.exception.BusinessLogicException;
import seb39_40.coffeewithme.image.service.ImageService;
import seb39_40.coffeewithme.review.domain.Review;
import seb39_40.coffeewithme.review.repository.ReviewRepository;
import seb39_40.coffeewithme.user.domain.User;
import seb39_40.coffeewithme.user.domain.UserStatus;
import seb39_40.coffeewithme.user.dto.request.UserJoinRequestDto;
import seb39_40.coffeewithme.user.dto.request.UserUpdateRequestDto;
import seb39_40.coffeewithme.user.repository.UserRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ReviewRepository reviewRepository;
    private final ImageService imageService;

    public void createUser(UserJoinRequestDto joinDto) {
        verifyEmail(joinDto.getEmail());
        Long profilePhotoId = imageService.saveDefaultImage();
        User newUser = User.builder()
                .userName(joinDto.getUserName())
                .mobile(joinDto.getMobile())
                .email(joinDto.getEmail())
                .password(bCryptPasswordEncoder.encode(joinDto.getPassword()))
                .roles("ROLE_USER")
                .status(UserStatus.USER_SIGNUP)
                .registerDate(LocalDate.now())
                .build();
        newUser.setProfilePhoto(imageService.findById(profilePhotoId));
        userRepository.save(newUser);
    }

    public void withdrawUser(String email){
        User user = findByEmail(email);
        user.updateStatus(UserStatus.USER_WITHDRAW);
        user.getProfilePhoto().setUser(null);
        userRepository.save(user);
    }

    public User getInformation(String email) {
        User user = findByEmail(email);
        return user;
    }

    public User updateInformation(String email, UserUpdateRequestDto updateDto){
        User newUser = findByEmail(email);
        newUser.updateInformation(updateDto.getUserName(), updateDto.getMobile(),
                imageService.findById(updateDto.getProfilePhotoId()));
        return userRepository.save(newUser);
    }

    public List<Review> getReview(Long userId) {
        List<Review> reviewList = new ArrayList<>();
        if(reviewRepository.countByUserId(userId)==0)
            return reviewList;
        reviewList = reviewRepository.findAllByUserId(userId);
        return reviewList;
    }

    private void verifyEmail(String email){
        Optional<User> user=userRepository.findByEmail(email);
        if(user.isPresent()) {
            throw new BusinessLogicException(HttpStatus.CONFLICT, "이미 사용중인 이메일 입니다.");
        }
    }

    public User findById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> {
            throw new BusinessLogicException(HttpStatus.NOT_FOUND, "존재하지 않는 회원 ID입니다.");
        });
    }

    public User findByEmail(String username) {
        return userRepository.findByEmail(username).orElseThrow(() -> {
            throw new BusinessLogicException(HttpStatus.NOT_FOUND, "존재하지 않는 회원 EMAIL입니다.");
        });
    }
}
