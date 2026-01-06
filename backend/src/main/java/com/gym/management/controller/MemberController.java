package com.gym.management.controller;

import com.gym.management.dto.MemberRequest;
import com.gym.management.dto.MemberResponse;
import com.gym.management.entity.Member;
import com.gym.management.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<Member> registerMember(@Valid @RequestBody MemberRequest request) {
        Member member = memberService.registerMember(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(member);
    }

    @GetMapping
    public ResponseEntity<List<MemberResponse>> getAllMembers() {
        List<MemberResponse> members = memberService.getAllMembers();
        return ResponseEntity.ok(members);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Member> getMemberById(@PathVariable Long id) {
        Member member = memberService.getMemberById(id);
        return ResponseEntity.ok(member);
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<Member> activateMember(@PathVariable Long id) {
        Member member = memberService.activateMember(id);
        return ResponseEntity.ok(member);
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Member> deactivateMember(@PathVariable Long id) {
        Member member = memberService.deactivateMember(id);
        return ResponseEntity.ok(member);
    }
}
