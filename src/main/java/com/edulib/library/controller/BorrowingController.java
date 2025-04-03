package com.edulib.library.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.edulib.library.model.Borrowing;
import com.edulib.library.service.BorrowingService;
import com.edulib.library.service.UserService;

import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = "http://192.168.1.5:8080")
@RestController
@RequestMapping("/api/borrowings")
@RequiredArgsConstructor
public class BorrowingController {

    private final BorrowingService borrowingService;
    private final UserService userService;

    @GetMapping()
    public ResponseEntity<List<Borrowing>> getAllBorrowings() {
        return ResponseEntity.ok(borrowingService.getAllBorrowings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Borrowing> getBorrowingById(@PathVariable Long id) {
        return borrowingService.getBorrowingById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Borrowing>> getBorrowingsByStudentId(@PathVariable String studentId) {
        return userService.getUserByStudentId(studentId)
                .map(user -> ResponseEntity.ok(borrowingService.getBorrowingsByUser(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<Borrowing>> getOverdueBooks() {
        return ResponseEntity.ok(borrowingService.getOverdueBooks());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Borrowing>> getBorrowingsByStatus(@PathVariable Borrowing.BorrowingStatus status) {
        return ResponseEntity.ok(borrowingService.getBorrowingsByStatus(status));
    }

    @PostMapping("/borrow")
    public ResponseEntity<Borrowing> borrowBook(@RequestBody Map<String, Object> request) {
        // Check if studentId is provided instead of userId
        Long userId;
        if (request.containsKey("studentId")) {
            String studentId = request.get("studentId").toString();
            userId = userService.getUserByStudentId(studentId)
                    .map(user -> user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found with studentId: " + studentId));
        } else {
            userId = Long.valueOf(request.get("userId").toString());
        }
        
        Long bookId = Long.valueOf(request.get("bookId").toString());
        int borrowDays = request.containsKey("borrowDays") ? 
                Integer.parseInt(request.get("borrowDays").toString()) : 14; // Default to 14 days
        
        return new ResponseEntity<>(borrowingService.borrowBook(userId, bookId, borrowDays), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<Borrowing> returnBook(@PathVariable Long id) {
        return borrowingService.getBorrowingById(id)
                .map(borrowing -> ResponseEntity.ok(borrowingService.returnBook(id)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBorrowing(@PathVariable Long id) {
        return borrowingService.getBorrowingById(id)
                .map(borrowing -> {
                    borrowingService.deleteBorrowing(id);
                    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/fine")
    public ResponseEntity<Double> calculateFine(@PathVariable Long id) {
        return borrowingService.getBorrowingById(id)
                .map(borrowing -> ResponseEntity.ok(borrowingService.calculateFine(id)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/pay-fine")
    public ResponseEntity<Void> payFine(@PathVariable Long id) {
        return borrowingService.getBorrowingById(id)
                .map(borrowing -> {
                    borrowingService.payFine(id);
                    return new ResponseEntity<Void>(HttpStatus.OK);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateBorrowingStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Borrowing.BorrowingStatus status = Borrowing.BorrowingStatus.valueOf(request.get("status"));
        
        return borrowingService.getBorrowingById(id)
                .map(borrowing -> {
                    borrowingService.updateBorrowingStatus(id, status);
                    return new ResponseEntity<Void>(HttpStatus.OK);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}