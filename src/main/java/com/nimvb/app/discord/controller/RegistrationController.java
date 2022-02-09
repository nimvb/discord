package com.nimvb.app.discord.controller;


import com.nimvb.app.discord.request.UserRegistrationRequest;
import com.nimvb.app.discord.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class RegistrationController {

    private final UserService userService;

    @PostMapping(value = "/",consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(code = HttpStatus.CREATED)
    public Mono<Void> register(@RequestBody @Valid UserRegistrationRequest request){
        return userService.create(request);
    }

//    @GetMapping("/")
//    public Mono<String> message(@AuthenticationPrincipal Authentication authentication){
//
//        return Mono.deferContextual(contextView -> {
//            return ReactiveSecurityContextHolder.getContext();
//                })
//                .map(securityContext -> {
//                    String s = securityContext.getAuthentication().getPrincipal().toString();
//                    return s;
//                })
//                .map(s -> s + " ((-: Heloo :-))")
//                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
//
//    }
}
