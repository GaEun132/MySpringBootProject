package com.rookies4.myspringboot.service;

import com.rookies4.myspringboot.controller.dto.StudentDTO;
import com.rookies4.myspringboot.entity.Student;
import com.rookies4.myspringboot.entity.StudentDetail;
import com.rookies4.myspringboot.exception.BusinessException;
import com.rookies4.myspringboot.exception.advice.ErrorCode;
import com.rookies4.myspringboot.repository.StudentDetailRepository;
import com.rookies4.myspringboot.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {
    private final StudentRepository studentRepository;
    private final StudentDetailRepository studentDetailRepository;

    public List<StudentDTO.Response> getAllStudents() {
        return studentRepository.findAll()
                .stream()
                //Student => StudentDTO.Response
                //.map(student -> StudentDTO.Response.fromEntity(student))
                .map(StudentDTO.Response::fromEntity)
                .toList();
        //.collect(Collectors.toList());
    }

    public StudentDTO.Response getStudentById(Long id) {
        Student student = studentRepository.findByIdWithStudentDetail(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,"Student","Id",id));
        return StudentDTO.Response.fromEntity(student);
    }

    public StudentDTO.Response getStudentByStudentNumber(String studentNumber) {
        Student student = studentRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,"Student","학번",studentNumber));
        return StudentDTO.Response.fromEntity(student);
    }

    @Transactional
    public StudentDTO.Response createStudent(StudentDTO.Request request) {
        // Validate student number is not already in use
        if (studentRepository.existsByStudentNumber(request.getStudentNumber())) {
            throw new BusinessException(ErrorCode.STUDENT_NUMBER_DUPLICATE, request.getStudentNumber());
        }

        // Validate email is not already in use (if provided)
        if (request.getDetailRequest() != null &&
                request.getDetailRequest().getEmail() != null &&
                !request.getDetailRequest().getEmail().isEmpty() &&
                studentDetailRepository.existsByEmail(request.getDetailRequest().getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_DUPLICATE,request.getDetailRequest().getEmail());
        }

        // Validate phone number is not already in use
        if (request.getDetailRequest() != null &&
                studentDetailRepository.existsByPhoneNumber(request.getDetailRequest().getPhoneNumber())) {
            throw new BusinessException(ErrorCode.PHONE_NUMBER_DUPLICATE,request.getDetailRequest().getPhoneNumber());
        }

        // Create student entity
        Student studentEntity = Student.builder()
                .name(request.getName())
                .studentNumber(request.getStudentNumber())
                .build();

        // Create student detail if provided
        if (request.getDetailRequest() != null) {
            StudentDetail studentDetailEntity = StudentDetail.builder()
                    .address(request.getDetailRequest().getAddress())
                    .phoneNumber(request.getDetailRequest().getPhoneNumber())
                    .email(request.getDetailRequest().getEmail())
                    .dateOfBirth(request.getDetailRequest().getDateOfBirth())
                    //양방향 연관관계 - StudentDetail에게 Student 객체의 레퍼런스 알려주기
                    .student(studentEntity)
                    .build();
            //양방향 연관관계 - Student에게 StudentDetail 객체의 레퍼런스 알려주기
            studentEntity.setStudentDetail(studentDetailEntity);
        }

        // Save and return the student
        Student savedStudent = studentRepository.save(studentEntity);
        return StudentDTO.Response.fromEntity(savedStudent);
    }

    @Transactional
    public StudentDTO.Response updateStudent(Long id, StudentDTO.Request request) {
        // Find the student
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,"Student","Id",id));

        // Check if another student already has the student number
        if (!student.getStudentNumber().equals(request.getStudentNumber()) &&
                studentRepository.existsByStudentNumber(request.getStudentNumber())) {
            throw new BusinessException(ErrorCode.STUDENT_NUMBER_DUPLICATE, request.getStudentNumber());
        }

        // Update student basic info
        student.setName(request.getName());
        student.setStudentNumber(request.getStudentNumber());

        // Update student detail if provided
        if (request.getDetailRequest() != null) {
            StudentDetail studentDetail = student.getStudentDetail();

            // Create new detail if not exists
            // 등록할때 Student만 등록하고, StudentDetail을 등록하지 않은 경우의 Update
            if (studentDetail == null) {
                //StudentDetail 엔티티를 생성
                studentDetail = new StudentDetail();
                //수정하기 위해 입력받은 address로 StudentDetail 엔티티에 set 하기
                studentDetail.setAddress(request.getDetailRequest().getAddress());
                //수정하기 위해 입력받은 phoneNumber로 StudentDetail 엔티티에 set 하기
                studentDetail.setPhoneNumber(request.getDetailRequest().getPhoneNumber());
                //수정하기 위해 입력받은 email로 StudentDetail 엔티티에 set 하기
                studentDetail.setEmail(request.getDetailRequest().getEmail());
                //수정하기 위해 입력받은 dateOfBirth로 StudentDetail 엔티티에 set 하기
                studentDetail.setDateOfBirth(request.getDetailRequest().getDateOfBirth());
                //양방향 연관관계 설정
                studentDetail.setStudent(student);
                student.setStudentDetail(studentDetail);
            }

            // Validate email is not already in use (if changing)
            if (request.getDetailRequest().getEmail() != null &&
                    !request.getDetailRequest().getEmail().isEmpty() &&
                    (studentDetail.getEmail() == null || !studentDetail.getEmail().equals(request.getDetailRequest().getEmail())) &&
                    studentDetailRepository.existsByEmail(request.getDetailRequest().getEmail())) {
                throw new BusinessException(ErrorCode.EMAIL_DUPLICATE,request.getDetailRequest().getEmail());
            }

            // Validate phone number is not already in use (if changing)
            if ((studentDetail.getPhoneNumber() == null || !studentDetail.getPhoneNumber().equals(request.getDetailRequest().getPhoneNumber())) &&
                    studentDetailRepository.existsByPhoneNumber(request.getDetailRequest().getPhoneNumber())) {
                throw new BusinessException(ErrorCode.PHONE_NUMBER_DUPLICATE,request.getDetailRequest().getPhoneNumber());
            }

            // Update detail fields
            studentDetail.setAddress(request.getDetailRequest().getAddress());
            studentDetail.setPhoneNumber(request.getDetailRequest().getPhoneNumber());
            studentDetail.setEmail(request.getDetailRequest().getEmail());
            studentDetail.setDateOfBirth(request.getDetailRequest().getDateOfBirth());
        }

        // Save and return updated student
        Student updatedStudent = studentRepository.save(student);
        return StudentDTO.Response.fromEntity(updatedStudent);
    }

    @Transactional
    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,"Student","Id",id);
        }
        studentRepository.deleteById(id);
    }
}