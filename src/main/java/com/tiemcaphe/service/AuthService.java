package com.tiemcaphe.service;

import com.tiemcaphe.model.Account;
import com.tiemcaphe.model.Role;
import com.tiemcaphe.repository.AccountRepository;
import com.tiemcaphe.util.PasswordHasher;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public final class AuthService {

    public static final int MIN_PASSWORD = 6;
    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9._-]{3,32}");

    private final AccountRepository accounts;

    public AuthService(AccountRepository accounts) {
        this.accounts = accounts;
    }

    public Account login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            throw new BusinessException("Vui lòng nhập tên đăng nhập và mật khẩu.");
        }
        Optional<Account> found = accounts.findByUsername(username);
        if (found.isEmpty()) {
            throw new BusinessException("Tên đăng nhập hoặc mật khẩu không đúng.");
        }
        Account acc = found.get();
        String hash = accounts.passwordHashOf(acc.id()).orElse("");
        if (!PasswordHasher.verify(password, hash)) {
            throw new BusinessException("Tên đăng nhập hoặc mật khẩu không đúng.");
        }
        if (!acc.active()) {
            throw new BusinessException("Tài khoản đã bị khóa. Vui lòng liên hệ quản trị.");
        }
        return acc;
    }

    public List<Account> all() {
        return accounts.findAll();
    }

    public Optional<Account> findById(long id) {
        return accounts.findById(id);
    }

    public Account create(String username, String password, String fullName, Role role, boolean active) {
        validateUsername(username, null);
        validateFullName(fullName);
        validatePassword(password);
        long id = accounts.insert(username, PasswordHasher.hash(password), fullName, role, active);
        return accounts.findById(id).orElseThrow();
    }

    public Account update(long id, String username, String fullName, Role role, boolean active) {
        Account current = accounts.findById(id).orElseThrow(() -> new BusinessException("Tài khoản không tồn tại."));
        validateUsername(username, id);
        validateFullName(fullName);
        boolean losesAdmin = current.isAdmin() && current.active() && (role != Role.ADMIN || !active);
        if (losesAdmin && accounts.countActiveAdmins() <= 1) {
            throw new BusinessException("Phải còn ít nhất một tài khoản Quản trị đang hoạt động.");
        }
        accounts.update(id, username, fullName, role, active);
        return accounts.findById(id).orElseThrow();
    }

    public void resetPassword(long id, String newPassword) {
        validatePassword(newPassword);
        accounts.updatePassword(id, PasswordHasher.hash(newPassword));
    }

    public void changePassword(long id, String oldPassword, String newPassword) {
        String hash = accounts.passwordHashOf(id).orElseThrow(() -> new BusinessException("Tài khoản không tồn tại."));
        if (!PasswordHasher.verify(oldPassword, hash)) {
            throw new BusinessException("Mật khẩu hiện tại không đúng.");
        }
        validatePassword(newPassword);
        accounts.updatePassword(id, PasswordHasher.hash(newPassword));
    }

    public void delete(long id, Account actor) {
        Account target = accounts.findById(id).orElseThrow(() -> new BusinessException("Tài khoản không tồn tại."));
        if (actor != null && actor.id() == id) {
            throw new BusinessException("Không thể xóa tài khoản đang đăng nhập.");
        }
        if (target.isAdmin() && target.active() && accounts.countActiveAdmins() <= 1) {
            throw new BusinessException("Không thể xóa tài khoản Quản trị cuối cùng.");
        }
        accounts.delete(id);
    }

    private void validateUsername(String username, Long excludeId) {
        if (username == null || !USERNAME.matcher(username.trim()).matches()) {
            throw new BusinessException("Tên đăng nhập từ 3-32 ký tự, chỉ gồm chữ không dấu, số, dấu chấm, gạch dưới, gạch ngang.");
        }
        if (accounts.existsUsername(username, excludeId)) {
            throw new BusinessException("Tên đăng nhập đã tồn tại.");
        }
    }

    private static void validateFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new BusinessException("Vui lòng nhập họ tên.");
        }
    }

    private static void validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD) {
            throw new BusinessException("Mật khẩu phải có ít nhất " + MIN_PASSWORD + " ký tự.");
        }
    }
}
