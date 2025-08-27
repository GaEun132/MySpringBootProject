package com.rookies4.myspringboot.service;

import com.rookies4.myspringboot.controller.dto.UserDTO;
import com.rookies4.myspringboot.entity.UserEntity;
import com.rookies4.myspringboot.exception.BusinessException;
import com.rookies4.myspringboot.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

        private final UserRepository userRepository;
        //User 등록
        @Transactional
        public UserDTO.UserResponse createUser(UserDTO.UserCreateRequest request) {
            //Email 중복검사 -> 중복되면 BusinessException을 발생시키고 종료
            userRepository.findByEmail(request.getEmail())
                    .ifPresent(entity -> {
                        throw new BusinessException("User with this Email already Exist", HttpStatus.CONFLICT);
                    } );
            //중복이 아니면 DTO -> Entity로 변환
            UserEntity entity = request.toEntity();
            UserEntity savedEntity = userRepository.save(entity);
            //Entity -> DTO로 변환후 리턴
            return new UserDTO.UserResponse(savedEntity);

        }

        //Id로 User 조회하기
        public UserDTO.UserResponse getUserById(Long id){
            UserEntity userEntity = getUserExist(id);
            return new UserDTO.UserResponse(userEntity);
        }

        //User 목록 조회하기
        public List<UserDTO.UserResponse> getAllUsers() {
            //List<UserEntity> -> List<UserDTO.UserResponse>
            //Level1
            /*
            return userRepository.findAll()//List<UserEntity>
                    .stream() //Stream<UserEntity>
                    .map(entity -> new UserDTO.UserResponse(entity)) //Stream<UserDTO.UserResponse>
                    .collect(Collectors.toList()); //List<UserDTO.UserResponse>
            */
            //Level2
            return userRepository.findAll()
                    .stream()
                    .map(UserDTO.UserResponse::new)
                    .toList();
        }

        //User 수정
        @Transactional
        public UserDTO.UserResponse updateUser(String email,
                                               UserDTO.UserUpdateRequest request) {
            UserEntity existUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BusinessException("User Not Found", HttpStatus.NOT_FOUND));
            //dirty read 변경 감지: setter만 호출하고, save()는 호출할 필요 없다.
            existUser.setName(request.getName());
            return new UserDTO.UserResponse(existUser);

        }

        //User 삭제
        @Transactional
        public void deleteUser(Long id){
            UserEntity userEntity = getUserExist(id);
            userRepository.delete(userEntity);
        }


        //내부 Helper Method
        private UserEntity getUserExist(Long id){
            return userRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("User Not Found", HttpStatus.NOT_FOUND));
        }



    }
