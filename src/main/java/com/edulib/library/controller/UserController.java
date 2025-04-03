package com.edulib.library.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.edulib.library.dto.UserStatsDTO;
import com.edulib.library.model.User;
import com.edulib.library.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = "http://192.168.1.5:8080")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<User> getUserByStudentId(@PathVariable String studentId) {
        return userService.getUserByStudentId(studentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String keyword) {
        return ResponseEntity.ok(userService.searchUsers(keyword));
    }

    @GetMapping("/type/{userType}")
    public ResponseEntity<List<User>> getUsersByType(@PathVariable User.UserType userType) {
        return ResponseEntity.ok(userService.getUsersByType(userType));
    }

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        return new ResponseEntity<>(userService.saveUser(user), HttpStatus.CREATED);
    }

    @PutMapping("/student/{studentId}")
    public ResponseEntity<User> updateUserByStudentId(@PathVariable String studentId, @Valid @RequestBody User user) {
        return userService.getUserByStudentId(studentId)
                .map(existingUser -> {
                    user.setId(existingUser.getId());
                    return ResponseEntity.ok(userService.saveUser(user));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/student/{studentId}")
    public ResponseEntity<Void> deleteUserByStudentId(@PathVariable String studentId) {
        return userService.getUserByStudentId(studentId)
                .map(user -> {
                    userService.deleteUser(user.getId());
                    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/student/{studentId}/activate")
    public ResponseEntity<Void> activateUserByStudentId(@PathVariable String studentId) {
        return userService.getUserByStudentId(studentId)
                .map(user -> {
                    userService.activateUser(user.getId());
                    return new ResponseEntity<Void>(HttpStatus.OK);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/student/{studentId}/deactivate")
    public ResponseEntity<Void> deactivateUserByStudentId(@PathVariable String studentId) {
        return userService.getUserByStudentId(studentId)
                .map(user -> {
                    userService.deactivateUser(user.getId());
                    return new ResponseEntity<Void>(HttpStatus.OK);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/student/{studentId}/borrowed-count")
    public ResponseEntity<Integer> getCurrentBorrowedBooksCountByStudentId(@PathVariable String studentId) {
        return userService.getUserByStudentId(studentId)
                .map(user -> ResponseEntity.ok(userService.getCurrentBorrowedBooksCount(user.getId())))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/can-borrow")
    public ResponseEntity<Boolean> canBorrowMoreBooks(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(userService.canBorrowMoreBooks(id)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/student/{studentId}/fine-pending")
    public ResponseEntity<UserStatsDTO> getFinePendingByStudentId(@PathVariable String studentId) {
        try {
            return userService.getUserByStudentId(studentId)
                .map(user -> {
                    UserStatsDTO stats = userService.getUserStats(user.getId());
                    return ResponseEntity.ok(stats);
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            UserStatsDTO errorStats = new UserStatsDTO();
            errorStats.setMessage("Error retrieving fine pending: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorStats);
        }
    }

    @GetMapping("/student/{studentId}/issued-books")
    public ResponseEntity<UserStatsDTO> getIssuedBooksCountByStudentId(@PathVariable String studentId) {
        try {
            return userService.getUserByStudentId(studentId)
                .map(user -> {
                    UserStatsDTO stats = userService.getUserStats(user.getId());
                    return ResponseEntity.ok(stats);
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            UserStatsDTO errorStats = new UserStatsDTO();
            errorStats.setMessage("Error retrieving issued books count: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorStats);
        }
    }

    @PutMapping("/student/{studentId}/pay-all-fines")
    public ResponseEntity<UserStatsDTO> payAllFinesByStudentId(@PathVariable String studentId) {
        try {
            return userService.getUserByStudentId(studentId)
                .map(user -> {
                    Double amountPaid = user.getTotalFinePending() != null ? user.getTotalFinePending() : 0.0;
                    user.setTotalFinePending(0.0);
                    userService.saveUser(user);
                    
                    UserStatsDTO stats = userService.getUserStats(user.getId());
                    stats.setMessage("All fines paid successfully. Amount paid: " + amountPaid);
                    
                    return ResponseEntity.ok(stats);
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            UserStatsDTO errorStats = new UserStatsDTO();
            errorStats.setMessage("Error paying fines: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorStats);
        }
    }

    @GetMapping("/student/{studentId}/can-borrow")
    public ResponseEntity<Boolean> canBorrowMoreBooksByStudentId(@PathVariable String studentId) {
        return userService.getUserByStudentId(studentId)
                .map(user -> ResponseEntity.ok(userService.canBorrowMoreBooks(user.getId())))
                .orElse(ResponseEntity.notFound().build());
    }
}