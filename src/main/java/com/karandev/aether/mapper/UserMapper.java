package com.karandev.aether.mapper;

import com.karandev.aether.dto.auth.SignupRequest;
import com.karandev.aether.dto.auth.UserProfileResponse;
import com.karandev.aether.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUserEntityFromSignupRequest(SignupRequest signupRequest);
    UserProfileResponse toUserProfileResponseFromUserEntity(User user);
}
