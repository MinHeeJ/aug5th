package kr.ac.knue.commonfoundation.auth;

import java.util.Optional;

import org.springframework.stereotype.Component;

@Component
public class CurrentUserContext {

    private final ThreadLocal<SessionPrincipal> holder = new ThreadLocal<>();

    public Optional<SessionPrincipal> current() {
        return Optional.ofNullable(holder.get());
    }

    void set(SessionPrincipal principal) {
        holder.set(principal);
    }

    void clear() {
        holder.remove();
    }
}
