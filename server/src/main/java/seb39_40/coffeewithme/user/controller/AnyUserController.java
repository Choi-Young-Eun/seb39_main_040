package seb39_40.coffeewithme.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import seb39_40.coffeewithme.user.dto.request.UserJoinRequestDto;
import seb39_40.coffeewithme.user.service.UserService;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class AnyUserController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity joinUser(@Valid @RequestBody UserJoinRequestDto joinDto){
        userService.createUser(joinDto);
        log.info("** Success Signup [{}]",joinDto.getEmail());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
