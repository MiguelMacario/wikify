package com.wikify.services;

import com.wikify.dto.UserSummaryDTO;
import com.wikify.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public List<UserSummaryDTO> getUsers() {
        return userRepository.findAll(Sort.by("name")).stream()
                .map(UserSummaryDTO::from)
                .toList();
    }


}
