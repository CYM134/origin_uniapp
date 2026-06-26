package com.companyleveltraining.backend.security;

import java.io.IOException;
import java.util.Optional;

import com.companyleveltraining.backend.auth.UserAccountRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** sys_users.status 中代表「启用」的取值，详见 AdminUserService / 注册种子数据。 */
    private static final String STATUS_ACTIVE = "active";

    private final JwtService jwtService;
    private final RedisTokenSessionService tokenSessionService;
    private final UserAccountRepository userAccountRepository;

    public JwtAuthenticationFilter(JwtService jwtService, RedisTokenSessionService tokenSessionService,
                                   UserAccountRepository userAccountRepository) {
        this.jwtService = jwtService;
        this.tokenSessionService = tokenSessionService;
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7);
        if (!jwtService.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        SecurityUser user = jwtService.parseToken(token);

        // 回库按 id 校验账号当前状态：禁用/删除/不存在的账号即便持有未过期 token 也不再放行，
        // 让请求落到未认证 -> RestAuthenticationEntryPoint 返回 401。
        if (!isAccountActive(user.id())) {
            filterChain.doFilter(request, response);
            return;
        }
        if (!tokenSessionService.isSessionActive(user)) {
            filterChain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private boolean isAccountActive(Long userId) {
        if (userId == null) {
            return false;
        }
        Optional<UserAccountRepository.AccountStatus> status =
            userAccountRepository.findAccountStatusById(userId);
        return status
            .map(s -> !s.deleted() && STATUS_ACTIVE.equals(s.status()))
            .orElse(false);
    }
}
